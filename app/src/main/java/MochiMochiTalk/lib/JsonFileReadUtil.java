package MochiMochiTalk.lib;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import MochiMochiTalk.lib.datatype.LicenseData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonFileReadUtil {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonFileReadUtil() {
        /* util class */
    }
    
    @Nullable
    public static List<LicenseData> getLicenseData() {
        InputStream is = JsonFileReadUtil.class.getResourceAsStream("/license.json");
        log.info("attempt to read license data from: {}", is);
        try {
            JsonNode node = MAPPER.readTree(is);
            if(node.isArray()) {
                List<LicenseData> res = new ArrayList<>();
                node.forEach(each -> 
                    res.add(LicenseData.of(
                        each.get("deps").asText(),
                        each.get("description").asText(),
                        each.get("license").asText(),
                        each.get("url").asText()))
                );
                return res;
            }
            return Collections.emptyList();
        } catch (IOException e) {
            log.error("Exception reading license data", e);
            return Collections.emptyList();
        }
    }
}
