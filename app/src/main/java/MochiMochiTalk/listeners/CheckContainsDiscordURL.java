package MochiMochiTalk.listeners;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CheckContainsDiscordURL extends ListenerAdapter {

    // Logger
    private Logger logger = LoggerFactory.getLogger(CheckContainsDiscordURL.class);

    // constants
    private static final String REGEX_STRING = "https://discord.com/channels/\\d*/\\d*/\\d*";
    private static final Pattern PATTERN = Pattern.compile(REGEX_STRING);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
	Guild guild = event.getGuild();
	Message message = event.getMessage();
	User author = message.getAuthor();
	MessageChannel channel = event.getChannel();

	if(author.isBot()) {
	    return;
	}

	Matcher m = PATTERN.matcher(message.getContentRaw());
	if(!m.find()) {
	    return;
	} else {
	    this.logger.info("recieved discord message url");
	    this.logger.info("starting to parse...");
	    String url = m.group();
	    String data = url.substring(29);
	    String[] ids = data.split("/");
	    String guildID = ids[0];
	    String channelID = ids[1];
	    String messageID = ids[2];
	    if(guild.getId().equals(guildID)) {
		this.logger.info("detected that both are the same origin.");
		this.logger.info("analyzing...");
		MessageChannel requestedChannel = guild.getTextChannelById(channelID);
		requestedChannel.retrieveMessageById(messageID).queue(response -> {
		    String avatarUrl = response.getAuthor().getAvatarUrl();
		    String authorName = response.getAuthor().getName();
		    String content = response.getContentRaw();
		    String postedChannelName = response.getChannel().getName();
		    String postedDate = response.getTimeCreated().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
		    this.logger.info("author's avatar url : {}", avatarUrl);
		    this.logger.info("author name : {}", authorName);
		    this.logger.info("content body : {}", content);
		    this.logger.info("posted channel : {}", postedChannelName);
		    this.logger.info("posted date : {}", postedDate);
		    EmbedBuilder builder = new EmbedBuilder();
		    builder.setAuthor(authorName, null, avatarUrl);
		    builder.setDescription(content);
		    builder.setFooter(postedChannelName + " - " + postedDate);
		    channel.sendMessageEmbeds(builder.build()).queue();
		}, failure -> {
		    this.logger.warn("Exception while processing and parsing message.", failure);
		    EmbedBuilder builderOnException = new EmbedBuilder();
		    builderOnException.setTitle(failure.getClass().getName());
		    builderOnException.addField("Localized Message", failure.getMessage(), false);
		    builderOnException.setFooter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		    channel.sendMessageEmbeds(builderOnException.build()).queue();
		});
	    }
	}
    }

}
