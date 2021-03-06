package MochiMochiTalk.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;

public class CommandReport extends ListenerAdapter {
    
    private Logger logger = LoggerFactory.getLogger(CommandReport.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = event.getAuthor();
        User dev = event.getJDA().getUserById("399143446939697162");
        MessageChannel channel = event.getChannel();
        if(author.isBot()) {
            return;
        }
        if(content.startsWith(App.prefix + "report ")) {
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
        } else if (content.equals(App.prefix + "report")) {
            logger.warn("sendBody parameter is missing.");
            channel.sendMessage("!!reportの後に半角のスペースを入れて、その後に伝えたい内容を入れてください。").queue();
        }
    }
}
