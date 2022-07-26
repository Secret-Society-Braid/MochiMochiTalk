package MochiMochiTalk.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandDictionary extends ListenerAdapter {

    private Logger logger = LoggerFactory.getLogger(CommandDictionary.class);
    private Map<String, String> dictionary = new HashMap<>();
    private static final String DICT_API_URL = "https://script.google.com/macros/s/AKfycbwqUjLTZrt1MkxR79dgLdnNh7uKTIUL9x0pZigrlFmXhshM4U6TuXIWXlZqXiAgvGot/exec";
    private static final String DICPATH = "dictionary.json";
    private static CommandDictionary singleton;

    public static CommandDictionary getInstance() {
        if (singleton == null)
            singleton = new CommandDictionary();
        return singleton;
    }

    public CommandDictionary() {
        if (Files.notExists(Paths.get(DICPATH))) {
            logger.info("dictionary.json is not found. Create new dictionary.json.");
            Map<String, String> initMap = new HashMap<>();
            initMap.put("IDOLM@STER", "アイドルマスター");
            writeDic(initMap);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String content = message.getContentRaw();
        User author = event.getAuthor();
        MessageChannel channel = event.getChannel();
        if (author.isBot()) {
            return;
        }
        if (content.startsWith(App.prefix + "dic")) {
            String[] split = content.split(" ");
            if (split.length == 3) {
                String word = split[1];
                String meaning = split[2];
                Map<String, String> dic = readDic();
                if (split[1].equals("del")) {
                    dic.remove(split[2]);
                    writeDic(dic);
                    channel.sendMessageFormat("読み方を削除しました: %s", split[2]).queue();
                    logger.info("Deleted word: {}", split[2]);
                } else {
                    dic.put(word, meaning);
                    writeDic(dic);
                    channel.sendMessageFormat("読み方を更新しました： %s -> %s", word, meaning).queue();
                    logger.info("dic updated: {} -> {}", word, meaning);
                }
            } else if (split.length == 2) {
                String word = split[1];
                Map<String, String> dic = readDic();
                if (word.equals("show")) {
                    StringBuilder sb = new StringBuilder("```登録されている読み方一覧：\n");
                    for (String key : dic.keySet()) {
                        sb.append(key).append(" -> ").append(dic.get(key)).append("\n");
                    }
                    sb.append("```");
                    channel.sendMessage(sb.toString()).queue();
                }
            } else {
                channel.sendMessage("読み方を更新するには2つの引数が必要です。もしくは「dic show」で登録されている全ての読み方を表示します。").queue();
            }
        }
    }

    public void writeDic(Map<String, String> dictionary) {
        ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(Paths.get(DICPATH).toFile(), dictionary);
        } catch (IOException e) {
            logger.error("Failed to write file.", e);
        }
    }

    public Map<String, String> readDic() {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
        };
        try {
            return mapper.readValue(Paths.get(DICPATH).toFile(), typeRef);
        } catch (IOException e) {
            logger.error("Failed to read file.", e);
        }
        return Collections.emptyMap();
    }

    public static Map<String, String> getDictionary() {
        return new CommandDictionary().readDic();
    }

    public void updateDic(String field, String dict) throws IOException {
        Map<String, String> data = Map.of(field, dict);
        String sttringify = new ObjectMapper().writeValueAsString(data);
        URL url = new URL(DICT_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        OutputStream outStream = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream, StandardCharsets.UTF_8));
        writer.write(sttringify);
        writer.flush();
        writer.close();
        outStream.close();
        conn.connect();
        logger.debug("stringify data: {}", sttringify);
        logger.debug("statuscode: {}", conn.getResponseCode());
        logger.debug("connection data: {}", conn);
        conn.disconnect();
    }

    public Optional<String> getDict(String field) {
        URL url;
        try {
            url = new URL(DICT_API_URL + "?field=" + field);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            JsonNode res = new ObjectMapper().readTree(conn.getInputStream());
            logger.info("fetch complete: {}", res);
            return Optional.ofNullable(res.get("response").get("dict").asText());
        } catch (IOException e) {
            logger.error("Failed to fetch dictionary", e);
        }
        return Optional.empty();
    }

}
