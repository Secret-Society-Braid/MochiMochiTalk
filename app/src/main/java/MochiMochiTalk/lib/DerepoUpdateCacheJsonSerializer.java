package MochiMochiTalk.lib;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class DerepoUpdateCacheJsonSerializer extends StdSerializer<CacheData> {

  public DerepoUpdateCacheJsonSerializer() {
    this(null);
  }

  public DerepoUpdateCacheJsonSerializer(Class<CacheData> t) {
    super(t);
  }

  @Override
  public void serialize(CacheData value, JsonGenerator generator, SerializerProvider provider)
      throws IOException {
    generator.writeStartObject();
    generator.writeStringField("latestUpdateDateString", value.getLatestUpdateDateString());
    generator.writeFieldName("internalData");
    generator.writeTree(value.getInternalData());
    generator.writeEndObject();
  }

}
