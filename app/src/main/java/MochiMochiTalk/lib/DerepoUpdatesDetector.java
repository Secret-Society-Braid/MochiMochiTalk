package MochiMochiTalk.lib;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;

/**
 * でれぽ更新検知クラス
 * 
 * @author Ranfa
 * @since 1.0.0
 */
public class DerepoUpdatesDetector {
    
    private static final Logger log = LoggerFactory.getLogger(DerepoUpdatesDetector.class);
    public static final String DEREPO_UPDATE_API_URI = "https://api.matsurihi.me/cgss/v1/derepo/statuses?idolId=216&maxResults=1";
    private static CacheData prevCache = null;
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+'Z");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DerepoUpdatesDetector() {
        /* do nothing */
    }

    @Nullable
    private static CacheData getDerepoUpdate() throws IOException {
        Date date = new Date();
        final String dateNow = df.format(date);
        final String uri = DEREPO_UPDATE_API_URI + "&timeMin=" + dateNow;
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
                // TODO: Complete implementation
            }
        } catch (IOException e) {
            log.error("There was a problem while getting derepo update.", e);
        }
    }

}
