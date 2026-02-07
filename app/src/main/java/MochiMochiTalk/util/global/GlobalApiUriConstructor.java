package MochiMochiTalk.util.global;

import MochiMochiTalk.lib.global.InvokeMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class GlobalApiUriConstructor {

  private static final String BASE_URI;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    JsonNode configNode;
    try {
      configNode = MAPPER.readTree(
          GlobalApiUriConstructor.class.getResourceAsStream("/property.json"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    BASE_URI = configNode.get("global_raid_api_uri").asText();
  }

  private final InvokeMethod invokeMethod;
  private final String guildId;
  private final String channelId;

  public GlobalApiUriConstructor(InvokeMethod invokeMethod, String guildId, String channelId) {
    this.invokeMethod = invokeMethod;
    this.guildId = guildId;
    this.channelId = channelId;
  }

  public String construct() {
    return String.format("%s?invokeMethod=%s&guildId=%s&channelId=%s", BASE_URI, invokeMethod,
        guildId,
        channelId);
  }
}
