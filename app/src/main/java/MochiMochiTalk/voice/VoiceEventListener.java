package MochiMochiTalk.voice;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

import com.google.protobuf.Struct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class VoiceEventListener extends ListenerAdapter {
    
    private Logger logger = LoggerFactory.getLogger(VoiceEventListener.class);

    private MessageChannel channel = null;
    private AudioManager audioManager;
    private AudioSendHandler sendHandler;
    private AudioReceiveHandler receiveHandler;
    private SingHandler singHandler;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        logger.info("Message received: {}", event.getMessage().getContentRaw());
        User author = event.getAuthor();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        audioManager = event.getGuild().getAudioManager();
        sendHandler = audioManager.getSendingHandler();
        receiveHandler = audioManager.getReceivingHandler();

        // ignore messages from bots
        if (author.isBot()) {
            return;
        }

        if(content.equalsIgnoreCase("!!connect")) {
            logger.info("Connecting to voice channel.");
            onConnectCommand(event);
        }

        if(content.equalsIgnoreCase("!!disconnect")) {
            logger.info("Disconnecting from voice channel.");
            onDisconnectCommand(event);
        }

        if(channel != null && !content.startsWith("!!")) {
            logger.info("Analyzing message: {}", content);
            logger.info("Channel: {}", channel.getName());
            logger.info("Author: {}", author.getName());
            logger.info("Guild: {}", event.getGuild().getName());
            MessageBuilder builder = new MessageBuilder();
            builder.setTTS(true);
            builder.append("**" + author.getName() + "**さん: " + content)
                .build();
            channel.sendMessage(builder.build()).queue();
            message.delete().queue();/*
            ProcessBuilder pBuilder = new ProcessBuilder();
            Path path = Paths.get("").toAbsolutePath();
            String curAbsolutePath = path.toString() + "\\tmp.wav";
            pBuilder.directory(path.toFile());
            pBuilder.command("softalkw.exe", "/R:" + curAbsolutePath, "/W:" + content);
            try {
                Process process = pBuilder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                logger.warn("Failed to execute softalkw.exe", e);
            }
            Path wavPath = Paths.get(curAbsolutePath);
            byte[] wavBytes = fileToByteArray(wavPath.toString());
            singHandler.addQueue(wavBytes);
            singHandler.provide20MsAudio();
            try {
                Files.delete(wavPath);
            } catch (IOException e) {
                logger.warn("Failed to delete wav file", e);
            }*/
        }
    }

    private void connect(VoiceChannel channel) {
        Guild guild = channel.getGuild();
        audioManager = guild.getAudioManager();
        singHandler = new SingHandler();
        audioManager.setReceivingHandler(singHandler);
        audioManager.setSendingHandler(singHandler);
        audioManager.openAudioConnection(channel);
    }

    private void disconnect(VoiceChannel channel) {
        Guild guild = channel.getGuild();
        audioManager = guild.getAudioManager();
        audioManager.closeAudioConnection();
        
    }

    private void onConnecting(VoiceChannel voice, MessageChannel text) {
        text.sendMessage(voice.getName() + "で準備できました…！").queue();
        channel = text;
    }

    private void onDisconnecting(MessageChannel text) {
        text.sendMessage("終わりますか？お疲れ様でした…").queue();
        channel = null;
    }

    private void onConnectCommand(MessageReceivedEvent event) {
        MessageChannel text = event.getChannel();
        Member member = event.getMember();
        VoiceChannel voice = member.getVoiceState().getChannel();

        if (voice == null) {
            text.sendMessage("まだボイスチャンネルに入っていないみないです。プロデューサーさん").queue();
            return;
        }

        connect(voice);
        onConnecting(voice, event.getChannel());
    }

    private void onDisconnectCommand(MessageReceivedEvent event) {
        MessageChannel text = event.getChannel();
        Member member = event.getMember();
        VoiceChannel voice = member.getVoiceState().getChannel();

        if(voice == null) {
            text.sendMessage("まだボイスチャンネルに入っていないみないです。プロデューサーさん").queue();
            return;
        }

        disconnect(voice);
        onDisconnecting(event.getChannel());
    }

    public static byte[] fileToByteArray(String name){
        Path path = Paths.get(name);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
