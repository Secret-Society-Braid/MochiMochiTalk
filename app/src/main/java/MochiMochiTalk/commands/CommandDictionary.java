package MochiMochiTalk.commands;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandDictionary extends ListenerAdapter {

  private static final String DICT_API_URL = "https://script.google.com/macros/s/AKfycbxdQpZ4W4wlNcLJxX36fzSnp5BouNGB3sm_kQ0dim0ZKNnJzuGLfKLqnBlqUzeQV2nB/exec";
  private static final String DICPATH = "dictionary.json";
  private static CommandDictionary singleton;
  private Logger logger = LoggerFactory.getLogger(CommandDictionary.class);

  public CommandDictionary() {
    if (Files.notExists(Paths.get(DICPATH))) {
      logger.info("dictionary.json is not found. Create new dictionary.json.");
      Map<String, String> initMap = new HashMap<>();
      initMap.put("IDOLM@STER", "アイドルマスター");
    }
  }

  public static CommandDictionary getInstance() {
    if (singleton == null) {
      singleton = new CommandDictionary();
    }
    return singleton;
  }

  @Override
  public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
    if (event.getName().equals("dict")) {
      String phrase = event.getOption("phrase", OptionMapping::getAsString);
      String dict = event.getOption("dict", OptionMapping::getAsString);
      if (Objects.requireNonNull(phrase).equals(
          "del")) { // since it requires phrase option, we don't need to check whether null or not
        event.reply("現在辞書の移行作業中のため、削除は report コマンドにてお知らせください。").queue();
        return;
      }
      if (phrase.equals("show")) {
        event.reply("現在辞書の移行作業中のため、辞書の表示は report コマンドにてお知らせください。").queue();
        return;
      }
      try {
        logger.info("attempt to update dictionary");
        logger.info("word: {}, meaning: {}", phrase, dict);
        updateDic(phrase, dict);
        event.replyFormat("辞書更新が完了しました。 %s : %s", phrase, dict).queue();
        logger.info("update complete");
      } catch (IOException e) {
        logger.error("error while updating dictionary", e);
        event.replyFormat(
            "辞書を更新中にエラーが発生しました。このことを report コマンドで報告する場合は以下のメッセージを開発者にお伝えください。%n%s",
            e.getMessage()).queue();
      }
    }
  }

  /**
   * @param dictionary
   * @deprecated オリジナルAPIでの辞書管理に変更するため、非推奨
   */
  @Deprecated(forRemoval = true)
  public void writeDic(Map<String, String> dictionary) {
    ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
    try {
      writer.writeValue(Paths.get(DICPATH).toFile(), dictionary);
    } catch (IOException e) {
      logger.error("Failed to write file.", e);
    }
  }

  /**
   * @return
   * @deprecated オリジナルAPIでの辞書管理に変更するため
   */
  @Deprecated(forRemoval = true)
  public Map<String, String> readDic() {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
    };
    try {
      return mapper.readValue(Paths.get(DICPATH).toFile(), typeRef);
    } catch (IOException e) {
      logger.error("Failed to read file.", e);
    }
    return Collections.emptyMap();
  }


  public void updateDic(String field, String dict) throws IOException {
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
    logger.debug("stringify data: {}", sttringify);
    logger.debug("statuscode: {}", conn.getResponseCode());
    logger.debug("connection data: {}", conn);
    conn.disconnect();
  }

  public Optional<String> getDict(String field) {
    URL url;
    try {
      url = new URL(DICT_API_URL + "?field=" + field);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.connect();
      JsonNode res = new ObjectMapper().readTree(conn.getInputStream());
      logger.info("fetch complete: {}", res);
      return res.get("code").asInt() == 404 ? Optional.empty()
          : Optional.ofNullable(res.get("response").get("dict").asText());
    } catch (IOException e) {
      logger.error("Failed to fetch dictionary", e);
    }
    return Optional.empty();
  }

}
