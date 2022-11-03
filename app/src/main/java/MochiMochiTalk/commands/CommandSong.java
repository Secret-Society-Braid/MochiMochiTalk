package MochiMochiTalk.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import HajimeAPI4J.api.HajimeAPI4J;
import HajimeAPI4J.api.HajimeAPI4J.List_Params;
import HajimeAPI4J.api.HajimeAPI4J.List_Type;
import HajimeAPI4J.api.HajimeAPI4J.Music_Params;
import HajimeAPI4J.api.HajimeAPI4J.Production;
import HajimeAPI4J.api.HajimeAPI4J.Token;
import HajimeAPI4J.api.HajimeAPIBuilder;
import MochiMochiTalk.App;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

// no fixes need for potentially null access
public class CommandSong extends ListenerAdapter {

    // logger
    private Logger logger = LoggerFactory.getLogger(CommandSong.class);


    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = message.getAuthor();
        AtomicBoolean isDigit = new AtomicBoolean(false);

        if (author.isBot()) {
            return;
        }

        if (content.startsWith(App.getStaticPrefix() + "song ")) {
            logger.info("Guild: {}", guild);
            logger.info("channel: {}", channel);
            logger.info("message: {}", message);
            String[] split = content.split(" ");
            String data;
            if (split.length >= 2) {
                HajimeAPIBuilder builder = null;
                StringBuilder sb = new StringBuilder();
                if (split.length == 2)
                    data = split[1];
                else {
                    for (String tmp : Arrays.copyOfRange(split, 1, split.length))
                        sb.append(tmp).append(" ");
                    sb.deleteCharAt(sb.length() - 1);
                    data = sb.toString();
                }
                if (!data.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    for (int i = 0; i < data.length(); i++) {
                        if (Character.isDigit(data.charAt(i))) {
                            isDigit.set(true);
                            break;
                        }
                    }
                }
                if (isDigit.get()) {
                    builder = HajimeAPIBuilder.createDefault(Token.MUSIC)
                            .addParameter(Music_Params.ID, data);
                } else {
                    builder = HajimeAPIBuilder.createDefault(Token.LIST)
                            .addParameter(List_Params.TYPE, List_Type.MUSIC.toString())
                            .addParameter(List_Params.SEARCH, data);
                }
                CompletableFuture<JsonNode> hajimeApiFuture = builder.build().getAsync();
                CompletableFuture<Message> sendMessageFuture = channel
                        .sendMessage("楽曲情報APIのレスポンスを待っています……(Powered by ふじわらはじめAPI)").submit();
                sendMessageFuture.thenAcceptBothAsync(hajimeApiFuture, (response, node) -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    String name = "";
                    String link = "";
                    String api = "";
                    int id = 0;
                    if (isDigit.get()) {
                        name = Objects.requireNonNull(node.get("name").asText());
                        link = Objects.requireNonNull(node.get("link").asText());
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
                            list = new ObjectMapper().readValue(node.traverse(),
                                    new TypeReference<List<Map<String, Object>>>() {
                                    });
                        } catch (IOException e) {
                            logger.error("Error while traversing json", e);
                            response.delete().queue();
                            return;
                        }
                        int size = list.size();
                        Map<String, Object> mostRelated = new HashMap<>(list.get(0));
                        for (Map<String, Object> map : list) {
                            logger.info("map: {}", map);
                            if (map.get("name").toString().contains(data)) {
                                mostRelated = new LinkedHashMap<>(map);
                                break;
                            }
                        }
                        name = Objects.requireNonNull(mostRelated.get("name").toString());
                        link = Objects.requireNonNull(mostRelated.get("link").toString());
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
                        response.editMessageFormat("指定されたワードを含む情報が %d 件見つかりました。最も関連性が高いものを表示します...", size)
                                .queue(success -> {
                                    success.editMessageEmbeds(embedBuilder.build()).queue();
                                });
                    }
                }).whenCompleteAsync((ret, ex) -> {
                    if (ex == null) {
                        logger.info("Successfully completed.");
                        return;
                    }
                    logger.error("Exception while interacting with fujiwara hajime API", ex);
                    channel.sendMessageFormat("楽曲情報APIからの情報取得中にエラーが発生しました。\nこのエラーを報告する場合は以下のメッセージを一緒に伝えてください\n%s", ex.getMessage());
                });
            }
        } else if (content.equalsIgnoreCase(App.getStaticPrefix() + "song")) {
            channel.sendMessage("使用方法：" + App.getStaticPrefix() + "song (検索ワードもしくはふじわらはじめ楽曲DB内部管理ID)").queue();
        } else {
            /* do nothing */
        }
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (event.getName().equals("song")) {
            CompletableFuture<InteractionHook> replyFuture =  event.reply("検索を開始します。少々お待ちください...( Powered by FujiwaraHajime Song Database)").submit();
            final String varId = "id";
            final String varKeyword = "keyword";
            CompletableFuture<JsonNode> nodeFuture;
            String subCommandName = Objects.requireNonNull(event.getSubcommandName());
            if (subCommandName.equals(varId))
                nodeFuture = getSongDetailWithInternalId(event.getOption(varId, OptionMapping::getAsString));
            else if (subCommandName.equals(varKeyword))
                nodeFuture = getSongDetailWithKeyword(event.getOption(varKeyword, OptionMapping::getAsString));
            else
                return;

            MessageEmbed result;
            if (subCommandName.equals(varId))
                result = generateSongDetailMessage(nodeFuture.join());
            else
                result = generateFetchSongListMessage(event.getOption(varKeyword, OptionMapping::getAsString), nodeFuture.join());
            
            replyFuture.thenAcceptAsync(hook -> hook.editOriginal("取得完了。表示します…").queue(suc -> suc.getChannel().sendMessageEmbeds(Objects.requireNonNull(result)).queue()));
        }
    }

    private static MessageEmbed generateSongDetailMessage(JsonNode detail) {
        // declare detail vars
        String songName = detail.get("name").asText();
        String songId = detail.get("song_id").asText();
        String link = detail.get("link").asText();
        JsonNode remixNode = detail.get("remix");
        JsonNode originalNode = detail.get("original");
        JsonNode lyricsNode = detail.get("lyrics");
        JsonNode composerNode = detail.get("composer");
        JsonNode arrangeNode = detail.get("arrange");
        JsonNode memberNode = detail.get("member");

        // future vars if needed
        CompletableFuture<JsonNode> remixFuture = null;
        CompletableFuture<JsonNode> originalFuture = null;

        if (remixNode != null) {
            HajimeAPIBuilder builder = HajimeAPIBuilder.createDefault(Token.MUSIC)
                    .addParameter(Music_Params.ID, remixNode.get("song_id").asText());
            HajimeAPI4J api = builder.build();
            api.setURI(Objects.requireNonNull(remixNode.get("api").asText()));
            api.setURI(remixNode.get("api").asText());
            remixFuture = api.getAsync();
        }
        if (originalNode != null) {
            HajimeAPIBuilder builder = HajimeAPIBuilder.createDefault(Token.MUSIC)
                    .addParameter(Music_Params.ID, originalNode.get("song_id").asText());
            HajimeAPI4J api = builder.build();
            api.setURI(originalNode.get("api").asText());
            originalFuture = api.getAsync();
        }

        // construct embed
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("内部ID ( " + songId + " ) の楽曲情報", link);
        builder.setDescription("このメッセージのタイトル部分をクリックすることで楽曲DB内のページへ飛びます");
        builder.addField("楽曲名", songName, false);
        builder.addField("楽曲DB内部管理ID", songId, false);

        StringBuilder composerBuilder = new StringBuilder();
        composerNode.forEach(each -> composerBuilder.append(each.get("name").asText()).append(","));
        StringBuilder lyricsBuilder = new StringBuilder();
        lyricsNode.forEach(each -> lyricsBuilder.append(each.get("name").asText()).append(","));
        StringBuilder arrangeBuilder = new StringBuilder();
        arrangeNode.forEach(each -> arrangeBuilder.append(each.get("name").asText()).append(","));
        composerBuilder.deleteCharAt(composerBuilder.length() - 1);
        lyricsBuilder.deleteCharAt(lyricsBuilder.length() - 1);
        arrangeBuilder.deleteCharAt(arrangeBuilder.length() - 1);

        builder.addField("作曲者", composerBuilder.toString(), false);
        builder.addField("作詞者", lyricsBuilder.toString(), false);
        builder.addField("編曲", arrangeBuilder.toString(), false);
        memberNode.forEach(member -> {
            builder.addField("歌唱メンバー", member.get("name").asText(), true);
        });
        if (remixNode != null) {
            builder.addField("リミックス楽曲", "あり", false);
            JsonNode remixSongNode = remixFuture.join();
            builder.addField("リミックスタイトル", remixSongNode.get("name").asText(), false);
            builder.addField("リミックス曲DBページ", remixSongNode.get("link").asText(), false);
        }
        if (originalNode != null) {
            JsonNode originalSongNode = originalFuture.join();
            builder.addField("リミックス元", originalSongNode.get("name").asText(), false);
            builder.addField("リミックス元曲DBページ", originalSongNode.get("link").asText(), false);
        }
        builder.setFooter(
                "ProDiary -swiss army knife for Producers- These information are powered by FujiwaraHajime Song DataBase");
        return builder.build();
    }

    private static MessageEmbed generateFetchSongListMessage(String keyword, JsonNode listNode) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(keyword + " の検索結果");
        builder.setDescription("詳細が見たい場合はリンクをクリックするか、かっこ内のIDにて「/song id」コマンドを使用してください。\n検索結果が6曲以上ある場合は5曲まで表示されています。");
        for (int i = 0; i < 5; i++) {
            JsonNode tmpNode = listNode.get(i);
            if(tmpNode == null)
                break;
            builder.addField((i + 1) + " 曲目", tmpNode.get("name").asText() + " ( 管理ID:" + tmpNode.get("song_id").asInt() + ") ",
                    false);
            builder.addField("リンク先", tmpNode.get("link").asText(), false);
        }
        builder.setFooter(
                "ProDiary -swiss army knife for Producers- These information are powered by FujiwaraHajime song Database");
        return builder.build();
    }

    private static CompletableFuture<JsonNode> getSongDetailWithInternalId(String id) {
        HajimeAPIBuilder builder = HajimeAPIBuilder.createDefault(Token.MUSIC)
                .addParameter(Music_Params.ID, id);
        HajimeAPI4J api = builder.build();
        return api.getAsync();
    }

    private static CompletableFuture<JsonNode> getSongDetailWithKeyword(String keyword) {
        HajimeAPIBuilder builder = HajimeAPIBuilder.createDefault(Token.LIST)
                .addParameter(List_Params.TYPE, List_Type.MUSIC.toString())
                .addParameter(List_Params.MUSIC_TYPE, Production.CG.toString())
                .addParameter(List_Params.SEARCH, keyword);
        HajimeAPI4J api = builder.build();
        return api.getAsync();
    }
}
