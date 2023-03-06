package MochiMochiTalk.lib;

import MochiMochiTalk.util.ConcurrencyUtil;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheFileController {

  public static final String DIRECTORY_NAME = "tmp" + File.separator + ".vocalcord";
  private static final ScheduledExecutorService updateScheduler = Executors.newScheduledThreadPool(
      1,
      ConcurrencyUtil.createThreadFactory("Voice byte data Cache Controller Thread"));
  private static CacheFileController singleton;
  private List<Path> paths;

  private CacheFileController() {
    createParentDirectoriesIfNeed();
    this.paths = getListOfPaths();
    updateScheduler.scheduleAtFixedRate(this::update, 3, 1, TimeUnit.SECONDS);
  }

  public static synchronized CacheFileController getInstance() {
    if (singleton == null) {
      singleton = new CacheFileController();
    }
    return singleton;
  }

  private static List<Path> getListOfPaths() {
    List<Path> res = new ArrayList<>();
    try (Stream<Path> paths = Files.walk(Paths.get(DIRECTORY_NAME))) {
      res = paths
          .filter(file -> file.getFileName().endsWith(".cache"))
          .map(CacheFileController::getFileNameWithPath)
          .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("Encountered I/O error while traversing cache file directory", e);
    }
    return res;
  }

  private static void createParentDirectoriesIfNeed() {
    try {
      Files.createDirectories(Paths.get(DIRECTORY_NAME));
    } catch (IOException e) {
      log.error("Encountered I/O Error while handling directory creation");
    }
  }

  private static Path getFileNameWithPath(Path path) {
    return path.getFileName();
  }

  public Path storeCache(String phrase, byte[] bytes) throws IOException {
    if (Strings.isNullOrEmpty(phrase)) {
      return Paths.get(DIRECTORY_NAME);
    }
    String fileName = Hashing.sha384().newHasher().putString(phrase, StandardCharsets.UTF_8).hash()
        .toString();
    Path storePath = Paths.get(DIRECTORY_NAME + File.separator + fileName + ".cache");
    return Files.write(storePath, bytes);
  }

  public Optional<byte[]> getCacheBytes(String phrase) throws IOException {
    if (Strings.isNullOrEmpty(phrase)) {
      return Optional.empty();
    }
    String fileName = Hashing.sha384().newHasher().putString(phrase, StandardCharsets.UTF_8).hash()
        .toString();
    Path indexPath = Paths.get(DIRECTORY_NAME + File.separator + fileName + ".cache");
    if (Files.exists(indexPath)) {
      return Optional.ofNullable(Files.readAllBytes(indexPath));
    } else {
      return Optional.empty();
    }
  }

  public List<Path> getPaths() {
    return this.paths;
  }

  public synchronized boolean update() {
    List<Path> updated = getListOfPaths();
    if (this.paths.equals(updated)) {
      return false;
    }
    this.paths = updated;
    return true;
  }

}
