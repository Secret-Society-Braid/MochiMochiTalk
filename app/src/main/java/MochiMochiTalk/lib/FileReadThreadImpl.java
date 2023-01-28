package MochiMochiTalk.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileReadThreadImpl {

  private static final String FILENAME = "property.json";
  private Logger logger = LoggerFactory.getLogger(FileReadThreadImpl.class);
  private Map<String, Object> propertyMap;
  private TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
  };
  private boolean flag = false;

  public FileReadThreadImpl() {
    logger.info("FileReadThreadImpl created.");
    if (Files.notExists(Paths.get(FILENAME))) {
      logger.info("File not found. Creating new file.");
      Map<String, Object> initMap = new ConcurrentHashMap<>();
      initMap.put("prefix", "!!");
      initMap.put("token", "none");
      write(initMap);
    }
  }

  public void run() {
    logger.info("FileReadThreadImpl started.");
    read();
    propertyMap.forEach((key, value) -> logger.info("{} : {}", key, value));
    flag = true;
  }

  @Deprecated
  public void write(Map<String, Object> data) {
    ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
    try {
      writer.writeValue(Paths.get(FILENAME).toFile(), data);
    } catch (IOException e) {
      logger.error("Failed to write file.", e);
    }
  }

  public void read() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      propertyMap = mapper.readValue(this.getClass().getResourceAsStream("/" + FILENAME), typeRef);
    } catch (IOException e) {
      logger.error("Failed to read file.", e);
    }
  }

  public boolean getFlag() {
    return flag;
  }

  public String getPrefix() {
    return flag ? (String) propertyMap.get("prefix") : null;
  }

  public String getToken() {
    return flag ? (String) propertyMap.get("token") : null;
  }

}
