package MochiMochiTalk.commands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandShutdown extends ListenerAdapter {

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(CommandShutdown.class);

    private static final List<String> IDs = List.of("399143446939697162", "666213020653060096", "682079802605174794", "365695324947349505", "492145462908944422", "538702103372103681", "482903571625410560", "686286747084390433", "706819045286215765");

    // Shutdown command
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        LOG.info("Message received: {}", event.getMessage().getContentRaw());
        String content = event.getMessage().getContentRaw();
        if (content.equalsIgnoreCase(App.prefix + "shutdown")) {
            if(IDs.contains(event.getAuthor().getId())) {
                LOG.info("Shutdown command received");
                event.getChannel().sendMessage("終了しています…おやすみなさい。プロデューサーさん").queue();
                event.getJDA().shutdown();
            }
            event.getChannel().sendMessage("このコマンドは管理者のみ使用できます。").queue();
        }
    }
    
}
