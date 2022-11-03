package MochiMochiTalk.commands;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandWhatsNew extends ListenerAdapter {

    // フィールド作成
    private Logger logger = LoggerFactory.getLogger(CommandWhatsNew.class);
    private Map<String, String> description = new HashMap<>();

    private static final String DESCRIPTION_FILE_NAME = "description.json";
    private static Random random = new Random();
    private static CommandWhatsNew singleton = null;

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

    public static CommandWhatsNew getInstance() {
        if (singleton == null)
            singleton = new CommandWhatsNew();
        return singleton;
    }

    // コンストラクタ
    private CommandWhatsNew() {
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
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User user = event.getAuthor();
        if(user.isBot())
            return;
        if(content.equalsIgnoreCase(App.getStaticPrefix() + "whatsnew")) {
            description.forEach((k, v) -> logger.info("{} : {}", k, v));
            logger.info("Guild : {}", guild);
            logger.info("Channel : {}", channel);
            logger.info("Message : {}", message);
            logger.info("User : {}", user.getName());
            channel.sendMessageEmbeds(buildMessage()).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if(event.getName().equals("whatsnew")) {
            description.forEach((k, v) -> logger.info("{} : {}", k, v));
            logger.info("whatsnew slash command invoked");
            event.replyEmbeds(buildMessage()).queue();
        }
    }

    @Nonnull
    public MessageEmbed buildMessage() {
        readDescription();
        int index = random.nextInt(TITLE_MEME.length);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(TITLE_MEME[index]);
        builder.setColor(Color.YELLOW);
        builder.setDescription("それぞれの番号がインクリメントしたときに追加、修正された内容です。");
        builder.addField("不具合対応", Objects.requireNonNull(description.get("hotfix")), false);
        builder.addField("機能追加", Objects.requireNonNull(description.get("feature")), false);
        builder.addField("機能修正", Objects.requireNonNull(description.get("bugfix")), false);
        builder.addField("既知の不具合", Objects.requireNonNull(description.get("bugs")), false);
        builder.setFooter(description.get("version") + " by " + description.get("Developer"));
        return builder.build();
    }
    
}
