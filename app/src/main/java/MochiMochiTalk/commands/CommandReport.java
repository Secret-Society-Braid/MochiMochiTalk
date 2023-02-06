package MochiMochiTalk.commands;

import MochiMochiTalk.App;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Message;
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
  private final Logger logger = LoggerFactory.getLogger(CommandReport.class);

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    Message message = event.getMessage();
    String content = message.getContentRaw();
    User author = event.getAuthor();
    User dev = Objects.requireNonNull(event.getJDA().getUserById(DEV_USER_ID));
    MessageChannel channel = event.getChannel();
    if (author.isBot()) {
      return;
    }
    if (content.startsWith(App.getStaticPrefix() + "report ")) {
      String sendBody = content.substring(9);
      logger.info("Sending report message.");
      String formattedDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
      logger.info("Date: {}", formattedDate);
      dev.openPrivateChannel().queue(pChannel -> {
        pChannel.sendMessageFormat("プロデューサーさんからおかしな挙動の報告がありました。\n"
                + "送信したプロデューサーさん：** %s **さん\n"
                + "送信内容：\n``` %s ```\n"
                + "が報告されました。\n"
                + "障害発生予想時刻： %s \n"
            , author.getName(), sendBody, formattedDate).queue();
      });
      channel.sendMessage("報告ありがとうございます。治るまで時間が掛かるかもしれないので、気長にお待ちください by 中の人").queue();
    } else if (content.equals(App.getStaticPrefix() + "report")) {
      logger.warn("sendBody parameter is missing.");
      channel.sendMessage("!!reportの後に半角のスペースを入れて、その後に伝えたい内容を入れてください。").queue();
    } else {
      /* do nothing */
    }
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("report")) {
      User author = event.getUser();
      User dev = Objects.requireNonNull(event.getJDA().getUserById(DEV_USER_ID));
      String desc = event.getOption("description", OptionMapping::getAsString);
      String formattedDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
      logger.info("sending report message...");
      logger.info("description: {}", desc);
      logger.info("estimate occured date: {}", formattedDate);
      logger.info("reported via {}", author);
      dev.openPrivateChannel().queue(channel -> {
        channel.sendMessageFormat("プロデューサーさんからおかしな挙動の報告がありました。\n"
                + "送信したプロデューサーさん: ** %s **\n"
                + "内容: ``` %s ```\n"
                + "が報告されました。\n"
                + "障害発生予想時刻: %s \n",
            author.getName(), desc, formattedDate).queue();
      });
      event.replyFormat("%s プロデューサーさん、報告ありがとうございます。治るまで時間が掛かるかもしれませんが、私、がんばりますっ…",
          author.getName()).setEphemeral(true).queue();
    }
  }
}
