package MochiMochiTalk.commands;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandWhatsNew extends ListenerAdapter {

    // フィールド作成
    private Logger logger = LoggerFactory.getLogger(CommandWhatsNew.class);
    private Map<String, String> description = new HashMap<>();

    private static final String DESCRIPTION_FILE_NAME = "description.json";
    private static Random random = new Random();

    protected static final String[] TITLE_MEME = {
        "What's up, world?",
        "What's new?",
        "It's a beautiful day, isn't it?",
        "new day, new world.",
        "follow the new world.",
        "Blooming, isn't it?",
        "It's high time to adventure.",
        "To be, or not to be, that is the question.",
        "I'm a newbie, I'm a newbie, you are boobie.",
        "Also try to use DelesteRandomSelector!",
        "ブルームジャーニーしか勝たん",
        "お前もむつみPにならないか？",
        "おもちもちもち望月聖ちゃん",
        "java.lang.NullPointerException : You got BAMBOOZLED!",
        "このタイトルは全部で15個あるよ！でも探すためだけにサーバーを荒らすのはやめてね！"
    };

    // コンストラクタ
    public CommandWhatsNew() {
        if(Files.notExists(Paths.get(DESCRIPTION_FILE_NAME))) {
            logger.info("description.json does not exist.");
            ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
            description.put("version", "1.0.1");
            description.put("major", "破壊的な変更はありません。ちゃんとコードが書けててえらい！");
            description.put("minor", "機能追加はありません。ちゃんとコードが書けててえらい！");
            description.put("patch", "修正はありません。ちゃんとコードが書けててえらい！");
            description.put("Developer", "Ranfa/hizumiaoba/Indigo_leaF P#4144");
            try {
                writer.writeValue(Paths.get(DESCRIPTION_FILE_NAME).toFile(), description);
            } catch (IOException e) {
                logger.error("Failed to create description.json", e);
            }
        }
        readDescription();
        description.forEach((k, v) -> logger.info("{} : {}", k, v));
        logger.info("Completed initialization for command : \"whatsnew\"");
    }

    public void readDescription() {
        logger.info("reading whats new from the description file...");
        logger.info("description file name is " + DESCRIPTION_FILE_NAME);

        final InputStream is = getClass().getResourceAsStream("/" + DESCRIPTION_FILE_NAME);

        try {
            description = new ObjectMapper().readValue(is, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            logger.warn("failed to read description file", e);
            description = new HashMap<>();
        }
        logger.info("description file read completed");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        readDescription();
        description.forEach((k, v) -> logger.info("{} : {}", k, v));
        Guild guild = event.getGuild();
        TextChannel channel = event.getTextChannel();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User user = event.getAuthor();
        if(user.isBot())
            return;
        if(content.equalsIgnoreCase(App.prefix + "whatsnew")) {
            int index = random.nextInt(TITLE_MEME.length);
            logger.info("Guild : {}", guild);
            logger.info("Channel : {}", channel);
            logger.info("Message : {}", message);
            logger.info("User : {}", user.getName());
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(TITLE_MEME[index]);
            builder.setColor(Color.YELLOW);
            builder.setDescription("それぞれの番号がインクリメントしたときに追加、修正された内容です。");
            builder.addField("メジャー", description.get("major"), false);
            builder.addField("機能追加", description.get("minor"), false);
            builder.addField("修正", description.get("patch"), false);
            builder.setFooter(description.get("version") + " by " + description.get("Developer"));
            channel.sendMessageEmbeds(builder.build()).queue();
        }
    }
    
}
