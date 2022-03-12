package MochiMochiTalk.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.text.WrappedPlainView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GlobalChannelAddCommand extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalChannelAddCommand.class);

    private static final String GLOBAL_CHANNEL_LIST_FILE_NAME = "global_channel_list.json";

    private WebhookClient webhookClient;
    
    private volatile static ConcurrentHashMap<String, String> globalChannelMap = new ConcurrentHashMap<>();

    public GlobalChannelAddCommand() {
        if(Files.notExists(Paths.get(GLOBAL_CHANNEL_LIST_FILE_NAME))) {
            globalChannelMap.put("649603185115267092", "951674644472143873");
            write(globalChannelMap);
        }
        globalChannelMap = read();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User user = event.getAuthor();
        webhookClient = channel.get

        if(user.isBot())
            return;
        if(content.startsWith(App.prefix + "global ")) {
            String[] split = content.split(" ");
            if(split[1].equals("add"))
                addEvent(event);
            else if(split[1].equals("remove"))
                removeEvent(event);
            else if(split[1].equals("list"))
                listEvent(event);
            else
                channel.sendMessage("```" + App.prefix + "global add <channel id>\n" + App.prefix + "global remove <channel id>\n" + App.prefix + "global list```").queue();
        }
    }

    public static synchronized void write(ConcurrentHashMap<String, String> data) {
        try {
            new ObjectMapper().writeValue(Paths.get(GLOBAL_CHANNEL_LIST_FILE_NAME).toFile(), globalChannelMap);
        } catch (IOException e) {
            LOG.error("Failed to write file.", e);
        }
    }

    public synchronized static ConcurrentHashMap<String, String> read() {
        try {
            return new ObjectMapper().readValue(Paths.get(GLOBAL_CHANNEL_LIST_FILE_NAME).toFile(), new TypeReference<ConcurrentHashMap<String, String>>() {});
        } catch (IOException e) {
            LOG.error("Failed to read file.", e);
        }
        return null;
    }

    public synchronized void addEvent(MessageReceivedEvent event) {
        globalChannelMap = read();
        String guildId = event.getGuild().getId();
        String channelId = event.getChannel().getId();
        if(globalChannelMap.containsKey(guildId)) {
            event.getChannel().sendMessage("このサーバーは既に他のチャンネルが登録されています。自動的に情報をこのチャンネルで上書きします。").queue(response -> {
                response.delete().queueAfter(5, TimeUnit.SECONDS);
            });
        }
        Guild guild = event.getGuild();
        globalChannelMap.put(guildId, channelId);
        write(globalChannelMap);

    }

    public synchronized void removeEvent(MessageReceivedEvent event) {
        globalChannelMap = read();
        String guildId = event.getGuild().getId();
        if(!globalChannelMap.containsKey(guildId)) {
            event.getChannel().sendMessage("このサーバーは登録されていません。").queue(response -> {
                response.delete().queueAfter(5, TimeUnit.SECONDS);
            });
        }
        globalChannelMap.remove(guildId);
        write(globalChannelMap);
    }

    public synchronized void listEvent(MessageReceivedEvent event) {
        globalChannelMap = read();
        StringBuilder sb = new StringBuilder();
        sb.append("```");
        for(String key : globalChannelMap.keySet()) {
           Guild guild = event.getJDA().getGuildById(key);
           MessageChannel channel = event.getJDA().getTextChannelById(globalChannelMap.get(key));
           sb.append(guild.getName() + " : " + channel.getName() + "\n");
        }
        sb.append("```");
        event.getChannel().sendMessage(sb.toString()).queue();
    }

    public static synchronized ConcurrentHashMap<String, String> getGlobalChannelMap() {
        globalChannelMap = read();
        return globalChannelMap;
    }
}
