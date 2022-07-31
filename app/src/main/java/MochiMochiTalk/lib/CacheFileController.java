package MochiMochiTalk.lib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheFileController {
    
    private static CacheFileController singleton;

    public static final String DIRECTORY_NAME = ".tmp" + File.separator + ".vocalcord";

    private List<Path> paths;
 
    public static CacheFileController getInstance() {
        if(singleton == null)
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

}
