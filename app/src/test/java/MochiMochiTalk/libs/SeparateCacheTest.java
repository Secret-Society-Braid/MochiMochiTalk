package MochiMochiTalk.libs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.google.common.hash.Hashing;

import MochiMochiTalk.mocks.CacheSeparatelyMock;

public class SeparateCacheTest {

    static String cacheName = "";
    
    @Test
    public void cacheTest() throws IOException {
        Map<String, byte[]> byteCache = CacheSeparatelyMock.generateBytes(48000);

        assertTrue(() -> CacheSeparatelyMock.writeBytes(byteCache));

        String name = byteCache.keySet().iterator().next();
        cacheName = name;
        byte[] extBytes = CacheSeparatelyMock.readBytes(name);
        
        assertTrue(Objects.deepEquals(byteCache.get(name), extBytes));
    }

    
    @AfterAll
    public static void cleanUp() throws IOException {
        String hashName = Hashing.sha256().newHasher().putString(cacheName, StandardCharsets.UTF_8).hash().toString();
        Files.deleteIfExists(Paths.get(hashName));
    }
}
