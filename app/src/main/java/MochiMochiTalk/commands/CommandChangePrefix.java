package MochiMochiTalk.commands;


import MochiMochiTalk.App;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandChangePrefix extends ListenerAdapter {

  private static Logger logger = LoggerFactory.getLogger(CommandChangePrefix.class);

  private static void changePrefix(String prefix) {
    App.setStaticPrefix(prefix);
  }

  private static void write(String prefix) {
    ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
    Map<String, Object> data = new HashMap<>();
    data.put("prefix", prefix);
    data.put("token", App.getStaticToken());
    try {
      writer.writeValue(Paths.get("property.json").toFile(), data);
    } catch (IOException e) {
      logger.error("Failed to write file.", e);
    }
  }

  @Override
  public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
    Message message = event.getMessage();
    String content = message.getContentRaw();
    User author = event.getAuthor();
    MessageChannel channel = event.getChannel();
    if (author.isBot()) {
      return;
    }
    if (content.startsWith(App.getStaticPrefix() + "prefix")) {
      String[] split = content.split(" ");
      if (split.length == 2) {
        changePrefix(split[1]);
        write(split[1]);
        channel.sendMessage("prefixを" + App.getStaticPrefix() + "に変更しました").queue();
      } else {
        channel.sendMessage("prefixを変更するには1つの引数が必要です").queue();
      }
    }
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("prefix")) {
      String newPrefix = event.getOption("new",
          OptionMapping::getAsString); // #getOption won't be null since it requires this option to interact this slash command
      changePrefix(newPrefix);
      write(newPrefix);
      event.replyFormat("Prefixを %s に変更しました。", newPrefix).queue();
    }
  }
}
