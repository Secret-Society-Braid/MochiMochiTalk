package MochiMochiTalk.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = DerepoUpdateCacheJsonSerializer.class)
public class CacheData {
    
    final String latestUpdateDateString;
    final JsonNode internalData;

    public CacheData(final String latestUpdateDateString, final JsonNode internalData) {
        this.latestUpdateDateString = latestUpdateDateString;
        this.internalData = internalData;
    }

    public String getLatestUpdateDateString() {
        return latestUpdateDateString;
    }

    public JsonNode getInternalData() {
        return internalData;
    }
}
