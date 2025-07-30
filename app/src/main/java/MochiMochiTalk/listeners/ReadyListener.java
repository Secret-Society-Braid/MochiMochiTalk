package MochiMochiTalk.listeners;

import jakarta.annotation.Nonnull;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyListener implements EventListener {

  private final Logger logger = LoggerFactory.getLogger(ReadyListener.class);

  @Override
  public void onEvent(@Nonnull GenericEvent event) {
    if (event instanceof ReadyEvent) {
      logger.info("Bot is ready to sing.");
    }
  }
}
