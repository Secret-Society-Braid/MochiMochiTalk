package MochiMochiTalk.voice;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

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
        String rawContent = message.getContentRaw();
        audioManager = event.getGuild().getAudioManager();
        logger.debug("connect command fired");

        // ignore messages from bots
        if (author.isBot()) {
            return;
        }
        logger.debug("connect command fired");
        if(rawContent.equalsIgnoreCase(App.prefix + "connect") || rawContent.equalsIgnoreCase(App.prefix + "c")) {
            logger.debug("connect command fired");
            if(!CheckVCAllowed(event)) {
                logger.warn("VC is not allowed this server. : {}", message.getGuild().getName());
                event.getChannel().sendMessage("??????????????????API??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????").queue();
                return;
            }
            logger.info("Connecting to voice channel.");
            onConnectCommand(event);
        }

        if(rawContent.equalsIgnoreCase(App.prefix + "disconnect") || rawContent.equalsIgnoreCase(App.prefix + "dc")) {
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

        if(flag && !rawContent.startsWith(App.prefix)) {
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
        VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        if(voiceChannel == null) {
            event.getChannel().sendMessage("????????????????????????????????????????????????????????????????????????????????????????????????").queue();
            logger.info("User is not in a voice channel.");
            return;
        }
        audioManager.setSendingHandler(ttsEngine);
        audioManager.openAudioConnection(voiceChannel);
        channel = event.getChannel();
        flag = true;
        channel.sendMessage("??????????????????????????????????????????????????????????????????").queue();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("????????????????????????Bot??????????????????????????????");
        builder.setDescription("??????????????????????????????????????????????????????????????????????????????????????????????????????");
        builder.addField("??????????????????????????????", "???????????????40?????????????????????\n\n???????????????????????????????????????\n\n???????????????????????????????????????\n\n???URL???????????????", false);
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
        event.getChannel().sendMessage("?????????????????????????????????????????????").queue();
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

        if(content.matches("\\b.*(http?|https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
            logger.info("Received URL.");
            return true;
        }
        return false;
    }

    private void checkVoiceChannel() {
        if(audioManager.isConnected()) {
            if(audioManager.getConnectedChannel().getMembers().size() == 1) {
                audioManager.closeAudioConnection();
                channel.sendMessage("?????????????????????????????????????????????????????????????????????").queue();
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
        Pattern repeatedPattern = Pattern.compile("(!|w|???|???)");
        Matcher plainUserMatcher = plainUserPattern.matcher(content);
        Matcher nicknamedUserMatcher = nicknamedUserPattern.matcher(content);
        Matcher roleMatcher = rolePattern.matcher(content);
        Matcher channelMatcher = channelPattern.matcher(content);
        Matcher repeatedMatcher = repeatedPattern.matcher(content);
        while(plainUserMatcher.find()) {
            String mention = plainUserMatcher.group();
            String id = mention.substring(2, mention.length() - 1);
            User user = event.getGuild().getMemberById(id).getUser();
            content = content.replace(mention, user.getName() + "??????");
        }
        while(nicknamedUserMatcher.find()) {
            String mention = nicknamedUserMatcher.group();
            String id = mention.substring(3, mention.length() - 1);
            User user = event.getGuild().getMemberById(id).getUser();
            content = content.replace(mention, user.getName() + "??????");
        }
        while(roleMatcher.find()) {
            String mention = roleMatcher.group();
            String id = mention.substring(2, mention.length() - 1);
            Role role = event.getGuild().getRoleById(id);
            content = content.replace(mention, "?????????" + role.getName() + " ???????????????");
        }
        while(channelMatcher.find()) {
            String mention = channelMatcher.group();
            String id = mention.substring(2, mention.length() - 1);
            TextChannel tmpChannel = event.getGuild().getTextChannelById(id);
            content = content.replace(mention, "??????????????????" + tmpChannel.getName());
        }
        while(repeatedMatcher.find()) {
            String repString = repeatedMatcher.group();
            content = content.replace(repString, "");
        }
        return content;
    }
}
