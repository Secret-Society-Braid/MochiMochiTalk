package MochiMochiTalk.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import MochiMochiTalk.lib.JsonFileReadUtil;
import MochiMochiTalk.lib.datatype.LicenseData;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.concurrent.CountingThreadFactory;

@Slf4j
public class CommandShowLicense extends ListenerAdapter {
    
    private static CompletableFuture<List<LicenseData>> licenses;
    private static ExecutorService serv = Executors.newCachedThreadPool(
        new CountingThreadFactory(() -> "MochiMochiTalk", "license file fetch thread")
    );

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if(!event.getName().equals("license"))
            return;
        log.info("showing license data...");
            CompletableFuture<List<LicenseData>> licenseFuture = fetchLicenseData();
        licenseFuture
            .thenApplyAsync(CommandShowLicense::constructLicenseEmbed, serv)
            .thenAcceptAsync(event::replyEmbeds, serv)
            // TODO: make this implementation global so that we can use this anywhere.
            .whenCompleteAsync((ret, ex) -> {
                if(ex == null) {
                    log.info("the command interaction [showLicense] has been finished successfully");
                    return;
                }
                log.warn("There was an exception while invoking [showLicense] event handling.", ex);
                log.warn("constructing information message for devs...");
                EmbedBuilder builder = new EmbedBuilder();
                final String exceptionClassName = ex.getClass().getSimpleName();
                log.warn("The exception that was encountered: {}", exceptionClassName);
                final String exceptionMessage = ex.getMessage();
                log.warn("The message that was sent: {}", exceptionMessage);
                final String exceptionStackTrace = Arrays.toString(ex.getStackTrace());
                log.warn("Got stack trace.");
                builder.setTitle("イベント処理中に例外が発生しました")
                    .addField("例外", exceptionClassName, false)
                    .addField("例外メッセージ", (exceptionMessage == null ? "null" : exceptionMessage), false)
                    .addField("スタックトレース", exceptionStackTrace, false);
                event.getJDA()
                    .getUserById("399143446939697162")
                    .openPrivateChannel()
                    .submit()
                    .thenComposeAsync(channel -> channel.sendMessageEmbeds(builder.build()).submit(), serv);
            }, serv);
    }

    @Nonnull
    private static synchronized CompletableFuture<List<LicenseData>> fetchLicenseData() {
        if(licenses == null) {
            licenses = CompletableFuture.supplyAsync(JsonFileReadUtil::getLicenseData, serv);
        }
        return Objects.requireNonNull(licenses);
    }
    
    private static synchronized MessageEmbed constructLicenseEmbed(List<LicenseData> data) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("ライセンス一覧");
        data.parallelStream().forEach(each -> builder
            .addField(each.getDeps(), each.getUrl(), false)
            .addField("説明", each.getDescription() , false)
            .addField("ライセンス名", each.getLicense(), false)
        );
        return builder.build();
    }
}
