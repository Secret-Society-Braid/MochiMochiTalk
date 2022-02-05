package MochiMochiTalk.commands;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import HajimeAPI4J.api.HajimeAPI4J.List_Params;
import HajimeAPI4J.api.HajimeAPI4J.List_Type;
import HajimeAPI4J.api.HajimeAPI4J.Music_Params;
import HajimeAPI4J.api.HajimeAPI4J.Token;
import HajimeAPI4J.api.HajimeAPIBuilder;
import HajimeAPI4J.api.util.HajimeAPI4JImpl;
import HajimeAPI4J.api.util.parse.ParseList;
import MochiMochiTalk.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandSong extends ListenerAdapter {

    // logger
    private Logger logger = LoggerFactory.getLogger(CommandSong.class);
    private HajimeAPI4JImpl apiImpl = null;
    private CompletableFuture<JsonNode> hajimeApiFuture = null;
    private boolean isDigit;
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = message.getAuthor();

        if(author.isBot()) {
            return;
        }

        if(content.startsWith(App.prefix + "song ")) {
            logger.info("Guild: {}", guild);
            logger.info("channel: {}", channel);
            logger.info("message: {}", message);
            String[] split = content.split(" ");
            if(split.length == 2) {
                HajimeAPIBuilder builder = HajimeAPIBuilder.create();
                String data = split[1];
                isDigit = false;
                if(!data.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    for(int i = 0; i < data.length(); i++) {
                        if(Character.isDigit(data.charAt(i))) {
                            isDigit = true;
                            break;
                        }
                    }
                }
                if(isDigit) {
                    builder.setToken(Token.MUSIC)
                        .addParameter(Music_Params.ID, data);
                } else {
                    builder.setToken(Token.LIST)
                        .addParameter(List_Params.TYPE, List_Type.MUSIC.toString())
                        .addParameter(List_Params.SEARCH, data);
                }
                apiImpl = builder.build();
                hajimeApiFuture = apiImpl.getAsync();
                CompletableFuture<Message> sendMessageFuture = channel.sendMessage("楽曲情報APIのレスポンスを待っています……(Powered by ふじわらはじめAPI)").submit();
                sendMessageFuture.thenAcceptBothAsync(hajimeApiFuture, (response, node) -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    String name, link, api = "";
                    int id = 0;
                    if(isDigit) {
                        name = node.get("name").asText();
                        link = node.get("link").asText();
                        id = Integer.parseInt(data);
                        api = node.get("api").asText();
                        logger.info("name: {}", name);
                        logger.info("link: {}", link);
                        logger.info("id: {}", id);
                        logger.info("api: {}", api);
                        embedBuilder.setAuthor("HajimeAPI4J fetched from ふじわらはじめAPI", "https://fujiwarahaji.me/")
                            .addField("楽曲情報", name, false)
                            .addField("詳細情報", link, false)
                            .setColor(Color.CYAN);
                        response.editMessage("取得完了。表示します……").queue();
                        response.editMessageEmbeds(embedBuilder.build()).queue();
                    } else {
                        ParseList parsed = new ParseList(node);
                        int size = parsed.converse().asList().size();
                        Map<String, String> mostRelated = null;
                        for(Map<String, String> map : parsed.asList()) {
                            if(map.get("name").contains(data)) {
                                mostRelated = new HashMap<>(map);
                                logger.info("escaping detected name: {}", map.get("name"));
                            }
                        }
                        if(mostRelated == null) {
                            mostRelated = new HashMap<>(parsed.asList().get(0));
                            logger.info("detected name: {}", mostRelated.get("name"));
                        }
                        name = mostRelated.get("name");
                        link = mostRelated.get("link");
                        id = Integer.parseInt(mostRelated.get("song_id"));
                        api = mostRelated.get("api");
                        logger.info("name: {}", name);
                        logger.info("link: {}", link);
                        logger.info("id: {}", id);
                        logger.info("api: {}", api);
                        embedBuilder.setAuthor("HajimeAPI4J fetched from ふじわらはじめAPI", "https://fujiwarahaji.me/")
                            .addField("楽曲情報", name, false)
                            .addField("詳細情報", link, false)
                            .addField("API内部管理ID", id + "", false)
                            .setColor(Color.CYAN);
                        response.editMessageFormat("指定されたワードを含む情報が %d 件見つかりました。最も関連性が高いものを表示します...", size).queue(success -> {
                            success.editMessageEmbeds(embedBuilder.build()).queue();
                        });
                    }
                }).whenCompleteAsync((ret, ex) -> {
                    if(ex == null){
                        logger.info("Successfully completed.");
                        return;
                    }
                    logger.info("Failed to complete.", ex);
                    EmbedBuilder onException = new EmbedBuilder();
                    onException.setTitle(ex.getClass().toString())
                        .setDescription(ex.getMessage())
                        .addField("StackTrace", Arrays.toString(ex.getStackTrace()), false)
                        .addField("CAUTION", "このメッセージは15秒後に自動削除されます。", false)
                        .setColor(Color.RED);
                    channel.sendMessageEmbeds(onException.build()).queue(success -> {
                        success.delete().queueAfter(15, TimeUnit.SECONDS);
                    });
                });
            }
        } else if(content.equalsIgnoreCase(App.prefix + "song")) {
            channel.sendMessage("使用方法：" + App.prefix + "song (検索ワードもしくはふじわらはじめ楽曲DB内部管理ID)").queue();
        }
    }
}
