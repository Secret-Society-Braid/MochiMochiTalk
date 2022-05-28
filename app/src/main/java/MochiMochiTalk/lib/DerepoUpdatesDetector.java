package MochiMochiTalk.lib;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * でれぽ更新検知クラス
 * 
 * @author Ranfa
 * @since 1.0.0
 */
public class DerepoUpdatesDetector {
    
    private static final Logger log = LoggerFactory.getLogger(DerepoUpdatesDetector.class);
    public static final String DEREPO_UPDATE_API_URI = "https://api.matsurihi.me/cgss/v1/derepo/statuses?idolId=216&maxResults=1";
    private static final String LATEST_UPDATE_CACHE_FILE_NAME = "derepoLatest.json";
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+'Z");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DerepoUpdatesDetector() {
        /* do nothing */
    }

    public static CacheData getDerepoUpdate() throws IOException {
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

        if(Files.notExists(Paths.get(LATEST_UPDATE_CACHE_FILE_NAME))) {
            CacheData latest = new CacheData(dateNow, response);
            OBJECT_MAPPER.writeValue(Paths.get(LATEST_UPDATE_CACHE_FILE_NAME).toFile(), latest);
            return latest;
        }
        CacheData latestCache = OBJECT_MAPPER.readValue(Paths.get(LATEST_UPDATE_CACHE_FILE_NAME).toFile(), CacheData.class);
        try {
            final Date latestDate = df.parse(response.get("postTime").toString());
            final Date cacheDate = df.parse(latestCache.getLatestUpdateDateString());
            int compared = latestDate.compareTo(cacheDate);
            switch (compared) {
                case 0:
                    log.info("There is no updates.");
                    return latestCache;
                case 1:
                    log.info("There are some updates.");
                    CacheData cache = new CacheData(dateNow, response);
                    OBJECT_MAPPER.writeValue(Paths.get(LATEST_UPDATE_CACHE_FILE_NAME).toFile(), cache);
                    return cache;
                default:
                    log.warn("something went wrong. The system clock might be incorrect.");
                    break;
            }
        } catch (ParseException e) {
            log.error("Failed to parse cache date: {}", e);
        }
        return null;
    }

}
