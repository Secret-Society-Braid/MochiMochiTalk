package MochiMochiTalk.commands;

import MochiMochiTalk.App;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandReport extends ListenerAdapter {

  private static final String DEV_USER_ID = "399143446939697162";
  private static final Logger logger = LoggerFactory.getLogger(CommandReport.class);

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    User author = event.getAuthor();

//    early return when author is bot
    if (author.isBot()) {
      return;
    }
    String contentRaw = event.getMessage().getContentRaw();

//    early return when contentRaw is not equal to prefix + "report"
    if (!contentRaw.equals(App.getStaticPrefix() + "report")) {
      return;
    }
    String[] args = contentRaw.split(" ");
    MessageChannel channel = event.getChannel();

//    early return when args length is not equal to 2
    if (args.length != 2) {
      logger.info("invalid args length");
      channel.sendMessage("!!report <伝えたい内容> と入力してください").queue();
      return;
    }

    String sendBody = args[1];
//    asynchronous user retrieve to avoid null reference of dev user
//    merge this async user retrieve with the message send
    CompletableFuture<User> devUserFuture = event.getJDA().retrieveUserById(DEV_USER_ID).submit();

    CompletableFuture.supplyAsync(() -> buildEmbedMessage(author, sendBody))
        .thenAcceptBothAsync(devUserFuture.thenApply(dev -> dev.openPrivateChannel().complete()),
            (embed, privateChannel) -> privateChannel.sendMessageEmbeds(embed).queue());
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (!event.getName().equals("report")) {
      return;
    }
    User author = event.getUser();
    User dev = Objects.requireNonNull(event.getJDA().getUserById(DEV_USER_ID));
    String desc = event.getOption("description", OptionMapping::getAsString);
    String formattedDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    logger.info("sending report message...");
    logger.info("description: {}", desc);
    logger.info("estimate occurred date: {}", formattedDate);
    logger.info("reported via {}", author);
    dev.openPrivateChannel()
        .queue(channel -> channel.sendMessageFormat("プロデューサーさんからおかしな挙動の報告がありました。\n"
                + "送信したプロデューサーさん: ** %s **\n"
                + "内容: ``` %s ```\n"
                + "が報告されました。\n"
                + "障害発生予想時刻: %s \n",
            author.getName(), desc, formattedDate).queue());
    event.replyFormat("%s プロデューサーさん、報告ありがとうございます。治るまで時間が掛かるかもしれませんが、私、がんばりますっ…",
        author.getName()).setEphemeral(true).queue();
  }

  private static MessageEmbed buildEmbedMessage(User author, String body) {
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle("不正常挙動報告");
    builder.setDescription("プロデューサーさんからおかしな挙動の報告がありました。");
    builder.addField("送信したプロデューサーさん", author.getAsMention(), false);
    builder.addField("内容", body, false);
    String formattedDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    builder.addField("障害発生予想時刻", formattedDate, false);
    builder.setFooter("MochiMochiTalk");
    builder.setColor(0x00ff00);
    logger.info("sending report message...");
    logger.info("description: {}", body);
    logger.info("estimate occurred date: {}", formattedDate);
    logger.info("reported via {}", author);
    return builder.build();
  }
}
