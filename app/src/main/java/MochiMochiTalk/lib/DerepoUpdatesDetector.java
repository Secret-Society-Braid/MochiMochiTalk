package MochiMochiTalk.lib;

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

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * でれぽ更新検知クラス
 * 
 * @author Ranfa
 * @since 1.0.0
 */
public class DerepoUpdatesDetector {
    
    private static final Logger log = LoggerFactory.getLogger(DerepoUpdatesDetector.class);
    public static final String DEREPO_UPDATE_API_URI = "https://api.matsurihi.me/cgss/v1/derepo/statuses";
    private static CacheData prevCache = null;
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+'Z");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> UPDATE_POST_CHANNELS = List.of("650565684924776448");

    private DerepoUpdatesDetector() {
        /* do nothing */
    }

    @Nullable
    private static CacheData getDerepoUpdate() throws IOException {
        Date date = new Date();
        final String dateNow = df.format(date);
        final String uri = DEREPO_UPDATE_API_URI + "?idolId=216&maxResults=1" + "&timeMin=" + dateNow;
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if(responseCode != 200) {
            log.error("Failed to get Derepo update. Response code: {}", responseCode);
            return null;
        }
        JsonNode response = OBJECT_MAPPER.readTree(conn.getInputStream());

        if(prevCache == null) {
            prevCache = new CacheData(dateNow, response);
            return null;
        }
        try {
            final Date latestDate = df.parse(response.get("postTime").toString());
            final Date cacheDate = df.parse(prevCache.getLatestUpdateDateString());
            int compared = latestDate.compareTo(cacheDate);
            switch (compared) {
                case 0:
                    log.info("There is no updates.");
                    return null;
                case 1:
                    log.info("There are some updates.");
                    prevCache = new CacheData(dateNow, response);
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
            final CacheData latest = getDerepoUpdate();
            if(latest != null) {
                final MessageEmbed embed = embedData(latest);
                UPDATE_POST_CHANNELS
                    .stream()
                    .map(jda::getTextChannelById)
                    .filter(Objects::nonNull)
                    .forEach(textChannel -> {
                        textChannel.sendMessageEmbeds(embed).submit();
                    }
                );
            }
        } catch (IOException e) {
            log.error("There was a problem while getting derepo update.", e);
        }
    }

    private static MessageEmbed embedData(CacheData data) {
        JsonNode node = data.getInternalData().get(0);
        final int groupOrder = node.get("groupOrder").asInt();
        CompletableFuture<JsonNode> groupNodeFuture = null;
        if (groupOrder != 1) {
            groupNodeFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    final int groupId = node.get("groupId").asInt();
                    final String uri = DEREPO_UPDATE_API_URI + "?idolId=216&groupId=" + groupId;
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
        if(groupNodeFuture != null) {
            JsonNode groupNode = Objects.requireNonNull(groupNodeFuture.join());
            builder.addField("返信先", groupNode.get("name").asText(), false);
        }
        String detail = node.get("message").asText();
        detail = detail.replace("<br>", "\n");
        if(detail.contains("stamp_only"))
            detail = "[スタンプのみ投稿]";
        builder.addField("内容", detail, false);
        JsonNode hashTagNode = node.get("hashtags");
        if(!hashTagNode.isEmpty()) {
            builder.addField("ハッシュタグ", "なし", false);
        } else {
            StringBuilder sBuilder = new StringBuilder();
            hashTagNode.forEach(value -> {
                sBuilder.append(value.get("word").asText()).append(", ");
            });
            sBuilder.deleteCharAt(sBuilder.length() - 1);
            sBuilder.deleteCharAt(sBuilder.length() - 1);
            builder.addField("ハッシュタグ", sBuilder.toString(), false);
        }
        builder.setFooter(node.get("postTime").asText());
        return builder.build();
    }

}
