package MochiMochiTalk.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = DerepoUpdateCacheJsonSerializer.class)
public class CacheData {
    
    final String latestUpdateDateString;
    final JsonNode internalData;

}
