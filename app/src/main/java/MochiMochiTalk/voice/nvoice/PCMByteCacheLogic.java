package MochiMochiTalk.voice.nvoice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import MochiMochiTalk.lib.CacheFileController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PCMByteCacheLogic {
    
    private final CacheFileController cacheController;

    private final List<Path> cachePathList;

    static class CacheResponse {
        byte[] pcmIfCached;
        boolean shouldCache;

        public static CacheResponse phraseAlreadyCached(byte[] pcmIfCached) {
            CacheResponse response = new CacheResponse();
            response.pcmIfCached = pcmIfCached;
            return response;
        }

        public static CacheResponse shouldCachePhrase() {
            CacheResponse response = new CacheResponse();
            response.shouldCache = true;
            return response;
        }

        public static CacheResponse doNothing() { 
            CacheResponse response = new CacheResponse();
            response.shouldCache = false;
            response.pcmIfCached = null;
            return response;
        }
    }

    PCMByteCacheLogic() {
        // initialize fields
        cacheController = CacheFileController.getInstance();
        cachePathList = cacheController.getPaths();

        // init
        log.debug("Cache loading complete. amount of paths: {}", cachePathList.size());

    }

    CacheResponse checkCache(String phrase) {
        String scrubed = scrubPhrase(phrase);

        Optional<byte[]> optionalPcmByte = Optional.empty();
        try {
            optionalPcmByte = cacheController.getCacheBytes(scrubed);
        } catch (IOException e) {
            log.error("Encountered I/O error while searching cache file.", e);
        }
        return optionalPcmByte.isPresent()
            ? CacheResponse.phraseAlreadyCached(optionalPcmByte.get())
            : CacheResponse.shouldCachePhrase();
    }

    private static String scrubPhrase(String phrase) {
        return phrase.toLowerCase().replaceAll("\\s+", "");
    }

}
