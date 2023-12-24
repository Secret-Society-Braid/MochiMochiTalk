package MochiMochiTalk.voice;


import MochiMochiTalk.App;
import MochiMochiTalk.commands.CommandDictionary;
import MochiMochiTalk.lib.AllowedVCRead;
import MochiMochiTalk.util.ConcurrencyUtil;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceEventListener extends ListenerAdapter {

  private static final ThreadFactory THREAD_FACTORY = ConcurrencyUtil.createThreadFactory(
      "AFK-Checker");
  private final Logger logger = LoggerFactory.getLogger(VoiceEventListener.class);
  private ScheduledExecutorService schedulerService;

  private MessageChannel channel = null;
  private AudioManager audioManager;
  private final DeprecatedTTSEngine ttsEngine = new DeprecatedTTSEngine();
  private boolean flag = false;
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
    if (rawContent.equalsIgnoreCase(App.getStaticPrefix() + "connect")
        || rawContent.equalsIgnoreCase(App.getStaticPrefix() + "c")) {
      logger.debug("connect command fired");
      if (!checkVCAllowed(event)) {
        logger.warn("VC is not allowed this server. : {}", message.getGuild().getName());
        event.getChannel()
            .sendMessage("使用しているAPIの関係上、むつコード様以外のサーバーでは読み上げ機能は使用できません。ごめんなさい。")
            .queue();
        return;
      }
      logger.info("Connecting to voice channel.");
      onConnectCommand(event);
    }

    if (rawContent.equalsIgnoreCase(App.getStaticPrefix() + "disconnect")
        || rawContent.equalsIgnoreCase(App.getStaticPrefix() + "dc")) {
      logger.info("Disconnecting from voice channel.");
      onDisconnectCommand(event);
    }

    String[] split = content.split("\n");

    boolean isEscaped = false;

    if (content.length() > 40) {
      logger.info("Received long message.");
      logger.info("Escaped: {}", content);
      logger.info("target messageID: {}", message.getId());
      return;
    }

    for (String str : split) {
      isEscaped = escapeProgression(str);
      if (isEscaped) {
        break;
      }
    }

    if (isEscaped) {
      logger.info("Escaped: {}", content);
      logger.info("target messageID: {}", message.getId());
      return;
    }

    if (flag && !content.startsWith(App.getStaticPrefix())) {
      String immutableContent = content;
      CompletableFuture<Optional<String>> dictFetchFuture = CompletableFuture.supplyAsync(() -> {
        CommandDictionary instance = CommandDictionary.getInstance();
        return instance.getDict(immutableContent);
      });
      if (!event.getChannel().equals(channel)) {
        logger.info("Message is not in the same channel as the voice channel.");
        return;
      }
      logger.info("Analyzing message: {}", content);
      logger.info("Channel: {}", channel.getName());
      logger.info("Author: {}", author.getName());
      logger.info("Guild: {}", event.getGuild().getName());

      Optional<String> dictNullable = dictFetchFuture.join();
      content = dictNullable.orElse(content);
      logger.debug("current content: {}", content);

      try {
        ttsEngine.say(content);
      } catch (IOException e) {
        logger.error("IO exception while handling tts.", e);
      } catch (InterruptedException iException) {
        logger.error("The tts handle thread has been interrupted by someone.", iException);
        Thread.currentThread().interrupt();
      }
    }
  }

  private void onConnectCommand(MessageReceivedEvent event) {
    VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
    if (voiceChannel == null) {
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
    builder.addField("読まれないものの一覧",
        "・文字数が40文字以上の文章\n\n・サーバーオリジナル絵文字\n\n・コードブロックを含む文章\n\n・URLを含む文章", false);
    channel.sendMessageEmbeds(builder.build()).queue();
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
    if (content.matches(".*<:[A-Za-z].+\\d*>*")) {
      logger.info("Received emoji.");
      return true;
    }

    if (content.length() > 40) {
      logger.info("Received long message.");
      return true;
    }

    if (content.startsWith("```")) {
      logger.info("Received code block.");
      return true;
    }

    if (content.matches(
        "\\b.*(http?|https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
      logger.info("Received URL.");
      return true;
    }
    return false;
  }

  private void checkVoiceChannel() {
    if (audioManager.isConnected() && audioManager.getConnectedChannel().getMembers().size() == 1) {
      audioManager.closeAudioConnection();
      channel.sendMessage("誰もいないので私も戻りますね。お疲れ様でした。").queue();
      channel = null;
      flag = false;
      logger.info("Disconnected from voice channel caused by AFK.");
    }
  }

  private boolean checkVCAllowed(MessageReceivedEvent event) {
    if (allowed.isEmpty()) {
      return false;
    }
    for (String str : allowed) {
      if (str.equals(event.getGuild().getId())) {
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
    Pattern repeatedPattern = Pattern.compile("(!|w|！|ｗ)");
    Matcher plainUserMatcher = plainUserPattern.matcher(content);
    Matcher nicknamedUserMatcher = nicknamedUserPattern.matcher(content);
    Matcher roleMatcher = rolePattern.matcher(content);
    Matcher channelMatcher = channelPattern.matcher(content);
    Matcher repeatedMatcher = repeatedPattern.matcher(content);
    while (plainUserMatcher.find()) {
      String mention = plainUserMatcher.group();
      String id = mention.substring(2, mention.length() - 1);
      User user = event.getGuild().getMemberById(id).getUser();
      content = content.replace(mention, user.getName() + "さん");
    }
    while (nicknamedUserMatcher.find()) {
      String mention = nicknamedUserMatcher.group();
      String id = mention.substring(3, mention.length() - 1);
      User user = event.getGuild().getMemberById(id).getUser();
      content = content.replace(mention, user.getName() + "さん");
    }
    while (roleMatcher.find()) {
      String mention = roleMatcher.group();
      String id = mention.substring(2, mention.length() - 1);
      Role role = event.getGuild().getRoleById(id);
      content = content.replace(mention, "役職、" + role.getName() + " のみなさん");
    }
    while (channelMatcher.find()) {
      String mention = channelMatcher.group();
      String id = mention.substring(2, mention.length() - 1);
      TextChannel tmpChannel = event.getGuild().getTextChannelById(id);
      content = content.replace(mention, "チャンネル、" + tmpChannel.getName());
    }
    while (repeatedMatcher.find()) {
      String repString = repeatedMatcher.group();
      content = content.replace(repString, "");
    }
    return content;
  }

  private void onConnectWithSlash(SlashCommandInteractionEvent event) {
    VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
    if (voiceChannel == null) {
      event.reply("まだボイスチャンネルに入っていないようです。ボイスチャンネルに入ってからやり直してみてください。").setEphemeral(true).queue();
      logger.warn("User is not in a voice channel.");
      return;
    }
    audioManager.setSendingHandler(ttsEngine);
    audioManager.openAudioConnection(voiceChannel);
    channel = event.getChannel();
    flag = true;
    event.reply("準備が出来ました！いつでもお喋りできます！").queue();
    EmbedBuilder embed = new EmbedBuilder();
    embed.setTitle("テキスト読み上げBot「聖ちゃんの聖歌隊」");
    embed.setDescription("現在、以下の条件に当てはまる文章は読まれません。注意してください。");
    embed.addField("読まれないものの一覧",
        "・文字数が40文字以上の文章\n\n・サーバーオリジナル絵文字\n\n・コードブロックを含む文章\n\n・URLを含む文章", false);
    event.getChannel().sendMessageEmbeds(embed.build()).queue();
    schedulerService = Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
    schedulerService.scheduleWithFixedDelay(this::checkVoiceChannel, 1, 5, TimeUnit.SECONDS);
    logger.info("Connected to the voice channel.");
  }

  private void onDisconnectWithSlash(SlashCommandInteractionEvent event) {
    schedulerService.shutdownNow();
    audioManager.closeAudioConnection();
    channel = null;
    flag = false;
    event.reply("読み上げを終わります。お疲れ様でした……").queue();
    logger.info("Disconnected from the voice channel");
  }

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    if (event.getName().equals("vc")) {
      if (audioManager == null || !(audioManager.isConnected())) {
        logger.info("User wants Bot to be connected. connecting...");
        this.audioManager = event.getGuild().getAudioManager();
        onConnectWithSlash(event);
        return;
      }
      logger.info("Bot is currently connected to the voice channel. disconnecting");
      onDisconnectWithSlash(event);
    }
  }
}
