package MochiMochiTalk.lib;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheFileController {

    private static CacheFileController singleton;

    public static final String DIRECTORY_NAME = ".tmp" + File.separator + ".vocalcord";

    private List<Path> paths;

    public static CacheFileController getInstance() {
        if (singleton == null)
            singleton = new CacheFileController();
        return singleton;
    }

    private CacheFileController() {
        this.paths = getListOfPaths();
    }

    private static List<Path> getListOfPaths() {
        List<Path> res = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(DIRECTORY_NAME))) {
            res = paths.filter(file -> file.getFileName().endsWith(".cache")).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Encountered I/O error while traversing cache file directory", e);
        }
        return res;
    }

    public Path storeCache(String phrase, byte[] bytes) throws IOException {
        if (Strings.isNullOrEmpty(phrase))
            return Paths.get(DIRECTORY_NAME);
        String fileName = Base64.getEncoder().encodeToString(phrase.getBytes(StandardCharsets.UTF_8));
        Path storePath = Paths.get(DIRECTORY_NAME + File.separator + fileName);
        return Files.write(storePath, bytes);
    }

    public Optional<byte[]> getCacheBytes(String phrase) throws IOException {
        if(Strings.isNullOrEmpty(phrase))
            return Optional.empty();
        String fileName = Base64.getEncoder().encodeToString(phrase.getBytes(StandardCharsets.UTF_8));
        Path indexPath = Paths.get(DIRECTORY_NAME + File.separator + fileName);
        if(Files.exists(indexPath))
            return Optional.ofNullable(Files.readAllBytes(indexPath));
        else
            return Optional.empty();
    }

    public List<Path> getPaths() {
        return this.paths;
    }

    public boolean update() {
        List<Path> updated = getListOfPaths();
        if(this.paths.equals(updated))
            return false;
        this.paths = updated;
        return true;
    }

}
