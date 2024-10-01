package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

@Slf4j
public class CommandWhatsNew extends CommandInformation {

  private final DescriptionFileHandler description;

  public CommandWhatsNew() {
    this.description = new DescriptionFileHandler();
  }

  @Override
  public String getCommandName() {
    return "whatsnew";
  }

  @Override
  protected String getCommandDescription() {
    return "最近の更新内容を表示します。";
  }

  @Override
  protected void setCommandData() {
    this.commandData = Commands.slash(
      this.getCommandName(),
      this.getCommandDescription()
    );
  }

  @Override
  public void slashCommandHandler(@Nonnull SlashCommandInteractionEvent event) {
    log.info("whatsnew slash command invoked");
    event
      .getHook()
      .setEphemeral(true)
      .sendMessageEmbeds(this.description.buildMessage())
      .queue();
  }

  @Slf4j
  static class DescriptionFileHandler {

    private static final Random random = new Random();
    private static final String DESCRIPTION_FILE_NAME = "description.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String[] TITLE_MEME = {
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
    private final Map<String, String> description;

    DescriptionFileHandler() {
      this.description = readDescription();
    }

    private Map<String, String> readDescription() {
      log.trace("description file name is " + DESCRIPTION_FILE_NAME);

      final InputStream is = getClass().getResourceAsStream("/" + DESCRIPTION_FILE_NAME);

      try {
        return mapper.readValue(is, new TypeReference<>() {
        });
      } catch (IOException e) {
        log.warn("failed to read description file", e);
        return Collections.emptyMap();
      }
    }

    MessageEmbed buildMessage() {
      int index = random.nextInt(TITLE_MEME.length);
      EmbedBuilder builder = new EmbedBuilder();
      builder.setTitle(TITLE_MEME[index]);
      builder.setColor(Color.YELLOW);
      builder.setDescription("それぞれの番号がインクリメントしたときに追加、修正された内容です。");
      builder.addField("不具合対応",
        Objects.requireNonNullElse(this.description.get("hotfix"), "ありません。"), false);
      builder.addField("機能追加",
        Objects.requireNonNullElse(this.description.get("feature"), "ありません。"), false);
      builder.addField("機能修正",
        Objects.requireNonNullElse(this.description.get("bugfix"), "ありません。"), false);
      builder.addField("既知の不具合",
        Objects.requireNonNullElse(this.description.get("bugs"), "ありません。"), false);
      builder.setFooter(
        this.description.get("version") + " by " + this.description.get("Developer"));
      return builder.build();
    }
  }

}
