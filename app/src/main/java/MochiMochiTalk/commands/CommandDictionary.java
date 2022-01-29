package MochiMochiTalk.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MochiMochiTalk.App;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandDictionary extends ListenerAdapter {
    
    private Logger logger = LoggerFactory.getLogger(CommandDictionary.class);
    private Map<String, String> dictionary = new HashMap<>();
    private final String DICPATH = "dictionary.json";
    

    public CommandDictionary() {
        if(Files.notExists(Paths.get(DICPATH))) {
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
        if(author.isBot()) {
            return;
        }
        if(content.startsWith(App.prefix + "dic")) {
            String[] split = content.split(" ");
            if(split.length == 3) {
                String word = split[1];
                String meaning = split[2];
                Map<String, String> dic = readDic();
                if(split[1].equals("del")) {
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
            } else if(split.length == 2) {
                String word = split[1];
                Map<String, String> dic = readDic();
                if(word.equals("show")) {
                    StringBuilder sb = new StringBuilder("```登録されている読み方一覧：\n");
                    for(String key : dic.keySet()) {
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
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
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

}
