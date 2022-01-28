package MochiMochiTalk.commands;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import MochiMochiTalk.lib.FileReadThreadImpl;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandSkip extends ListenerAdapter {

    private Logger logger = LoggerFactory.getLogger(CommandSkip.class);
    private VoiceChannel channel = null;
    private int skipSec = 0;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot())
            return;

        channel = event.getMember().getVoiceState().getChannel();

        
        String content = event.getMessage().getContentRaw();
        if(content.startsWith(App.prefix + "skip ")) {
            String[] split = content.split(" ");
            if(split.length != 2) {
                try {
                    skipSec = Integer.parseInt(split[1]);
                } catch(NumberFormatException e) {
                    logger.error("Invalid number format: {}", split[1], e);
                    skipSec = 0;
                }
            }
            logger.info("Skipping {} seconds.", skipSec);
            if(channel == null) {
                event.getChannel().sendMessage("ボイスチャンネルに入っている間しかこのコマンドは使用できません。").queue();
                return;
            }
            logger.info("Trying to skip reading");
            skipSec = Integer.parseInt(FileReadThreadImpl.skipSecond());
            event.getChannel().sendMessageFormat("読み上げを %d 秒間スキップします。", skipSec).queue(response -> {
                event.getGuild().getAudioManager().setSelfMuted(true);
                try {
                    TimeUnit.SECONDS.sleep(skipSec);
                } catch (InterruptedException e) {
                    logger.error("Error while sleeping", e);
                    Thread.currentThread().interrupt();
                }
                event.getGuild().getAudioManager().setSelfMuted(false);
            });
        }
    }
    
}
