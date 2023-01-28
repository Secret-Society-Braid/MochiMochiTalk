package MochiMochiTalk.libs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

public class DerepoUpdatesTest {

  @Test
  public void DerepoUpdatesDetectorTest() {
    try {
      Class<?> clazz = Class.forName("MochiMochiTalk.lib.DerepoUpdatesDetector");
      Constructor<?>[] constructors = clazz.getDeclaredConstructors();

      // asserts that there is only one constructor
      assertEquals(1, constructors.length);

      Constructor<?> constructor = constructors[0];
      constructor.setAccessible(true);
      var instance = constructor.newInstance();

      // asserts that the constructor is nonnull
      assertNotNull(instance);

    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
             IllegalArgumentException | InvocationTargetException | SecurityException e) {
      fail(e);
    }
  }

}
