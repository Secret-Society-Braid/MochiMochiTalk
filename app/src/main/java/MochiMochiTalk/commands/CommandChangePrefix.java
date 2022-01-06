package MochiMochiTalk.commands;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandChangePrefix extends ListenerAdapter {
    
    private static Logger logger = LoggerFactory.getLogger(CommandChangePrefix.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = event.getAuthor();
        MessageChannel channel = event.getChannel();
        if(author.isBot()) {
            return;
        }
        if(content.startsWith(App.prefix + "prefix")) {
            String[] split = content.split(" ");
            if(split.length == 2) {
                changePrefix(split[1]);
                write(split[1]);
                channel.sendMessage("prefixを" + App.prefix + "に変更しました").queue();
            } else {
                channel.sendMessage("prefixを変更するには2つの引数が必要です").queue();
            }
        }
    }

    private static void changePrefix(String prefix) {
        App.prefix = prefix;
    }

    private static void write(String prefix) {
        ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
        Map<String, Object> data = new HashMap<>();
        data.put("prefix", prefix);
        data.put("token", App.token);
        try {
            writer.writeValue(Paths.get("property.json").toFile(), data);
        } catch (IOException e) {
            logger.error("Failed to write file.", e);
        }
    }
}
