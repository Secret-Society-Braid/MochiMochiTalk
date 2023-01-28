package MochiMochiTalk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import MochiMochiTalk.lib.FileReadThreadImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FileReadTest {

  @Test
  @Disabled("target file is ignored by gitignore")
  public void readFileTest() {
    FileReadThreadImpl thread = new FileReadThreadImpl();
    thread.run();

    assertEquals("!!", thread.getPrefix());
  }

}
