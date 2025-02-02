/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package MochiMochiTalk;

import MochiMochiTalk.commands.CommandDictionary;
import MochiMochiTalk.commands.SlashCommandRegisteration;
import MochiMochiTalk.listeners.CheckContainsDiscordURL;
import MochiMochiTalk.listeners.EventLogger;
import MochiMochiTalk.listeners.ReadyListener;
import MochiMochiTalk.util.ConcurrencyUtil;
import MochiMochiTalk.voice.nvoice.EventListenerForTTS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static final Logger logger = LoggerFactory.getLogger(App.class);
  private static String prefix = "";
  private static final ExecutorService executorService = Executors.newCachedThreadPool(
      ConcurrencyUtil.createThreadFactory("concurrent-runner"));

  public static void main(String[] args) {
    logger.info("Hello, world!");
    CompletableFuture.supplyAsync(() -> {
      // read token and prefix from property.json file in resources
      Map<String, String> map;
      try {
        map = new ObjectMapper().readValue(App.class.getResourceAsStream("/property.json"),
            new TypeReference<>() {
            });
      } catch (IOException e) {
        logger.error("Failed to read property.json file.", e);
        throw new RuntimeException(e);
      }
      return map;
    }, executorService).thenApplyAsync(map -> {
      JDABuilder builder = JDABuilder.createDefault(map.get("token"));
      setStaticPrefix(map.get("prefix"));
      return builder;
    }, executorService).thenAcceptAsync(builder -> {
      logger.info("TOKEN was successfully set.");
      try {
        builder
            .disableCache(CacheFlag.MEMBER_OVERRIDES)
            .setBulkDeleteSplittingEnabled(false)
            .setActivity(Activity.competing("ぷかぷかぶるーむ"))
            .setStatus(OnlineStatus.ONLINE)
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
              GatewayIntent.GUILD_EXPRESSIONS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(
                new ReadyListener(), // recognizes when the bot is ready
                new CheckContainsDiscordURL(), // check if the message contains a discord url
                EventLogger.getInstance(), // logger
                new EventListenerForTTS(), // refreshed voice event handler
                new SlashCommandRegisteration())
            .build();
        logger.info("JDA was successfully built.");
      } catch (InvalidTokenException e) {
        logger.error("Failed to login.", e);
        throw new RuntimeException(e);
      }
    }, executorService).join();
  }

  public static String getStaticPrefix() {
    return prefix;
  }

  public static void setStaticPrefix(String param) {
    prefix = param;
  }
}
