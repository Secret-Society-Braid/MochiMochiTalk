package MochiMochiTalk.commands;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import com.google.api.client.util.Strings;

import hajimeapi4j.api.endpoint.EndPoint;
import hajimeapi4j.api.endpoint.MusicEndPoint;
import hajimeapi4j.internal.builder.MusicEndPointBuilder;
import hajimeapi4j.util.enums.MusicParameter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

// no fixes need for potentially null access
@Slf4j
public class CommandSong extends ListenerAdapter {

    private static final Executor concurrentExecutor = Executors.newCachedThreadPool(
        new CountingThreadFactory(() -> "MochiMochiTalk", "Song detail integration thread", true));

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        // early return if not the right command
        if(!event.getName().equals("song")) return;
        CompletableFuture<InteractionHook> earlyReplyFuture = event.reply("検索を開始します…お待ちください。（Powered by ふじわらはじめAPI）").submit();
        final String subCommandName = Objects.requireNonNull(event.getSubcommandName());
        log.info("got interaction with following command: {} in {}", subCommandName, event.getName());
        if(subCommandName.equals("id")) {
            int id = event.getOption(subCommandName, () -> -1 ,OptionMapping::getAsInt);
            if(id == -1) {
                InteractionHook hook = event.getHook();
                earlyReplyFuture.thenAcceptBothAsync(
                    hook.sendMessage("IDが指定されていない可能性があります。処理を中止しました。").submit(),
                    (earlyReplyInteractionIgnore, errorMessageIgnore) -> log.warn("it seems user {} specified no id. this log is for unintended behavior recording purpose.", event.getUser()),
                    concurrentExecutor);
                return;
            }
            MusicEndPointBuilder builder = MusicEndPointBuilder.createWith(id);
            builder.setHide(
                MusicParameter.Hide.CD_MEMBER,
                MusicParameter.Hide.LIVE_MEMBER
            );
            CompletableFuture<Message> sendEmbedFuture = earlyReplyFuture.thenCombineAsync(
                builder.build().submit(),
                (earlyReplyInteraction, response) -> earlyReplyInteraction.editOriginalEmbeds(createSongDetailMessage(response)).complete(),
                concurrentExecutor);
            sendEmbedFuture.whenCompleteAsync(
                (ret, ex) -> {
                    // TODO: write handling code
                }
            );
        }
    }

    private static MessageEmbed createSongDetailMessage(MusicEndPoint response) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
            .setTitle(String.format("ID:%d の楽曲情報", response.getSongId()), response.getLink())
            .setDescription("ブラウザでこの情報を見るにはこのメッセージのタイトルをクリック")
            .addField("楽曲名", response.getName(), false)
            .setFooter("MochiMochiTalk Song detail integration powered by ふじわらはじめAPI");
        setInheritListedInformation(builder, response.getComposer().orElse(Collections.emptyList()), "作曲者名");
        setInheritListedInformation(builder, response.getLyrics().orElse(Collections.emptyList()), "作詞者名");
        setInheritListedInformation(builder, response.getArrange().orElse(Collections.emptyList()), "編曲者名");
        setInheritListedInformation(builder, response.getMember(), "歌唱メンバー");
        return builder.build();
    }

    private static void setInheritListedInformation(EmbedBuilder target, List<? extends EndPoint> information, @Nonnull String fieldTitle) {
        if(information.isEmpty())
            return;
        if(Strings.isNullOrEmpty(fieldTitle))
            return;
        information
            .parallelStream()
            .map(EndPoint::getName)
            .filter(Objects::nonNull)
            .forEach(name -> target.addField(fieldTitle, Objects.requireNonNull(name), false));
    }
}
