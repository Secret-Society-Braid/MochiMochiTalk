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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
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

    // TODO: Complete implementation
}
