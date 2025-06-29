package MochiMochiTalk.voice.nvoice;

import java.io.IOException;
import net.dv8tion.jda.api.audio.AudioSendHandler;

public interface TtsEngine extends AudioSendHandler {

  void say(String phrase) throws IOException, InterruptedException;
}
