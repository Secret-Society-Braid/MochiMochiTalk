package MochiMochiTalk.commands;

import java.awt.Color;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandHelp extends ListenerAdapter {

    private Logger logger = LoggerFactory.getLogger(CommandHelp.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = event.getAuthor();
        MessageChannel channel = event.getChannel();

        if (author.isBot()) {
            return;
        }

        if (content.equalsIgnoreCase(App.prefix + "help advanced")) {
            logger.info("sending help for advanced users");
            channel.sendMessageEmbeds(buildAdvanced()).queue();
        } else if(content.equalsIgnoreCase(App.prefix + "help")) {
            logger.info("sending help for general users");
            channel.sendMessageEmbeds(buildNormal()).queue();
        } else {
            logger.warn("there is no option for {}", content);
            channel.sendMessage("無効な引数です。").queue();
        }
    }

    @Nonnull
    private MessageEmbed buildNormal() {
        logger.info("Constructing help message for general users.");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Help");
        builder.setColor(Color.YELLOW);
        builder.setDescription("使い方は次の通りです…！プロデューサーさん！");
        builder.addField(App.prefix + "help", "このお助けメッセージを表示します", false);
        builder.addField(App.prefix + "connect もしくは " + App.prefix + "c", "コマンドを打った本人が入っているボイスチャンネルにわたしを入室させます", false);
        builder.addField(App.prefix + "disconnect もしくは " + App.prefix + "dc", "わたしをボイスチャンネルから退出させます", false);
        builder.addField(App.prefix + "report 【内容】", "もしわたしが変な動き方（テキストを全然喋ってくれないなど）をしたときに、わたしを作ってくれた人へそのことを伝えておきます", false);
        builder.addField(App.prefix + "dic 【読み方を変えたい単語】 【読み方】", "その単語の読み方を変更します", false);
        builder.addField(App.prefix + "song 【検索キーワードもしくは ふじわらはじめ楽曲DB内部管理ID】", "ふじわらはじめ楽曲DB様のAPIから曲情報を検索、取得します。(試験実装中)", false);
        builder.addField(App.prefix + "whatsnew", "一個前のバージョン(Github Release基準)からの変更点を表示します", false);
        return builder.build();
    }

    @Nonnull
    private MessageEmbed buildAdvanced() {
        logger.info("Constructing help message for advanced users.");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Help");
        builder.setColor(Color.YELLOW);
        builder.setDescription("使い方は次の通りです…！プロデューサーさん！");
        builder.addField(App.prefix + "help", "このお助けメッセージを表示します", false);
        builder.addField(App.prefix + "ping", "(開発者向け)わたしのpingを表示します", false);
        builder.addField(App.prefix + "connect もしくは " + App.prefix + "c", "コマンドを打った本人が入っているボイスチャンネルにわたしを入室させます", false);
        builder.addField(App.prefix + "disconnect もしくは " + App.prefix + "dc", "わたしをボイスチャンネルから退出させます", false);
        builder.addField(App.prefix + "report 【内容】", "もしわたしが変な動き方（テキストを全然喋ってくれないなど）をしたときに、わたしを作ってくれた人へそのことを伝えておきます", false);
        builder.addField(App.prefix + "prefix 【新しいprefix】", "コマンドの接頭辞を変更します", false);
        builder.addField(App.prefix + "dic 【読み方を変えたい単語】 【読み方】", "その単語の読み方を変更します", false);
        builder.addField(App.prefix + "whatsnew", "一個前のバージョン(Github Release基準)からの変更点を表示します", false);
        builder.addField(App.prefix + "song 【検索キーワードもしくは ふじわらはじめ楽曲DB内部管理ID】", "ふじわらはじめ楽曲DB様のAPIから曲情報を検索、取得します。(試験実装中)", false);
        builder.addField(App.prefix + "shutdown", "(むつコード 秘密結社幹部ロール付与者のみ使用可能) Botをシャットダウンし、オフライン状態へ移行します。", false);
        return builder.build();
    }
}
