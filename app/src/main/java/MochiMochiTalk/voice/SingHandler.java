package MochiMochiTalk.voice;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingHandler implements AudioSendHandler, AudioReceiveHandler {

    private Logger logger = LoggerFactory.getLogger(SingHandler.class);
    //set queue
    private final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();

    // receive handling
    @Override
    public boolean canReceiveCombined() {
        return queue.size() < 20;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        if(combinedAudio.getUsers().isEmpty()) {
            logger.info("No users in the queue.");
            return;
        }
        byte[] data = combinedAudio.getAudioData(1.0F);
        queue.add(data);
    }

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
    public ByteBuffer provide20MsAudio() {
        byte[] data = queue.poll();
        return data == null ? null : ByteBuffer.wrap(data);
    }
    
    @Override
    public boolean isOpus() {
        return false;
    }
}
