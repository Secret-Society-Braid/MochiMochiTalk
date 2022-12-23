/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package MochiMochiTalk;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.commands.CommandChangePrefix;
import MochiMochiTalk.commands.CommandDebugMode;
import MochiMochiTalk.commands.CommandDictionary;
import MochiMochiTalk.commands.CommandHelp;
import MochiMochiTalk.commands.CommandPing;
import MochiMochiTalk.commands.CommandReport;
import MochiMochiTalk.commands.CommandShowLicense;
import MochiMochiTalk.commands.CommandShutdown;
import MochiMochiTalk.commands.CommandSong;
import MochiMochiTalk.commands.CommandWhatsNew;
import MochiMochiTalk.commands.SlashCommandRegisteration;
import MochiMochiTalk.lib.FileReadThreadImpl;
import MochiMochiTalk.listeners.CheckContainsDiscordURL;
import MochiMochiTalk.listeners.EventLogger;
import MochiMochiTalk.listeners.ReadyListener;
import MochiMochiTalk.voice.nvoice.EventListenerForTTS;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class App {

    private static String token = "";
    private static Logger logger = LoggerFactory.getLogger(App.class);

    private static String prefix = "";

    public static void main(String[] args) {
	logger.info("Hello, world!");
	FileReadThreadImpl fileReadThread = new FileReadThreadImpl();
	fileReadThread.run();
	while (!fileReadThread.getFlag()) {
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		logger.error("Error: ", e);
		Thread.currentThread().interrupt();
	    }
	    logger.debug("Waiting for file read thread to finish.");
	}
	token = fileReadThread.getToken();
	prefix = fileReadThread.getPrefix();
	logger.info("token: {}", token);
	logger.info("prefix: {}", prefix);
	JDABuilder builder = JDABuilder.createDefault(token);
	logger.info("TOKEN was successfully set.");
	try {
	builder.disableCache(CacheFlag.MEMBER_OVERRIDES)
	    .setBulkDeleteSplittingEnabled(false)
	    .setActivity(Activity.competing("ぷかぷかぶるーむ"))
	    .setStatus(OnlineStatus.ONLINE)
		.enableIntents(GatewayIntent.GUILD_MESSAGES,
			GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
			GatewayIntent.GUILD_VOICE_STATES,
			GatewayIntent.GUILD_MESSAGE_REACTIONS,
			GatewayIntent.GUILD_MEMBERS,
			GatewayIntent.MESSAGE_CONTENT
		)
	    .addEventListeners(
		    new ReadyListener(), // recognizes when the bot is ready
		    // new VoiceEventListener(), // Event for text-to-speech
		    new CommandPing(), // ping command
		    new CommandHelp(), // help command
		    new CommandReport(), // report command
		    new CommandChangePrefix(), // change prefix command
		    CommandDictionary.getInstance(), // dictionary command
		    CommandWhatsNew.getInstance(), // whats new command
		    new CheckContainsDiscordURL(), // check if the message contains a discord url
            new CommandSong(), //  song information command
			new CommandShutdown(), // shutdown command
			EventLogger.getInstance(), // logger
			new EventListenerForTTS(), // refreshed voice event handler
			new SlashCommandRegisteration(), // registering slash commands
			new CommandDebugMode(), // handle debug mode
			new CommandShowLicense() // show license information
		    )
	    .build();
	    logger.info("JDA was successfully built.");
	} catch (InvalidTokenException e) {
	    logger.error("Failed to login.", e);
	}
    }

	public static String getStaticToken() {
		return token;
	}

	public static void setStaticPrefix(String param) {
		prefix = param;
	}

	public static String getStaticPrefix() {
		return prefix;
	}
}
