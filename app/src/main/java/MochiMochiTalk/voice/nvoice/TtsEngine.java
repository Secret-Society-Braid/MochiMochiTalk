package MochiMochiTalk.voice.nvoice;

import java.io.IOException;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.User;

public interface TtsEngine extends AudioSendHandler {

  void say(String phrase, User author) throws IOException, InterruptedException;
}
