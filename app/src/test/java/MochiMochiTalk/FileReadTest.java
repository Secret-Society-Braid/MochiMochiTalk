package MochiMochiTalk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import MochiMochiTalk.lib.FileReadThreadImpl;

public class FileReadTest {

    @Test
    public void readFileTest() {
        FileReadThreadImpl thread = new FileReadThreadImpl();
        thread.run();

        assertEquals("!!", thread.getPrefix());
    }
    
}
