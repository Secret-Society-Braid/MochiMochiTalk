package MochiMochiTalk.libs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import MochiMochiTalk.lib.CacheFileController;
import MochiMochiTalk.voice.DeprecatedTTSEngine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheControllTest {

    private static CacheFileController instance;
    private static DeprecatedTTSEngine ttsEngine;

    @BeforeAll
    public static void setUp() {
        instance = CacheFileController.getInstance();
        ttsEngine = new DeprecatedTTSEngine();
    }

    @Test
    public void instanceNonnullTest() {
        assertNotNull(instance);
    }

    @Test
    public void listOfPathsNonNullTest() {
        List<Path> paths = instance.getPaths();
        assertNotNull(paths);
    }

    @Disabled("in the Github action this will not work correctly")
    @Test
    public void cacheControllTest() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Optional<Path> path = writeTmpBytes();
        assertTrue(path::isPresent);
        assertTrue (Files.exists(path.orElseThrow()));
        byte[] fromFile = Files.readAllBytes(path.orElseThrow());
        byte[] fromTTS = invokeTts("テストメッセージ", ttsEngine);
        assertNotNull(fromFile);
        assertNotNull(fromTTS);
        assertArrayEquals(fromTTS, fromFile);

    }

    

    private static Optional<Path> writeTmpBytes() {
        final String testMessage = "テストメッセージ";
        Path res = null;
        try {
            byte[] bytes = invokeTts(testMessage, ttsEngine);
            res = instance.storeCache(testMessage, bytes);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException eInvoke) {
            log.error("Cannot invoke TTS method. Status: Reflection failed.", eInvoke);
        } catch (IOException ioe) {
            log.error("Encountered I/O Error while handling file operation.", ioe);
        }
        return Optional.ofNullable(res);
    }

    private static byte[] invokeTts(String phrase, DeprecatedTTSEngine ttsEngine) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName("MochiMochiTalk.voice.DeprecatedTTSEngine");
        Method ttsInvokeMethod = clazz.getDeclaredMethod("tts", String.class);
        ttsInvokeMethod.setAccessible(true);
        byte[] res = (byte[]) ttsInvokeMethod.invoke(ttsEngine, phrase);
        return res;
    }
    
}
