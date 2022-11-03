package MochiMochiTalk.commands;

import java.awt.Color;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class CommandHelp extends ListenerAdapter {

    private Logger logger = LoggerFactory.getLogger(CommandHelp.class);

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = event.getAuthor();
        MessageChannel channel = event.getChannel();

        if (author.isBot()) {
            return;
        }

        if (content.equalsIgnoreCase(App.getStaticPrefix() + "help advanced")) {
            logger.info("sending help for advanced users");
            channel.sendMessageEmbeds(buildAdvanced()).queue();
        } else if(content.equalsIgnoreCase(App.getStaticPrefix() + "help")) {
            logger.info("sending help for general users");
            channel.sendMessageEmbeds(buildNormal()).queue();
        } else {
            /* do nothing */
        }
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if(event.getName().equals("help")) {
            logger.info("help slash command invoked.");
            String cat = event.getOption("category", OptionMapping::getAsString);
            // it will be non-null value as OptionMapping implementation
            MessageEmbed embed = Objects.requireNonNull(cat).equals("advanced") ? buildAdvanced() : buildNormal();
            event.replyEmbeds(embed).queue();
        }
    }

    @Nonnull
    private MessageEmbed buildNormal() {
        logger.info("Constructing help message for general users.");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Help");
        builder.setColor(Color.YELLOW);
        builder.setDescription("使い方は次の通りです…！プロデューサーさん！");
        builder.addField(App.getStaticPrefix() + "help", "このお助けメッセージを表示します", false);
        builder.addField(App.getStaticPrefix() + "connect もしくは " + App.getStaticPrefix() + "c", "コマンドを打った本人が入っているボイスチャンネルにわたしを入室させます", false);
        builder.addField(App.getStaticPrefix() + "disconnect もしくは " + App.getStaticPrefix() + "dc", "わたしをボイスチャンネルから退出させます", false);
        builder.addField(App.getStaticPrefix() + "report 【内容】", "もしわたしが変な動き方（テキストを全然喋ってくれないなど）をしたときに、わたしを作ってくれた人へそのことを伝えておきます", false);
        builder.addField(App.getStaticPrefix() + "dic 【読み方を変えたい単語】 【読み方】", "その単語の読み方を変更します", false);
        builder.addField(App.getStaticPrefix() + "song 【検索キーワードもしくは ふじわらはじめ楽曲DB内部管理ID】", "ふじわらはじめ楽曲DB様のAPIから曲情報を検索、取得します。(試験実装中)", false);
        builder.addField(App.getStaticPrefix() + "whatsnew", "一個前のバージョン(Github Release基準)からの変更点を表示します", false);
        return builder.build();
    }

    @Nonnull
    private MessageEmbed buildAdvanced() {
        logger.info("Constructing help message for advanced users.");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Help");
        builder.setColor(Color.YELLOW);
        builder.setDescription("使い方は次の通りです…！プロデューサーさん！");
        builder.addField(App.getStaticPrefix() + "help", "このお助けメッセージを表示します", false);
        builder.addField(App.getStaticPrefix() + "ping", "(開発者向け)わたしのpingを表示します", false);
        builder.addField(App.getStaticPrefix() + "connect もしくは " + App.getStaticPrefix() + "c", "コマンドを打った本人が入っているボイスチャンネルにわたしを入室させます", false);
        builder.addField(App.getStaticPrefix() + "disconnect もしくは " + App.getStaticPrefix() + "dc", "わたしをボイスチャンネルから退出させます", false);
        builder.addField(App.getStaticPrefix() + "report 【内容】", "もしわたしが変な動き方（テキストを全然喋ってくれないなど）をしたときに、わたしを作ってくれた人へそのことを伝えておきます", false);
        builder.addField(App.getStaticPrefix() + "prefix 【新しいprefix】", "コマンドの接頭辞を変更します", false);
        builder.addField(App.getStaticPrefix() + "dic 【読み方を変えたい単語】 【読み方】", "その単語の読み方を変更します", false);
        builder.addField(App.getStaticPrefix() + "whatsnew", "一個前のバージョン(Github Release基準)からの変更点を表示します", false);
        builder.addField(App.getStaticPrefix() + "song 【検索キーワードもしくは ふじわらはじめ楽曲DB内部管理ID】", "ふじわらはじめ楽曲DB様のAPIから曲情報を検索、取得します。(試験実装中)", false);
        builder.addField(App.getStaticPrefix() + "shutdown", "(むつコード 秘密結社幹部ロール付与者のみ使用可能) Botをシャットダウンし、オフライン状態へ移行します。", false);
        return builder.build();
    }

    
}
