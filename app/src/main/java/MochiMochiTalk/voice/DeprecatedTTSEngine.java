package MochiMochiTalk.voice;

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
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeprecatedTTSEngine implements AudioSendHandler {

  public static final int AUDIO_FRAME = 3840; // 48000 / 50 (number of 20 ms in a second) * 2 (16-bit samples) * 2 (channels)

  private Logger logger = LoggerFactory.getLogger(DeprecatedTTSEngine.class);
  private byte[] out;
  private int index;
  private ByteBuffer lastFrame;
  private boolean isSpeaking = false;

  private DeprecatedTTSCache ttsCache;

  public DeprecatedTTSEngine() {
    this.out = new byte[0];

    // Load the cache
    try {
      ttsCache = new DeprecatedTTSCache();
    } catch (Exception e) {
      logger.error("Failed to load cache", e);
    }
  }

  byte[] tts(String text) throws IOException {
    CredentialsProvider provider = FixedCredentialsProvider.create(
        ServiceAccountCredentials.fromStream(
            this.getClass().getResourceAsStream("/GOOGLE_APP_CREDENTIALS.json")));
    TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
        .setCredentialsProvider(provider).build();
    try (TextToSpeechClient client = TextToSpeechClient.create(settings)) {
      SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

      VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("ja_JP")
          .setSsmlGender(SsmlVoiceGender.NEUTRAL).build();

      AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.LINEAR16)
          .setSampleRateHertz(48_000).setVolumeGainDb(-2.0).build();

      SynthesizeSpeechResponse response = client.synthesizeSpeech(input, voice, audioConfig);

      ByteString audioContents = response.getAudioContent();

      byte[] pcm = audioContents.toByteArray();

      // Three things need to happen - big endian, stereo, pad to a multiple of 3840
      byte[] converted = new byte[pcm.length * 2 + (AUDIO_FRAME
          - pcm.length * 2 % AUDIO_FRAME)]; // ensures converted is a multiple of AUDIO_FRAME
      for (int i = 0; i < pcm.length; i += 2) {
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

  public void say(String phrase) throws InterruptedException, IOException {
    while (this.isSpeaking) {
      TimeUnit.SECONDS.sleep(1);
    }
    this.isSpeaking = true;
    if (ttsCache != null) {
      DeprecatedTTSCache.CacheResponse response = ttsCache.checkCache(phrase);

      byte[] data = null;

      if (response.pcmIfCached != null) {
        logger.info("Using cached TTS");
        this.out = response.pcmIfCached;
      } else {
        data = tts(phrase);
        logger.info("Using TTS");
        this.out = data;
      }

      if (response.shouldCache) {
        logger.info("Caching TTS");
        ttsCache.cache(phrase, data);
      }
    } else {
      logger.info("Using TTS");
      this.out = tts(phrase);
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
        logger.info("maybe Finished speaking");
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
