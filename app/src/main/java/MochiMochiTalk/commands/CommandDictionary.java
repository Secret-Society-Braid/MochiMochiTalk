package MochiMochiTalk.commands;

import MochiMochiTalk.api.CommandInformation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
@Slf4j
public class CommandDictionary extends CommandInformation {

  private static final String DICT_API_URL = "https://script.google.com/macros/s/AKfycbxdQpZ4W4wlNcLJxX36fzSnp5BouNGB3sm_kQ0dim0ZKNnJzuGLfKLqnBlqUzeQV2nB/exec";

  @Override
  public String getCommandName() {
    return "dict";
  }

  @Override
  protected String getCommandDescription() {
    return "Botの単語変換辞書を操作します。";
  }

  @Override
  protected void setCommandData() {
    this.commandData = Commands.slash(this.getCommandName(), this.getCommandDescription())
        .addOptions(new OptionData(OptionType.STRING, "phrase", "変換元の単語を指定します。")
            .setRequired(true))
        .addOptions(new OptionData(OptionType.STRING, "dict", "変換先の単語を指定します。")
            .setRequired(true))
        .setGuildOnly(true);
  }

  @Override
  public void slashCommandHandler(@Nonnull SlashCommandInteractionEvent event) {
    String phrase = event.getOption("phrase", OptionMapping::getAsString);
    String dict = event.getOption("dict", OptionMapping::getAsString);
    try {
      log.info("attempt to update dictionary");
      log.info("word: {}, meaning: {}", phrase, dict);
      updateDic(phrase, dict);
      event.replyFormat("辞書更新が完了しました。 %s : %s", phrase, dict).queue();
      log.info("update complete");
    } catch (IOException e) {
      log.error("error while updating dictionary", e);
      event.replyFormat(
          "辞書を更新中にエラーが発生しました。このことを report コマンドで報告する場合は以下のメッセージを開発者にお伝えください。%n%s",
          e.getMessage()).queue();
    }
  }

  public synchronized void updateDic(String field, String dict) throws IOException {
    Map<String, String> data = Map.of("field", field, "dict", dict);
    String sttringify = new ObjectMapper().writeValueAsString(data);
    URL url = new URL(DICT_API_URL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    OutputStream outStream = conn.getOutputStream();
    BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(outStream, StandardCharsets.UTF_8));
    writer.write(sttringify);
    writer.flush();
    writer.close();
    outStream.close();
    conn.connect();
    log.debug("stringify data: {}", sttringify);
    log.debug("statuscode: {}", conn.getResponseCode());
    log.debug("connection data: {}", conn);
    conn.disconnect();
  }

  public synchronized Optional<String> getDict(String field) {
    URL url;
    try {
      url = new URL(DICT_API_URL + "?field=" + field);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.connect();
      JsonNode res = new ObjectMapper().readTree(conn.getInputStream());
      log.info("fetch complete: {}", res);
      return res.get("code").asInt() == 404 ? Optional.empty()
          : Optional.ofNullable(res.get("response").get("dict").asText());
    } catch (IOException e) {
      log.error("Failed to fetch dictionary", e);
    }
    return Optional.empty();
  }

}
