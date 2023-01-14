package MochiMochiTalk.lib;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllowedVCRead {

  private static final String ALLOWED_LIST_FILE_NAME = "allowed_list";
  private final Logger LOG = LoggerFactory.getLogger(AllowedVCRead.class);

  public AllowedVCRead() {
    LOG.info("AllowedVCRead created.");
    if (Files.notExists(Paths.get(ALLOWED_LIST_FILE_NAME))) {
      LOG.info("File not found. Creating new file.");
      List<String> init = List.of("629249258960453652", "649603185115267092");
      try {
        Files.createFile(Paths.get(ALLOWED_LIST_FILE_NAME));
        try (FileWriter writer = new FileWriter(Paths.get(ALLOWED_LIST_FILE_NAME).toFile())) {
          for (String s : init) {
            writer.write(s + "\n");
          }
        } catch (IOException e) {
          LOG.error("Failed to write file.", e);
        }
      } catch (IOException e) {
        LOG.error("Failed to create file.", e);
      }
    }
  }

  protected synchronized void write(String... data) {
    try {
      Files.createFile(Paths.get(ALLOWED_LIST_FILE_NAME));
    } catch (IOException e) {
      LOG.error("Failed to create file.", e);
    }
    try (FileWriter writer = new FileWriter(Paths.get(ALLOWED_LIST_FILE_NAME).toFile(), true)) {
      for (String s : data) {
        writer.write(s + "\n");
      }
    } catch (IOException e) {
      LOG.error("Failed to write file.", e);
    }
  }

  public synchronized List<String> read() {
    List<String> list = new ArrayList<>();
    try {
      list = Files.readAllLines(Paths.get(ALLOWED_LIST_FILE_NAME));
    } catch (IOException e) {
      LOG.error("cannot read from file", e);
    }
    return list;
  }

}
