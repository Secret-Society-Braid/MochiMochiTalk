package MochiMochiTalk.voice;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import MochiMochiTalk.commands.CommandDictionary;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class VoiceEventListener extends ListenerAdapter {
    
    private Logger logger = LoggerFactory.getLogger(VoiceEventListener.class);

    private MessageChannel channel = null;
    private AudioManager audioManager;
    private DeprecatedTTSEngine ttsEngine = new DeprecatedTTSEngine();
    private boolean flag = false;
    private String replaced = "";
    private boolean isReplaced = false;


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        logger.info("Message received: {}", event.getMessage().getContentRaw());
        User author = event.getAuthor();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        audioManager = event.getGuild().getAudioManager();

        // ignore messages from bots
        if (author.isBot()) {
            return;
        }

        if(content.equalsIgnoreCase(App.prefix + "connect") || content.equalsIgnoreCase(App.prefix + "c")) {
            logger.info("Connecting to voice channel.");
            onConnectCommand(event);
        }

        if(content.equalsIgnoreCase(App.prefix + "disconnect") || content.equalsIgnoreCase(App.prefix + "dc")) {
            logger.info("Disconnecting from voice channel.");
            onDisconnectCommand(event);
        }

        String[] split = content.split("\n");

        boolean isEscaped = false;

        for( String str : split ) {
            isEscaped = escapeProgression(str);
            if(isEscaped)
                break;
        }

        if(isEscaped) {
            return;
        }

        String unicoded = convertToUnicode(content);
        logger.info("Unicoded: {}", unicoded);

        if(flag && !content.startsWith(App.prefix)) {
            logger.info("Analyzing message: {}", content);
            logger.info("Channel: {}", channel.getName());
            logger.info("Author: {}", author.getName());
            logger.info("Guild: {}", event.getGuild().getName());
            Map<String, String> dic = CommandDictionary.getDictionary();
            dic.forEach((key, value) -> {
                if(content.contains(key)) {
                    logger.info("Found Dic: {}", key);
                    replaced = content.replace(key, value);
                    logger.info("Dic: {}", value);
                    isReplaced = true;
                }
            });
            try {
                if(isReplaced) {
                    ttsEngine.say(replaced);
                } else {
                    ttsEngine.say(content);
                }
            } catch (Exception e) {
                logger.error("Cannot handle tts:", e);
            }
            isReplaced = false;
        }
    }

    private void onConnectCommand(MessageReceivedEvent event) {
        VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
        if(voiceChannel == null) {
            event.getChannel().sendMessage("まだボイスチャンネルに入っていないみたいです…プロデューサーさん").queue();
            logger.info("User is not in a voice channel.");
            return;
        }
        audioManager.setSendingHandler(ttsEngine);
        audioManager.openAudioConnection(voiceChannel);
        channel = event.getChannel();
        flag = true;
        channel.sendMessage("準備ができました！いつでもお喋りできます…！").queue();
        logger.info("Connected to voice channel.");
    }

    private void onDisconnectCommand(MessageReceivedEvent event) {
        audioManager.closeAudioConnection();
        channel = null;
        flag = false;
        event.getChannel().sendMessage("終わりますか？お疲れ様でした…").queue();
        logger.info("Disconnected from voice channel.");
    }

    private static String convertToUnicode(String original)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < original.length(); i++) {
            sb.append(String.format("\\u%04X", Character.codePointAt(original, i)));
            }
        String unicode = sb.toString();
        return unicode;
        }

    private boolean escapeProgression(String content) {
        if(content.matches(".*<:[A-Za-z].+\\d*>*")) {
            logger.info("Received emoji.");
            return true;
        }


        if(content.startsWith("```")) {
            logger.info("Received code block.");
            return true;
        }

        if(content.matches("\\b.*(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
            logger.info("Received URL.");
            return true;
        }
        return false;
    }
}
