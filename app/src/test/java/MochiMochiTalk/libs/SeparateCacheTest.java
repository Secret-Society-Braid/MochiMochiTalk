package MochiMochiTalk.libs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import MochiMochiTalk.mocks.CacheSeparatelyMock;

public class SeparateCacheTest {
    
    @Test
    public void cacheTest() throws IOException {
        Map<String, byte[]> byteCache = CacheSeparatelyMock.generateBytes(48000);

        assertTrue(() -> CacheSeparatelyMock.writeBytes(byteCache));

        String name = byteCache.keySet().stream().findFirst().orElse("");
        byte[] extBytes = CacheSeparatelyMock.readBytes(name);
        
        assertTrue(Objects.deepEquals(byteCache.get(name), extBytes));
     }

}
