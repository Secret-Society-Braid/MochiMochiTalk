package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import MochiMochiTalk.util.ConcurrencyUtil;
import MochiMochiTalk.util.DiscordServerOperatorUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class CommandReport extends CommandInformation {

  @Override
  public String getCommandName() {
    return "report";
  }

  @Override
  protected String getCommandDescription() {
    return "Bot開発者へ、Botの不具合などを報告できるコマンドです";
  }

  @Override
  protected void setCommandData() {
    if (this.commandData != null) {
      return;
    }
    this.commandData = Commands.slash(
        this.getCommandName(),
        this.getCommandDescription())
      .addOptions(
        new OptionData(
          OptionType.STRING,
          "description",
          "報告したい内容を入力してください")
          .setRequired(true));
  }

  @Override
  public void slashCommandHandler(@NotNull SlashCommandInteractionEvent event) {
    log.info("report command invoked");
    event.deferReply(true).queue();
    InteractionHook hook = event.getHook().setEphemeral(true);

    User author = event.getUser();
    // description won't be null as it is required option
    String description = event.getOption("description", OptionMapping::getAsString);

    // search for the Bot dev user.
    // since RestAction#flatMap will combine the result of the previous operation, we can use it as normal method chain.
    // Note that the interactions that are chained with flatMap won't be sent until the #submit is called.
    event
      .getJDA()
      .retrieveUserById(DiscordServerOperatorUtil.getBotDevUserId())
      .flatMap(User::openPrivateChannel)
      // send message to the dev user
      .flatMap(c -> c.sendMessageEmbeds(buildEmbedMessage(author, description)))
      // send message to the user that acknowledge is completed
      .flatMap(m -> hook.editOriginal(
        "プロデューサーさん、報告ありがとうございます。治るまで時間が掛かるかもしれませんが、私、がんばりますっ…"))
      // execute all the operations above.
      .submit(true)
      // post event handling for error logging.
      .whenCompleteAsync(ConcurrencyUtil::postEventHandling);
  }

  private MessageEmbed buildEmbedMessage(User author, String body) {
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
}
