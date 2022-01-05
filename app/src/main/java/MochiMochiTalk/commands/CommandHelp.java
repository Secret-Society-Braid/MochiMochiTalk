package MochiMochiTalk.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if(content.equalsIgnoreCase("!!help")) {
            logger.info("Sending help message.");
            channel.sendMessage("```" +
                    "使い方は次の通りです…！プロデューサーさん！\n" +
                    "!!connect - コマンドを打った本人が入っているボイスチャンネルにわたしを入室させます\n" +
                    "!!disconnect - わたしをボイスチャンネルから退出させます\n" +
                    "!!ping - （開発者向け）わたしのPingを測定します\n" +
                    "!!report 【内容】 - もしわたしが変な動き方（テキストを全然喋ってくれない）をしたときに、わたしを作ってくれた人へそのことを伝えておきます\n" +
                    "!!help - このお助けメッセージを表示します```").queue();
        }
    }
}
