package MochiMochiTalk.voice.nvoice;

import MochiMochiTalk.lib.CacheFileController;
import MochiMochiTalk.util.DiscordServerOperatorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class VoiceVoxTtsEngine implements TtsEngine {

  public static final int AUDIO_FRAME = 3840; // 48000 / 50 (number of 20 ms in a second) * 2 (16-bit samples) * 2 (channels)
  private static final OkHttpClient client = new OkHttpClient.Builder().build();
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String VOICEVOX_API_URL = "http://voicevox_api:50021";
  private byte[] out;
  private int index;
  private ByteBuffer lastFrame;
  private boolean isSpeaking = false;
  private final PCMByteCacheLogic cacheLogic;
  private final CacheFileController cacheController;
  private List<VoicevoxSpeaker> speakers;
  private static final SecureRandom secureRandom = new SecureRandom();
  private final Map<Integer, Set<User>> tiedSpeakerCache;

  public VoiceVoxTtsEngine() {
    this.out = new byte[0];

    // load cache
    cacheLogic = new PCMByteCacheLogic();
    cacheController = CacheFileController.getInstance();
    loadSpeakers();
    this.tiedSpeakerCache = new ConcurrentHashMap<>(10);
  }

  private void loadSpeakers() {
    final String speakersPath = "/speakers";
    Request speakersRequest = new Request.Builder()
      .url(VOICEVOX_API_URL + speakersPath)
      .get()
      .addHeader("Accept", "application/json")
      .build();
    try (Response response = client.newCall(speakersRequest).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }
      String jsonResponse = Objects.requireNonNull(response.body()).string();
      this.speakers = MAPPER.readValue(jsonResponse,
        MAPPER.getTypeFactory().constructCollectionType(List.class, VoicevoxSpeaker.class));
      log.info("Loaded {} speakers from VoiceVox API", speakers.size());
    } catch (IOException e) {
      throw new RuntimeException("Failed to load speakers from VoiceVox API", e);
    }
  }

  private int selectSpeakerId(User user) {
    if (speakers == null || speakers.isEmpty()) {
      throw new IllegalStateException("No speakers loaded from VoiceVox API");
    }
    if (DiscordServerOperatorUtil.getBotDevUserId().equals(user.getId())) {
      return 8; // Default speaker ID for bot developer
    }
    // 既存の紐付けを探す
    for (Map.Entry<Integer, Set<User>> entry : tiedSpeakerCache.entrySet()) {
      if (entry.getValue().contains(user)) {
        // SpeakerIDに紐付いている場合、そのSpeakerを返す
        return speakers.stream()
          .flatMap(s -> s.getStyles().stream())
          .filter(s -> s.getId() == entry.getKey())
          .findFirst()
          .map(VoicevoxSpeaker.Style::getId)
          .orElse(8); // Default to a common speaker ID if not found
      }
    }
    // Randomly select a speaker and style
    VoicevoxSpeaker randomSpeaker = speakers.get(secureRandom.nextInt(speakers.size()));
    VoicevoxSpeaker.Style style = randomSpeaker.getStyles()
      .get(secureRandom.nextInt(randomSpeaker.getStyles().size()));
    tiedSpeakerCache
      .computeIfAbsent(style.getId(), k -> ConcurrentHashMap.newKeySet())
      .add(user);
    return style.getId();
  }

  private String retrieveJsonAudioQuery(String phrase, int speakerId) {
    final String audioQueryPath = "/audio_query?speaker=%s&text=%s";
    final RequestBody body = RequestBody.create("", MediaType.get("application/json"));
    Request audioQueryRequest = new Request.Builder()
      .url(String.format(VOICEVOX_API_URL + audioQueryPath, speakerId, phrase))
      .post(body)
      .addHeader("Accept", "application/json")
      .build();
    String jsonAudioQuery;
    try (Response response = client.newCall(audioQueryRequest).execute()) {
      jsonAudioQuery = Objects.requireNonNull(response.body()).string();
      log.trace("audio query created for phrase '{}'", phrase);
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve audio query from VoiceVox API", e);
    }
    return jsonAudioQuery;
  }

  private byte[] tts(String audioQuery, int speakerId) throws IOException {
    final String synthesisPath = "/synthesis?speaker=%s";
    RequestBody body = RequestBody.create(audioQuery, MediaType.get("application/json"));
    Request synthesisRequest = new Request.Builder()
      .url(String.format(VOICEVOX_API_URL + synthesisPath, speakerId))
      .post(body)
      .addHeader("Accept", "audio/wav")
      .build();

    try (Response response = client.newCall(synthesisRequest).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }
      log.trace("synthesize completed.");
      return Objects.requireNonNull(response.body()).bytes();
    }
  }

  @Override
  public void say(String phrase, User author) throws IOException, InterruptedException {
    while (isSpeaking) {
      TimeUnit.SECONDS.sleep(1);
    }
    this.isSpeaking = true;
    this.index = Integer.MAX_VALUE;

    PCMByteCacheLogic.CacheResponse response = cacheLogic.checkCache(phrase);
    byte[] data = null;

    if (response.pcmIfCached != null) {
      log.info("Using cached TTS");
      this.out = response.pcmIfCached;
    } else {
      int speakerId = selectSpeakerId(author);
      byte[] ttsData = tts(retrieveJsonAudioQuery(phrase, speakerId), speakerId);
      data = convertToDiscordCompatible(resampling(ttsData, 16, 24000, 48000));
      log.info("Using TTS");
      this.out = data;
    }

    if (response.shouldCache) {
      log.info("Caching TTS bytes");
      cacheController.storeCache(phrase, data);
    }

    this.index = 0;
  }

  private byte[] resampling(byte[] input, int bitsPerSample, int sourceSampleRate,
    int targetSampleRate) {
    int bytePerSample = bitsPerSample / 8;
    int numSamples = input.length / bytePerSample;
    short[] amplitudes = new short[numSamples];
    int pointer = 0;
    for (int i = 0; i < numSamples; i++) {
      short amplitude = 0;
      for (int byteNumber = 0; byteNumber < bytePerSample; byteNumber++) {
        amplitude |= (short) ((input[pointer++] & 0xFF) << (byteNumber * 8));
      }
      amplitudes[i] = amplitude;
    }
    // Linear interpolation
    short[] targetSample = interpolate(sourceSampleRate, targetSampleRate, amplitudes);
    int targetLength = targetSample.length;
    byte[] bytes;
    if (bytePerSample == 1) {
      bytes = new byte[targetLength];
      for (int i = 0; i < targetLength; i++) {
        bytes[i] = (byte) targetSample[i];
      }
    } else {
      bytes = new byte[targetLength * 2];
      for (int i = 0; i < targetSample.length; i++) {
        bytes[i * 2] = (byte) (targetSample[i] & 0xff);
        bytes[i * 2 + 1] = (byte) ((targetSample[i] >> 8) & 0xff);
      }
    }
    return bytes;
  }

  private short[] interpolate(int beforeSampleRate, int newSampleRate, short[] samples) {
    if (beforeSampleRate == newSampleRate) {
      return samples;
    }
    int newLength = Math.round((float) samples.length / beforeSampleRate * newSampleRate);
    float lengthMultiplier = (float) newLength / samples.length;
    short[] interpolatedSamples = new short[newLength];
    for (int i = 0; i < newLength; i++) {
      float currentPosition = i / lengthMultiplier;
      int nearestLeft = (int) currentPosition;
      int nearestRight = nearestLeft + 1;
      if (nearestRight >= samples.length) {
        nearestRight = samples.length - 1;
      }
      float slope = samples[nearestRight] - samples[nearestLeft];
      float positionFromLeft = currentPosition - nearestLeft;
      interpolatedSamples[i] = (short) (slope * positionFromLeft + samples[nearestLeft]);
    }
    return interpolatedSamples;
  }

  private byte[] convertToDiscordCompatible(byte[] pcm) {
    // Add a bit of silence to the end of the audio data to avoid cutting off the last frame
    byte[] converted = new byte[AUDIO_FRAME + pcm.length * 2 + (AUDIO_FRAME
      - pcm.length * 2 % AUDIO_FRAME)]; // Ensures the length is a multiple of AUDIO_FRAME
    for (int i = AUDIO_FRAME; i < pcm.length; i += 2) {
      short reversed = Short.reverseBytes((short) ((pcm[i] << 8) | (
        pcm[i + 1] & 0xFF)));
      byte low = (byte) (reversed >> 8);
      byte high = (byte) (reversed & 0x00FF);

      // reverse bytes and double to convert to stereo
      converted[i * 2] = low;
      converted[i * 2 + 1] = high;
      converted[i * 2 + 2] = low;
      converted[i * 2 + 3] = high;
    }
    return converted;
  }

  @Override
  public boolean canProvide() {
    boolean provide = index < out.length;

    if (provide) {
      lastFrame = ByteBuffer.wrap(out, index, AUDIO_FRAME);
      index += AUDIO_FRAME;

      if (index >= out.length) {
        log.info("Bot may finish speaking");
        this.isSpeaking = false;
      }
    }

    return provide;
  }

  @Nullable
  @Override
  public ByteBuffer provide20MsAudio() {
    return this.lastFrame;
  }

  @Override
  public boolean isOpus() {
    return false; // VoiceVox uses PCM, not Opus
  }
}
