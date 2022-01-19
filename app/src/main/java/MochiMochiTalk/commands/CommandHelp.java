package MochiMochiTalk.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
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
        if(author.isBot()) {
            return;
        }
        if(content.equalsIgnoreCase(App.prefix + "help")) {
            logger.info("Sending help message.");
            channel.sendMessage("```" +
                    "使い方は次の通りです…！プロデューサーさん！\n" +
                    App.prefix + "connect - コマンドを打った本人が入っているボイスチャンネルにわたしを入室させます\n" +
                    App.prefix + "disconnect - わたしをボイスチャンネルから退出させます\n" +
                    App.prefix + "ping - （開発者向け）わたしのPingを測定します\n" +
                    App.prefix + "report 【内容】 - もしわたしが変な動き方（テキストを全然喋ってくれない）をしたときに、わたしを作ってくれた人へそのことを伝えておきます\n" +
                    App.prefix + "prefix 【新しいprefix】コマンドの接頭辞を変更します。\n" + 
                    App.prefix + "dic 【読み方を変えたい単語】 【読み方】 - その単語の読み方を変更します。\n" +
                    App.prefix + "help - このお助けメッセージを表示します```").queue();
        }
    }
}
