package MochiMochiTalk.mocks;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheSeparatelyMock {
    
    private CacheSeparatelyMock() { /* do nothing */}

    public static Map<String, byte[]> generateBytes() {
        final long generateSeed = System.currentTimeMillis();
        
    }
}
