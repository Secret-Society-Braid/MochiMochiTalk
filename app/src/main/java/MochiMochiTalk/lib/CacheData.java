package MochiMochiTalk.lib;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class CacheData {

  final String latestUpdateDateString;
  final JsonNode internalData;

}
