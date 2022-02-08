package MochiMochiTalk.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import HajimeAPI4J.api.HajimeAPI4J.List_Params;
import HajimeAPI4J.api.HajimeAPI4J.List_Type;
import HajimeAPI4J.api.HajimeAPI4J.Music_Params;
import HajimeAPI4J.api.HajimeAPI4J.Token;
import HajimeAPI4J.api.HajimeAPIBuilder;
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
    private CompletableFuture<JsonNode> hajimeApiFuture = null;
    private boolean isDigit;
    private String data;
    
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
            if(split.length >= 2) {
                HajimeAPIBuilder builder = HajimeAPIBuilder.create();
                StringBuilder sb = new StringBuilder();
                if(split.length == 2)
                    data = split[1];
                else {
                    for(String tmp : Arrays.copyOfRange(split, 1, split.length))
                        sb.append(tmp).append(" ");
                    sb.deleteCharAt(sb.length() - 1);
                    data = sb.toString();
                }
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
                hajimeApiFuture = builder.build().getAsync();
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
                        List<Map<String, Object>> list;
                        try {
                            list = new ObjectMapper().readValue(node.traverse(), new TypeReference<List<Map<String, Object>>>() {});
                        } catch (IOException e) {
                            logger.error("Error while traversing json", e);
                            response.delete().queue();
                            return;
                        }
                        int size = list.size();
                        Map<String, Object> mostRelated = new HashMap<>(list.get(0));
                        for(Map<String, Object> map : list) {
                            logger.info("map: {}", map);
                            if(map.get("name").toString().contains(data)) {
                                mostRelated = new LinkedHashMap<>(map);
                                break;
                            }
                        }
                        name = mostRelated.get("name").toString();
                        link = mostRelated.get("link").toString();
                        id = Integer.parseInt(mostRelated.get("song_id").toString());
                        api = mostRelated.get("api").toString();
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
                    logger.error("Failed to complete.", ex);
                    EmbedBuilder onException = new EmbedBuilder();
                    onException.setTitle(ex.getClass().getName())
                        .setDescription(ex.getMessage())
                        .addField("StackTrace", Arrays.toString(ex.getStackTrace()), false)
                        .addField("Caused by", ex.getCause() == null ? "null" : ex.getCause().getClass().getName(), false)
                        .addField("Cause StackTrace", ex.getCause() == null ? "null" : Arrays.toString(ex.getCause().getStackTrace()), false)
                        .addField("CAUTION", "このメッセージは20秒後に自動削除されます。", false)
                        .setColor(Color.RED);
                    channel.sendMessageEmbeds(onException.build()).queue(success -> {
                        success.delete().queueAfter(20, TimeUnit.SECONDS);
                    });
                });
            }
        } else if(content.equalsIgnoreCase(App.prefix + "song")) {
            channel.sendMessage("使用方法：" + App.prefix + "song (検索ワードもしくはふじわらはじめ楽曲DB内部管理ID)").queue();
        }
    }
}
