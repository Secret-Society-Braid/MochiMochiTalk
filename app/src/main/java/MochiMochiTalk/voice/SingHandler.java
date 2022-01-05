package MochiMochiTalk.voice;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingHandler extends ListenerAdapter implements AudioSendHandler, AudioReceiveHandler  {

    private Logger logger = LoggerFactory.getLogger(SingHandler.class);
    //set queue
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    // receive handling
    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) { /* no operation required */ }

    @Override
    public boolean canReceiveUser() {
       return false;
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) { /* No need to implement this method */ }

    // send handling
    @Override
    public boolean canProvide() {
        return !queue.isEmpty();
    }

    @Override
public ByteBuffer provide20MsAudio() { return null;}
    
    @Override
    public boolean isOpus() {
        return false;
    }

    public void addQueue(byte[] data) {
        queue.add(data);
    }
}
