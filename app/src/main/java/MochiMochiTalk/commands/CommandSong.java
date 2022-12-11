package MochiMochiTalk.commands;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import hajimeapi4j.api.endpoint.EndPoint;
import hajimeapi4j.api.endpoint.MusicEndPoint;
import hajimeapi4j.internal.builder.MusicEndPointBuilder;
import hajimeapi4j.util.enums.MusicParameter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
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
            earlyReplyFuture.thenAcceptBothAsync(
                builder.build().submit(),
                (earlyReplyInteraction, response) -> {
                    earlyReplyInteraction.editOriginalEmbeds();
                }
            );
        }
    }

    private static MessageEmbed createSongDetailMessage(MusicEndPoint response) {
        EmbedBuilder builder = new EmbedBuilder();
        builder
            .setTitle(String.format("ID:%d の楽曲情報", response.getSongId()), response.getLink())
            .setDescription("ブラウザでこの情報を見るにはこのメッセージのタイトルをクリック")
            .addField("楽曲名", response.getName(), false);
        
        return builder.build();
    }

    private static EmbedBuilder setInheritListedInformation(EmbedBuilder target, List<? extends EndPoint> informations) {
        return target;
    }
}
