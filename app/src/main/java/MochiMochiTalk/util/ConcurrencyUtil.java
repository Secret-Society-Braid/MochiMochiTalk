package MochiMochiTalk.util;

import java.util.concurrent.ThreadFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE) // implicit private constructor
public class ConcurrencyUtil {

  public static ThreadFactory createThreadFactory(String threadNameSupplier) {
    return new CountingThreadFactory(() -> "MochiMochiTalk", threadNameSupplier);
  }

  public static <T> void postEventHandling(T ret, Throwable t) {
    if (t == null) {
      log.info("Event handling completed successfully. ret={}", ret);
    } else {
      log.warn("There was an error while handling an event. ret={}, t={}", ret, t);
    }
  }
}
