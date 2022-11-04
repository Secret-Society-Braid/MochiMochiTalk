package MochiMochiTalk.commands;

import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

@Slf4j
public class SlashCommandRegisteration implements EventListener {


    private static final String LOG_FORMAT = "registering {}...";
    
    private static final CommandData changePrefixCommand;
    private static final CommandData debugModeCommand;
    private static final CommandData dictionaryCommand;
    private static final CommandData helpCommand;
    private static final CommandData pingCommand;
    private static final CommandData reportCommand;
    private static final CommandData shutdownCommand;

    private static final CommandData songCommand;
    private static final CommandData whatsnewCommand;
    private static final CommandData vcCommand;
    private static final CommandData showLicenseCommand;
    
    static  {
        log.debug("Registering commands...");
        log.debug("--------------------------------");
        log.debug(LOG_FORMAT, "changePrefix");

        changePrefixCommand = Commands.slash("prefix", "Prefixを変更します")
            .addOptions(new OptionData(OptionType.STRING, "new", "新しいPrefixを指定します。")
                .setRequired(true))
            .setGuildOnly(true);

        log.debug("complete.");
        log.debug(LOG_FORMAT, "debugModeCommand");

        // debugMode command

        debugModeCommand = Commands.slash("debugmode", "Botの開発を容易にするデバッグモードを切り替えます")
            .addSubcommands(
                new SubcommandData("on", "デバッグモードをONにします。"),
                new SubcommandData("off", "デバッグモードをOFFにします")
            );

        log.debug("complete");
        log.debug(LOG_FORMAT, "dictionaryCommand");
        
        dictionaryCommand = Commands.slash("dict", "Botの単語変換辞書を操作します。")
            .addOptions(new OptionData(OptionType.STRING, "phrase", "変換元の単語を指定します。")
                .setRequired(true))
            .addOptions(new OptionData(OptionType.STRING, "dict", "変換先の単語を指定します。")
                .setRequired(true))
            .setGuildOnly(true);

        log.debug("complete");
        log.debug(LOG_FORMAT, "helpCommand");

        helpCommand = Commands.slash("help", "Botのヘルプを表示します。")
            .addOptions(new OptionData(OptionType.STRING, "category", "ヘルプ内容を指定します。"));

        log.debug("complete");
        log.debug(LOG_FORMAT, "pingCommand");

        pingCommand = Commands.slash("ping", "BotのPingを表示します");

        log.debug("complete");
        log.debug(LOG_FORMAT, "reportCommand");

        reportCommand = Commands.slash("report", "Bot開発者へ、Botの不具合などを報告できるコマンドです")
            .addOptions(new OptionData(OptionType.STRING, "description", "報告したい内容を入力してください")
                .setRequired(true));
        
        log.debug("complete");
        log.debug(LOG_FORMAT, "shutdownCommand");
        
        shutdownCommand = Commands.slash("shutdown", "Botを強制的に停止させます。");

        log.debug("complete");
        log.debug(LOG_FORMAT, "song with keyword command");
        songCommand = Commands.slash("song", "デレステの楽曲情報を検索します (Powered by ふじわらはじめ楽曲DB)")
            .addSubcommands(
                new SubcommandData("keyword", "指定したキーワードを基に、楽曲を検索します。")
                    .addOptions(new OptionData(OptionType.STRING, "keyword", "検索する文字列を入力してください")
                        .setRequired(true)),
                new SubcommandData("id", "指定された ふじわらはじめ楽曲DB管理ID を使用して詳細な情報を表示します。")
                    .addOptions(new OptionData(OptionType.STRING, "id", "管理IDを入力します。")
                        .setRequired(true)))
            .setGuildOnly(true);
            

        log.debug("complete");
        log.debug(LOG_FORMAT, "whatsnew command");

        whatsnewCommand = Commands.slash("whatsnew", "Botに最近加えられた変更を表示します");

        log.debug("complete.");
        log.debug(LOG_FORMAT, "vc command");

        vcCommand = Commands.slash("vc", "Botが現在VCに入室しているかどうか自動で判断し、入っていない場合は入室して読み上げを開始、入っている場合は退出して読み上げの終了処理を行います。")
            .setGuildOnly(true);

            log.debug("complete.");
        log.debug(LOG_FORMAT, "showLicense command");

        showLicenseCommand = Commands.slash("showLicense", "Botが使用しているライブラリのライセンス情報を出力します。");

        log.debug("complete.");

        log.debug("-----------------------------------");
        log.debug("Slash command registeration complete.");
    }
    
    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if(event instanceof ReadyEvent) {
            log.info("Attempt to register slash commands.");
            JDA jda = event.getJDA();
            CommandListUpdateAction commands = jda.updateCommands();

            // register slash commands
            commands.addCommands(
                changePrefixCommand,
                debugModeCommand,
                dictionaryCommand,
                helpCommand,
                pingCommand,
                reportCommand,
                shutdownCommand,
                songCommand,
//                songKeywordCommand,
//                songIdCommand,
                whatsnewCommand,
                vcCommand,
                showLicenseCommand);
            
            log.info("Submitting command data to Discord...");
            commands.queue(suc -> log.info("complete submitting command data."),
                fail -> log.error("error while submitting command data to Discord.", fail));

        }
    }
    
}
