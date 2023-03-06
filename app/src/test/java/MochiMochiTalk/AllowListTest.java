package MochiMochiTalk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import MochiMochiTalk.lib.AllowedVCRead;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class AllowListTest {

  private static final String MUTSU_CORD = "629249258960453652";
  private static final String TEST_SERVER = "649603185115267092";

  private static AllowedVCRead read = new AllowedVCRead();
  private static List<String> allowList;

  @BeforeAll
  public static void setup() {
    allowList = read.read();
  }

  @Test
  public void matchMutsuCord() {
    assertTrue(allowList.contains(MUTSU_CORD));
  }

  @Test
  public void matchTestServer() {
    assertTrue(allowList.contains(TEST_SERVER));
  }

  @Test
  public void rejectOtherTest() {
    assertFalse(allowList.contains("000000000000000000"));
  }

}
