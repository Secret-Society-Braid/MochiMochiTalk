package MochiMochiTalk.voice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class DeprecatedTTSCache {
    
    private final File cacheFile;

    private static final int FREQUENT_THRESHOLD = 1;

    private final HashMap<String, byte[]> cachedPhrases;

    private Logger logger = LoggerFactory.getLogger(DeprecatedTTSCache.class);

    // Phrases that TTSCache is currently monitoring to decide if they are frequent enough to be cached
    private final HashMap<String, Integer> considerations = new HashMap<>();

    // Manages some caching related jobs, like IO or periodically cleaning up the hashmap
    private final ThreadPoolExecutor cacheService = new ThreadPoolExecutor(2, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    static class CacheResponse {
        byte[] pcmIfCached;
        boolean shouldCache = false;

        public static CacheResponse phraseAlreadyCached(byte[] pcmIfCached) {
            CacheResponse r = new CacheResponse();
            r.pcmIfCached = pcmIfCached;
            return r;
        }

        public static CacheResponse shouldCachePhrase() {
            CacheResponse r = new CacheResponse();
            r.shouldCache = true;
            return r;
        }

        public static CacheResponse doNothing() {
            CacheResponse r = new CacheResponse();
            r.shouldCache = false;
            r.pcmIfCached = null;
            return r;
        }
    }

    DeprecatedTTSCache() throws Exception {
         cacheFile = new File(".vocalcord" + File.separator + "vocalcord_phrases.cache");

         if(!cacheFile.exists()) {
            Files.createParentDirs(cacheFile);
             if(cacheFile.createNewFile()) {
                logger.info("Created cache file");
                cachedPhrases = new HashMap<>();
             } else {
                 throw new RuntimeException("Error creating cache file");
             }
         } else {
            logger.info("Loading cache file");
            cachedPhrases = load();
            cachedPhrases.forEach((key, value) -> logger.info("cached sentences -> {} : {}", key, value.length));
         }

         // Clear considerations every day, this means that a phrase can only be frequent if FREQUENT_THRESHOLD
        // is acquired in a week
        ScheduledExecutorService streamDaemon = Executors.newScheduledThreadPool(1);
        streamDaemon.scheduleAtFixedRate(considerations::clear, 0, (365 * 100), TimeUnit.DAYS);
    }

    CacheResponse checkCache(String phrase) {
        String cleaned = scrubPhrase(phrase);

        byte[] pcm = cachedPhrases.getOrDefault(cleaned, null);

        if(pcm == null) {
            int count = considerations.getOrDefault(cleaned, 0);
            considerations.put(cleaned, ++count);
            logger.info("Phrase {} is frequent enough to be cached. count : {}", phrase, count);
            if(count >= FREQUENT_THRESHOLD) {
                logger.info("Phrase {} is frequent enough to be cached. count : {}", phrase, count);
            } else {
                logger.info("Phrase {} is not frequent enough to be cached. count : {}", phrase, count);
            }
            return CacheResponse.shouldCachePhrase();
        }
        logger.info("Phrase {} is cached. count : {}", phrase, considerations.get(cleaned));
        return CacheResponse.phraseAlreadyCached(pcm);
    }

    void cache(String phrase, byte[] pcm) {
        cachedPhrases.put(scrubPhrase(phrase), pcm);

        cacheService.execute(this::save);
    }

    private static String scrubPhrase(String phrase) {
        return phrase.toLowerCase().replaceAll("\\s+", "");
    }

    private void save() {
        try {
            FileOutputStream fos = new FileOutputStream(cacheFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(cachedPhrases);
            oos.close();
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, byte[]> load() {
        try {
            FileInputStream fis = new FileInputStream(cacheFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            @SuppressWarnings("unchecked")
            HashMap<String, byte[]> map = (HashMap<String, byte[]>) ois.readObject();
            ois.close();
            fis.close();
            return map;
        } catch(Exception e) {
            logger.error("Error loading cache file", e);
            System.out.println("No cached phrases loaded. This probably isn't an error.");
        }

        return new HashMap<>();
    }
}
