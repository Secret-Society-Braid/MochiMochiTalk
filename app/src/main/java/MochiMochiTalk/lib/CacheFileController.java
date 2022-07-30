package MochiMochiTalk.lib;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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
        // TODO: implement initial path filling logic
    }

    private static List<Path> getListOfPaths() {
        // TODO: implement directory traversing logic
    }

}
