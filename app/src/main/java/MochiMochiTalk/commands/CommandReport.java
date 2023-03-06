package MochiMochiTalk.commands;

import MochiMochiTalk.util.ConcurrencyUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandReport extends ListenerAdapter {

  private static final String DEV_USER_ID = "399143446939697162";
  private static final Logger log = LoggerFactory.getLogger(CommandReport.class);
  private static final ExecutorService concurrentPool = Executors.newCachedThreadPool(
      new CountingThreadFactory(() -> "MochiMochiTalk", "Report command concurrent processor", true)
  );

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
    log.warn("sending report message...");
    log.warn("description: {}", body);
    log.warn("estimate occurred date: {}", formattedDate);
    log.warn("reported via {}", author);
    return builder.build();
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (!event.getName().equals("report")) {
      return;
    }
    User author = event.getUser();
    String desc = event.getOption("description", OptionMapping::getAsString);
    CompletableFuture<PrivateChannel> devUserChannelFuture = event.getJDA()
        .retrieveUserById(DEV_USER_ID)
        .submit()
        .thenApplyAsync(devUser -> devUser.openPrivateChannel().complete(), concurrentPool);
    CompletableFuture.supplyAsync(
            () -> buildEmbedMessage(author, desc),
            concurrentPool)
        .thenAcceptBothAsync(
            devUserChannelFuture,
            (embed, privateChannel) -> privateChannel.sendMessageEmbeds(embed).complete(),
            concurrentPool)
        .thenRunAsync(
            () -> event.reply("プロデューサーさん、報告ありがとうございます。治るまで時間が掛かるかもしれませんが、私、がんばりますっ…")
                .setEphemeral(true)
                .complete(),
            concurrentPool)
        .whenCompleteAsync(ConcurrencyUtil::postEventHandling, concurrentPool);
  }
}
