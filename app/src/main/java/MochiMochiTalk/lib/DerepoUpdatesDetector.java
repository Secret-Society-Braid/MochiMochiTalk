package MochiMochiTalk.lib;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerepoUpdatesDetector {
    
    private static final Logger log = LoggerFactory.getLogger(DerepoUpdatesDetector.class);
    public static final String DEREPO_UPDATE_API_URI = "https://api.matsurihi.me/cgss/v1/derepo/statuses?idolId=216";
    private static final String LATEST_UPDATE_CACHE_FILE_NAME = "derepoLatest.json";
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+'Z");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static DerepoUpdatesDetector singleton;

    public DerepoUpdatesDetector() {
        try {
            updateCache();
        } catch (IOException e) {
            log.error("Cannot load latest derepo update cache.", e);
        }
    }

    @Nonnull
    public static DerepoUpdatesDetector getInstance() {
        if(singleton == null)
            singleton = new DerepoUpdatesDetector();
        return singleton;
    }

    public void updateCache() throws IOException {
        if(Files.notExists(Paths.get(LATEST_UPDATE_CACHE_FILE_NAME)))
            createCache();
        writeLocal();
    }

    public void createCache() {
        log.info("Creating cache file...");
        // TODO: output the json file to cache.
        log.info("Cache file created.");
    }

    @Nonnull
    private String buildUri(final String sinceTime, final int maxResultThreshold) {
        final String timeMin = (sinceTime == null) ? null : "&timeMin=" + sinceTime;
        final String maxResults = (maxResultThreshold == 0) ? null : "&maxResults=" + maxResultThreshold;
        return Joiner.on("")
            .skipNulls()
            .join(DEREPO_UPDATE_API_URI, timeMin, maxResults);
    }

    @Nullable
    private JsonNode fetchAPI(String builtUri) throws IOException {
        URL apiUrl = new URL(builtUri);
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if(responseCode != 200) {
            log.error("Failed to fetch API. Response code: {}", responseCode);
            return null;
        }
        return OBJECT_MAPPER.readTree(conn.getInputStream());
    }

    @Nonnull
    private CacheData readLocal() throws IOException {
        return OBJECT_MAPPER.readValue(Paths.get(LATEST_UPDATE_CACHE_FILE_NAME).toFile(), CacheData.class);
    }

    private void writeLocal() throws IOException {
        String lastUpdatedDateString = df.format(new Date());
        CacheData cacheData = readLocal();
        final JsonNode latestUpdate = fetchAPI(buildUri(cacheData.getLatestUpdateDateString(), 0));
        OBJECT_MAPPER.writeValue(Paths.get(LATEST_UPDATE_CACHE_FILE_NAME).toFile(), null);
    }
}
