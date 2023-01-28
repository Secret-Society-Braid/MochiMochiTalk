package MochiMochiTalk.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * でれぽ更新検知クラス
 *
 * @author Ranfa
 * @since 1.0.0
 */
public class DerepoUpdatesDetector {

  public static final String DEREPO_UPDATE_API_URI = "https://api.matsurihi.me/cgss/v1/derepo/statuses";
  private static final Logger log = LoggerFactory.getLogger(DerepoUpdatesDetector.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final List<String> UPDATE_POST_CHANNELS = List.of("650565684924776448",
      "987723595692257330");
  private static JsonNode prevCache = null;

  private DerepoUpdatesDetector() {
    /* do nothing */
  }

  @Nullable
  private static JsonNode getDerepoUpdate() throws IOException {
    final String uri = DEREPO_UPDATE_API_URI + "?idolId=216&maxResults=1";
    URL url = new URL(uri);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
    conn.connect();
    int responseCode = conn.getResponseCode();
    if (responseCode != 200) {
      log.error("Failed to get Derepo update. Response code: {}", responseCode);
      return null;
    }
    JsonNode response = OBJECT_MAPPER.readTree(conn.getInputStream());

    if (prevCache == null) {
      prevCache = response.get(1);
      return null;
    }
    try {
      final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
      final Date latestDate = df.parse(response.get(0).get("postTime").asText());
      final Date cacheDate = df.parse(prevCache.get("postTime").asText());
      int compared = latestDate.compareTo(cacheDate);
      switch (compared) {
        case 0:
          log.info("There is no updates.");
          return null;
        case 1:
          log.info("There are some updates.");
          prevCache = response.get(0);
          return prevCache;
        default:
          log.warn("something went wrong. The system clock might be incorrect.");
          break;
      }
    } catch (ParseException e) {
      log.error("Failed to parse cache date: {}", e);
    }
    return null;
  }

  public static void postDataCycle(JDA jda) {
    try {
      final JsonNode latest = getDerepoUpdate();
      if (latest != null) {
        log.info("There is an update available.");
        log.info("update data: {}", latest);
        MessageEmbed embed = embedData(latest);
        log.debug("post data: {}", embed);
        UPDATE_POST_CHANNELS
            .stream()
            .map(jda::getTextChannelById)
            .filter(Objects::nonNull)
            .forEach(textChannel -> {
                  textChannel.sendMessageEmbeds(embed).queue();
                }
            );
      }
    } catch (IOException e) {
      log.error("There was a problem while getting derepo update.", e);
    }
  }

  @Nonnull
  private static MessageEmbed embedData(JsonNode data) {
    JsonNode node = data;
    final int groupOrder = node.get("groupOrder").asInt();
    CompletableFuture<JsonNode> groupNodeFuture = null;
    if (groupOrder != 1) {
      groupNodeFuture = CompletableFuture.supplyAsync(() -> {
        try {
          final int groupId = node.get("groupId").asInt();
          final String uri = DEREPO_UPDATE_API_URI + "?groupId=" + groupId;
          URL url = new URL(uri);
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("User-Agent", "Mozilla/5.0");
          conn.connect();
          int responseCode = conn.getResponseCode();
          if (responseCode != 200) {
            log.error("Failed to get Derepo update. Response code: {}", responseCode);
            return null;
          }
          return OBJECT_MAPPER.readTree(conn.getInputStream());
        } catch (IOException e) {
          log.error("Failed to get related posts.", e);
        }
        return null;
      });
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle("でれぽ更新検知");
    builder.setDescription("むつみちゃんの新しいでれぽ投稿を確認しました！");
    builder.setColor(0x00ff00);
    if (groupNodeFuture != null) {
      groupNodeFuture.whenComplete((ret, ex) -> {
        if (ex != null) {
          log.error("Failed to get related posts.", ex);
          return;
        }
      });
      JsonNode groupNode = Objects.requireNonNull(groupNodeFuture.join());
      log.debug("The root post is a part of a group.");
      log.debug("parsing group data. {}", groupNode);
      builder.addField("返信先",
          Objects.requireNonNull(groupNode.get(groupOrder).get("name").asText()), false);
    }
    String detail = node.get("message").asText();
    detail = detail.replace("<br>", "\n");
    if (detail.contains("stamp_only")) {
      detail = "[スタンプのみ投稿]";
    }
    builder.addField("内容", detail, false);
    JsonNode hashTagNode = node.get("hashtags");
    if (hashTagNode.isEmpty()) {
      builder.addField("ハッシュタグ", "なし", false);
    } else {
      StringBuilder sBuilder = new StringBuilder();
      hashTagNode.forEach(value -> {
        sBuilder.append(value.get("word").asText()).append(", ");
      });
      sBuilder.deleteCharAt(sBuilder.length() - 1);
      sBuilder.deleteCharAt(sBuilder.length() - 1);
      builder.addField("ハッシュタグ", Objects.requireNonNull(sBuilder.toString()), false);
    }
    builder.setFooter(node.get("postTime").asText() + " powered by matsurihi.me");
    return builder.build();
  }

}
