package MochiMochiTalk.voice.nvoice;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import MochiMochiTalk.App;
import MochiMochiTalk.commands.CommandDictionary;
import MochiMochiTalk.commands.CommandWhatsNew;
import MochiMochiTalk.lib.AllowedVCRead;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

@Slf4j
public class EventListenerForTTS extends ListenerAdapter {

    private static final CountingThreadFactory factory = new CountingThreadFactory(() -> "MochiMochiTalk", "AFK-Checker");

    private MessageChannel boundedChannel;
    private AudioManager audioManager;
    private GoogleTTSEngine engine;
    private boolean flag;
    
    private ScheduledExecutorService schedulerService;
    private static final List<String> allowed; 
    
    static {
        allowed = readWhiteList();
    }

    public EventListenerForTTS() {
        this.engine = new GoogleTTSEngine();
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        String content = replaceMentions(event);
        String rawContent = message.getContentRaw();
        audioManager = event.getGuild().getAudioManager();
        
        // ignore messages from bots
        if(author.isBot())
            return;
        
        if(rawContent.equalsIgnoreCase(App.getStaticPrefix() + "connect") || rawContent.equalsIgnoreCase(App.getStaticPrefix() + "c")) {
            if(!checkVCAllowed(event)) {
                log.warn("VC reading is not allowed in this server. : {}", event.getGuild());
                event.getChannel().sendMessage("使用しているAPIの都合上、むつコード様以外のサーバーでは読み上げを行いません。申し訳ございません。");
                return;
            }
            log.info("Connecting to voice channel");
            onConnectCommand(event);
        }

        if(rawContent.equalsIgnoreCase(App.getStaticPrefix() + "disconnect") || rawContent.equalsIgnoreCase(App.getStaticPrefix() + "dc")) {
            log.info("Disconnecting from voice channel.");
            onDisconnectCommand(event);
        }

        String[] split = content.split("\n");

        boolean shouldEscaped = doesNeedEscape(rawContent);

        for(int i = 0; i < split.length; i++) {
            if(shouldEscaped)
                break;
            shouldEscaped = doesNeedEscape(split[i]);
        }

        if(shouldEscaped) {
            log.info("Escape message. : {}", content);
            log.info("target message id : {}", message.getId());
            return;
        }

        if(flag && !rawContent.startsWith(App.getStaticPrefix())) {
            String immutableContent = content;
            CompletableFuture<Optional<String>> dictFetchFuture = CompletableFuture.supplyAsync(() -> {
                CommandDictionary instance = CommandDictionary.getInstance();
                return instance.getDict(immutableContent);
            });
            if(!event.getChannel().equals(boundedChannel)) {
                log.info("Message is not in the same channel as the voice channel.");
                return;
            }
            log.info("Analyzing message: {}", content);
            log.info("Channel: {}", boundedChannel.getName());
            log.info("Author: {}", author.getName());
            log.info("Guild: {}", event.getGuild().getName());

            Optional<String> dictNullable = dictFetchFuture.join();
            content = dictNullable.orElse(content);
            log.debug("current content: {}", content);

            try {
                    engine.say(content);
            } catch (InterruptedException ignore) {
                log.error("Cannot handle tts because another method(s) interrupt this thread:", ignore);
                log.warn("This event thread will continue working. if disconnecting still consists, it will need more investigating.");
            } catch (IOException ioe) {
                log.error("Encountered IO Error while handshaking tts engine: {}", ioe);
            }
        }
    }

    @Override 
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if(event.getName().equals("vc")) {
            if(audioManager == null || !(audioManager.isConnected())) {
                log.info("User wants Bot to be connected. connecting...");
                Guild requested = Objects.requireNonNull(event.getGuild()); // we don't need to be care about null access since we expect this command executed in only the guild.
                log.info("Requested guild: {}", requested);
                this.audioManager = requested.getAudioManager();
                onConnectWithSlash(event);
                return;
            }
            log.info("Bot is currently connected to the voice channel. disconnecting");
                onDisconnectWithSlash(event);
        }
    }

    private static List<String> readWhiteList() {
        return new AllowedVCRead().read();
    }

    private boolean doesNeedEscape(String content) {
        boolean res = false;
        // checks whether content contains mention or not
        if(content.matches(".*<:[A-Za-z].+\\d*>*"))
            res = true;
        // checks whether content is shorter than 40 characters.
        if(content.length() > 40)
            res = true;
        // checks whether contant is surrounded by quotes.
        if(content.startsWith("`") && content.endsWith("`"))
            res = true;
        // checks whether content is surrounded by code blocks
        if(content.startsWith("```") && content.endsWith("```"))
            res = true;
        // checks whether content contains 
        if(content.matches("\\b.*(http?|https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"))
            res = true;
        return res;
    }

    private static String replaceMentions(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        Pattern plainUserPattern = Pattern.compile("<@[0-9].*>");
        Pattern nicknamedUserPattern = Pattern.compile("<@![0-9].*>");
        Pattern rolePattern = Pattern.compile("<&[0-9].*>");
        Pattern channelPattern = Pattern.compile("<#[0-9].*>");
        Pattern repeatedPattern = Pattern.compile("(!|w|！|ｗ)");
        Matcher plainUserMatcher = plainUserPattern.matcher(content);
        Matcher nicknamedUserMatcher = nicknamedUserPattern.matcher(content);
        Matcher roleMatcher = rolePattern.matcher(content);
        Matcher channelMatcher = channelPattern.matcher(content);
        Matcher repeatedMatcher = repeatedPattern.matcher(content);
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
        while(repeatedMatcher.find()) {
            String repString = repeatedMatcher.group();
            content = content.replace(repString, "");
        }
        return content;
    }

    private boolean checkVCAllowed(MessageReceivedEvent event) {
        return allowed.contains(event.getGuild().getId());
    }

    private void onConnectCommand(MessageReceivedEvent event) {
        VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        if(voiceChannel == null) {
            event.getChannel().sendMessage("まだボイスチャンネルに入っていないようです。ボイスチャンネルに入ってからやり直してみてください。").queue();
            log.warn("User is not in a voice channel.");
            return;
        }
        audioManager.setSendingHandler(engine);
        audioManager.openAudioConnection(voiceChannel);
        boundedChannel = event.getChannel();
        flag = true;
        boundedChannel.sendMessage("準備が出来ました！いつでもお喋りできます！");
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("テキスト読み上げBot「聖ちゃんの聖歌隊」");
        embed.setDescription("現在、以下の条件に当てはまる文章は読まれません。注意してください。");
        embed.addField("読まれないものの一覧", "・文字数が40文字以上の文章\n\n・サーバーオリジナル絵文字\n\n・コードブロックを含む文章\n\n・URLを含む文章", false);
        boundedChannel.sendMessageEmbeds(embed.build()).queue();
        CommandWhatsNew whatsNew = CommandWhatsNew.getInstance();
        boundedChannel.sendMessageEmbeds(whatsNew.buildMessage()).queue();
        schedulerService = Executors.newSingleThreadScheduledExecutor(factory);
        schedulerService.scheduleWithFixedDelay(this::checkVoiceChannel, 1, 5, TimeUnit.SECONDS);
        log.info("Connected to the voice channel.");
    }

    private void onDisconnectCommand(MessageReceivedEvent event) {
        schedulerService.shutdownNow();
        audioManager.closeAudioConnection();
        boundedChannel = null;
        flag = false;
        event.getChannel().sendMessage("終わりますか？お疲れ様でした…").queue();
        log.info("Disconnected from the voice channel");
    }

    private void checkVoiceChannel() {
        if(audioManager.isConnected() && audioManager.getConnectedChannel().getMembers().size() == 1) {
            audioManager.closeAudioConnection();
            boundedChannel.sendMessage("誰もいないので私も戻りますね。お疲れ様でした…").queue();
            boundedChannel = null;
            flag = false;
            log.info("Disconnected from the voice channel caused by AFK.");
        }
    }

    private void onConnectWithSlash(SlashCommandInteractionEvent event) {
        VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        if(voiceChannel == null) {
            event.reply("まだボイスチャンネルに入っていないようです。ボイスチャンネルに入ってからやり直してみてください。").setEphemeral(true).queue();
            log.warn("User is not in a voice channel.");
            return;
        }
        audioManager.setSendingHandler(engine);
        audioManager.openAudioConnection(voiceChannel);
        boundedChannel = event.getChannel();
        flag = true;
        event.reply("準備が出来ました！いつでもお喋りできます！").queue();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("テキスト読み上げBot「聖ちゃんの聖歌隊」");
        embed.setDescription("現在、以下の条件に当てはまる文章は読まれません。注意してください。");
        embed.addField("読まれないものの一覧", "・文字数が40文字以上の文章\n\n・サーバーオリジナル絵文字\n\n・コードブロックを含む文章\n\n・URLを含む文章", false);
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        CommandWhatsNew whatsNew = CommandWhatsNew.getInstance();
        event.getChannel().sendMessageEmbeds(whatsNew.buildMessage()).queue();
        schedulerService = Executors.newSingleThreadScheduledExecutor(factory);
        schedulerService.scheduleWithFixedDelay(this::checkVoiceChannel, 1, 5, TimeUnit.SECONDS);
        log.info("Connected to the voice channel.");
    }

    private void onDisconnectWithSlash(SlashCommandInteractionEvent event) {
        schedulerService.shutdownNow();
        audioManager.closeAudioConnection();
        boundedChannel = null;
        flag = false;
        event.reply("読み上げを終わります。お疲れ様でした……").queue();
        log.info("Disconnected from the voice channel");
    } 
}
