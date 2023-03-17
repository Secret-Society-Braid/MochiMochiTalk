package MochiMochiTalk.util;

import java.util.Set;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordServerOperatorUtil {

  private static final String BOT_DEV_USER_ID = "399143446939697162";

  private static final Set<String> MUTSUCORD_OPERATOR_USERS = Set.of(
      BOT_DEV_USER_ID,
      "666213020653060096",
      "682079802605174794",
      "365695324947349505",
      "492145462908944422",
      "538702103372103681",
      "482903571625410560",
      "686286747084390433",
      "706819045286215765");

  private static final Set<String> TTS_OPERATIVE_SERVERS = Set.of(
      "629249258960453652",
      "649603185115267092");

  public static String getBotDevUserId() {
    return BOT_DEV_USER_ID;
  }

  @Contract(pure = true)
  public static boolean isMutsucordOperator(@Nonnull String userId) {
    return MUTSUCORD_OPERATOR_USERS.contains(userId);
  }

  @Contract(pure = true)
  public static boolean isTtsOperativeServer(@Nonnull String serverId) {
    return TTS_OPERATIVE_SERVERS.contains(serverId);
  }
}
