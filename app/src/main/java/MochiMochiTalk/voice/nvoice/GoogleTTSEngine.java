package MochiMochiTalk.voice.nvoice;

import MochiMochiTalk.lib.CacheFileController;
import MochiMochiTalk.voice.DeprecatedTTSEngine;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleTTSEngine implements TtsEngine {

  public static final int AUDIO_FRAME = DeprecatedTTSEngine.AUDIO_FRAME;

  private byte[] out;
  private int index;
  private ByteBuffer lastFrame;
  private boolean isSpeaking = false;

  private final PCMByteCacheLogic cacheLogic;
  private final CacheFileController cacheController;

  public GoogleTTSEngine() {
    this.out = new byte[0];

    // load cache
    cacheLogic = new PCMByteCacheLogic();
    cacheController = CacheFileController.getInstance();
  }

  byte[] tts(String phrase) throws IOException {
    CredentialsProvider provider = FixedCredentialsProvider.create(
        ServiceAccountCredentials.fromStream(
            this.getClass().getResourceAsStream("/GOOGLE_APP_CREDENTIALS.json")));
    TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
        .setCredentialsProvider(provider).build();
    try (TextToSpeechClient client = TextToSpeechClient.create(settings)) {
      SynthesisInput input = SynthesisInput.newBuilder().setText(phrase).build();

      VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("ja_JP")
          .setSsmlGender(SsmlVoiceGender.NEUTRAL).build();

      AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16)
          .setSampleRateHertz(48_000).setVolumeGainDb(-2.0).build();

      SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

      ByteString audioContents = response.getAudioContent();

      byte[] pcm = audioContents.toByteArray();

      // Three things need to happen - big endian, stereo, pad to a multiple of 3840
      // add a frame of silence at the beginning so that the sound doesn't clip weirdly
      byte[] converted = new byte[AUDIO_FRAME + pcm.length * 2 + (AUDIO_FRAME
          - pcm.length * 2 % AUDIO_FRAME)];
      for (int i = AUDIO_FRAME; i < pcm.length; i += 2) {
        short reversed = Short.reverseBytes((short) ((pcm[i] << 8) | (pcm[i + 1] & 0xFF)));
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
  }

  public void say(String phrase) throws IOException, InterruptedException {
    while (this.isSpeaking) {
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
      data = tts(phrase);
      log.info("Using TTS");
      this.out = data;
    }

    if (response.shouldCache) {
      log.info("Caching TTS bytes");
      cacheController.storeCache(phrase, data);
    }

    this.index = 0;
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
    return lastFrame;
  }

  @Override
  public boolean isOpus() {
    return false;
  }

}
