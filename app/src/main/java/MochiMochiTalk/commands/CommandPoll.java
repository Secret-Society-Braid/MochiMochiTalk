package MochiMochiTalk.commands;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Joiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

// !!poll <question> <duration> <timeunit> <option...>
public class CommandPoll extends ListenerAdapter {

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(CommandPoll.class);

    private static final ScheduledExecutorService serv = Executors.newSingleThreadScheduledExecutor(new CountingThreadFactory(() -> "MochiMochiTalk", "Poll Timer Thread"));

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        User author = event.getAuthor();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();
        
        if(author.isBot())
            return;
        

        String[] args = content.split(" ");
        if(args.length < 6) {
            channel.sendMessage("Invalid usage. Usage: !poll <question> <duration> <timeunit> <option...>").queue();
            LOG.warn("Invalid message: by: {} in: {}", author, channel);
        }

        String question = args[1];
        String duration = args[2];
        String timeunit = args[3];
        String[] options = new String[args.length - 4];
        System.arraycopy(args, 4, options, 0, options.length);

        // create poll
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle(question);
        String optionsString = Joiner.on("\n").join(options);
        builder.setColor(guild.getSelfMember().getColor());
        builder.setDescription(optionsString);
        channel.sendMessageEmbeds(builder.build()).queue(m -> {
            for(int i = 0; i < options.length; i++) {
                m.addReaction(createEmojiFromString("U+003" + (i + 1))).queue();
            }
            m.delete().queueAfter(Integer.parseInt(duration), TimeUnit.valueOf(timeunit), serv);
        });
    }

    private static Emoji createEmojiFromString(String str) {
        return Emoji.fromUnicode(str);
    }
    
}
