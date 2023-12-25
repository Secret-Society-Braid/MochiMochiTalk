package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class SlashCommandRegisteration extends ListenerAdapter {

  private static final List<CommandInformation> commandList = List.of(
    new CommandPing(),
    new CommandDebugMode(),
    new CommandHelp(),
    new CommandReport(),
    new CommandShowLicense(),
    new CommandShutdown(),
    new CommandSong(),
    new CommandWhatsNew(),
    new CommandDictionary()
  );

  private static final String LOG_FORMAT = "registering {}...";

  private static final CommandData changePrefixCommand;

  private static final CommandData vcCommand;

  static {

    changePrefixCommand = Commands.slash("prefix", "Prefixを変更します")
        .addOptions(new OptionData(OptionType.STRING, "new", "新しいPrefixを指定します。")
            .setRequired(true))
        .setGuildOnly(true);

    vcCommand = Commands.slash("vc",
            "Botが現在VCに入室しているかどうか自動で判断し、入っていない場合は入室して読み上げを開始、入っている場合は退出して読み上げの終了処理を行います。")
        .setGuildOnly(true);

  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    log.info("Slash command invoked.");
    event.deferReply().queue();
    commandList
      .parallelStream()
      .filter(c -> c.shouldHandle(event))
      .findFirst()
      .ifPresentOrElse(
        c -> c.slashCommandHandler(event),
        () -> {
          log.warn("Command not found.");
          event
            .getHook()
            .setEphemeral(true)
            .editOriginal("コマンドが見つかりませんでした。")
            .queue();
        }
      );
  }

  @Override
  public void onReady(@NotNull ReadyEvent event) {
    log.info("Attempt to register slash commands.");
    JDA jda = event.getJDA();
    CommandListUpdateAction commands = jda.updateCommands();

    log.info("Submitting command data to Discord...");
    // register slash commands
    commands.addCommands(
      commandList
        .parallelStream()
        .map(CommandInformation::getCommandData)
        .collect(Collectors.toList())
    ).queue(
      suc -> log.info("complete submitting command data."),
      fail -> log.error("error while submitting command data to Discord.", fail));
  }
}
