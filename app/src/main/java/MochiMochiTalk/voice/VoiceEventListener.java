package MochiMochiTalk.voice;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import MochiMochiTalk.commands.CommandDictionary;
import MochiMochiTalk.commands.CommandWhatsNew;
import MochiMochiTalk.lib.AllowedVCRead;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

public class VoiceEventListener extends ListenerAdapter {
    
    private Logger logger = LoggerFactory.getLogger(VoiceEventListener.class);

    private static final CountingThreadFactory THREAD_FACTORY = new CountingThreadFactory(() -> "MochiMochiTalk", "AFK-Checker");

    private MessageChannel channel = null;
    private AudioManager audioManager;
    private DeprecatedTTSEngine ttsEngine = new DeprecatedTTSEngine();
    private boolean flag = false;
    private String replaced = "";
    private boolean isReplaced = false;
    private ScheduledExecutorService service;
    private List<String> allowed = new ArrayList<>();

    public VoiceEventListener() {
        AllowedVCRead read = new AllowedVCRead();
        allowed = read.read();
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        String content = replaceMentions(event);
        audioManager = event.getGuild().getAudioManager();

        // ignore messages from bots
        if (author.isBot()) {
            return;
        }

        if(content.equalsIgnoreCase(App.prefix + "connect") || content.equalsIgnoreCase(App.prefix + "c")) {
            if(!CheckVCAllowed(event)) {
                logger.warn("VC is not allowed this server. : {}", event.getGuild().getName());
                event.getChannel().sendMessage("使用しているAPIの関係上、むつコード様以外のサーバーでは読み上げ機能は使用できません。ごめんなさい。").queue();
                return;
            }
            logger.info("Connecting to voice channel.");
            onConnectCommand(event);
        }

        if(content.equalsIgnoreCase(App.prefix + "disconnect") || content.equalsIgnoreCase(App.prefix + "dc")) {
            logger.info("Disconnecting from voice channel.");
            onDisconnectCommand(event);
        }

        String[] split = content.split("\n");

        boolean isEscaped = false;

        if(content.length() > 40) {
            logger.info("Received long message.");
            logger.info("Escaped: {}", content);
            logger.info("target messageID: {}", message.getId());
            return;
        }

        for( String str : split ) {
            isEscaped = escapeProgression(str);
            if(isEscaped)
                break;
        }

        if(isEscaped) {
            logger.info("Escaped: {}", content);
            logger.info("target messageID: {}", message.getId());
            return;
        }

        if(flag && !content.startsWith(App.prefix)) {
            if(!event.getChannel().equals(channel)) {
                logger.info("Message is not in the same channel as the voice channel.");
                return;
            }
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
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("テキスト読み上げBot「聖ちゃんの聖歌隊」");
        builder.setDescription("現在以下の条件に当てはまらない文章は読まれません。注意してください。");
        builder.addField("読まれないものの一覧", "・文字数が40文字以上の文章\n\n・サーバーオリジナル絵文字\n\n・コードブロックを含む文章\n\n・URLを含む文章", false);
        channel.sendMessageEmbeds(builder.build()).queue();
        CommandWhatsNew whatsNew = CommandWhatsNew.getInstance();
        channel.sendMessageEmbeds(whatsNew.buildMessage()).queue();
        service = Executors.newScheduledThreadPool(1, THREAD_FACTORY);
        service.scheduleWithFixedDelay(this::checkVoiceChannel, 1, 5, TimeUnit.SECONDS);
        logger.info("Connected to voice channel.");
    }

    private void onDisconnectCommand(MessageReceivedEvent event) {
        service.shutdownNow();
        audioManager.closeAudioConnection();
        channel = null;
        flag = false;
        event.getChannel().sendMessage("終わりますか？お疲れ様でした…").queue();
        logger.info("Disconnected from voice channel.");
    }


    private boolean escapeProgression(String content) {
        if(content.matches(".*<:[A-Za-z].+\\d*>*")) {
            logger.info("Received emoji.");
            return true;
        }

        if(content.length() > 40) {
            logger.info("Received long message.");
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

    private void checkVoiceChannel() {
        if(audioManager.isConnected()) {
            if(audioManager.getConnectedChannel().getMembers().size() == 1) {
                audioManager.closeAudioConnection();
                channel.sendMessage("誰もいないので私も戻りますね。お疲れ様でした。").queue();
                channel = null;
                flag = false;
                logger.info("Disconnected from voice channel caused by AFK.");
            }
        }
    }

    private boolean CheckVCAllowed(MessageReceivedEvent event) {
        if(allowed.isEmpty()) {
            return false;
        }
        for(String str : allowed) {
            if(str.equals(event.getGuild().getId())) {
                return true;
            }
        }
        return false;
    }

    private String replaceMentions(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        Pattern plainUserPattern = Pattern.compile("<@[0-9].*>");
        Pattern nicknamedUserPattern = Pattern.compile("<@![0-9].*>");
        Pattern rolePattern = Pattern.compile("<&[0-9].*>");
        Pattern channelPattern = Pattern.compile("<#[0-9].*>");
        Matcher plainUserMatcher = plainUserPattern.matcher(content);
        Matcher nicknamedUserMatcher = nicknamedUserPattern.matcher(content);
        Matcher roleMatcher = rolePattern.matcher(content);
        Matcher channelMatcher = channelPattern.matcher(content);
        while(plainUserMatcher.find()) {
            String mention = plainUserMatcher.group();
            String id = mention.substring(2, mention.length() - 1);
            User user = event.getGuild().getMemberById(id).getUser();
            content = content.replace(mention, user.getName() + "さん");
        }
        while(nicknamedUserMatcher.find()) {
            String mention = nicknamedUserMatcher.group();
            String id = mention.substring(3, mention.length() - 1);
            User user = event.getGuild().getMemberById(id).getUser();
            content = content.replace(mention, user.getName() + "さん");
        }
        while(roleMatcher.find()) {
            String mention = roleMatcher.group();
            String id = mention.substring(2, mention.length() - 1);
            Role role = event.getGuild().getRoleById(id);
            content = content.replace(mention, "役職、" + role.getName() + " のみなさん");
        }
        while(channelMatcher.find()) {
            String mention = channelMatcher.group();
            String id = mention.substring(2, mention.length() - 1);
            TextChannel tmpChannel = event.getGuild().getTextChannelById(id);
            content = content.replace(mention, "チャンネル、" + tmpChannel.getName());
        }
        return content;
    }
}
