package MochiMochiTalk.mocks;

import com.google.api.client.util.Strings;
import com.google.common.hash.Hashing;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheSeparatelyMock {

  private static final String ALPHABETS = "ABCDEFGHIJKLNMOPQRSTUVWXTZabcdefghijklnmopqrstuvwxyz";

  private CacheSeparatelyMock() { /* do nothing */}

  public static Map<String, byte[]> generateBytes(int length) {
    final Random random = new Random(System.currentTimeMillis());
    char[] chars = new char[length];
    for (int i = 0; i < length; i++) {
      int index = random.nextInt(ALPHABETS.length());
      chars[i] = ALPHABETS.charAt(index);
    }
    String res = new String(chars);
    byte[] resByte = res.getBytes(StandardCharsets.UTF_8);
    return Map.of(res, resByte);
  }

  public static byte[] readBytes(String name) throws IOException {
    if (Strings.isNullOrEmpty(name)) {
      throw new IllegalArgumentException("name must not be null");
    }
    String hashedName = Hashing.sha256().newHasher().putString(name, StandardCharsets.UTF_8).hash()
        .toString();
    byte[] bytes = null;
    try (FileInputStream fis = new FileInputStream(hashedName)) {
      ObjectInputStream ois = new ObjectInputStream(fis);
      bytes = (byte[]) ois.readObject();
    } catch (ClassCastException | ClassNotFoundException e) {
      log.error("error while reading objects", e);
    }
    return bytes;
  }

  public static boolean writeBytes(Map<String, byte[]> data) {
    if (data == null) {
      throw new IllegalArgumentException("data must not be null");
    }
    if (data.size() != 1) {
      log.warn("There are more than one data. will use first element");
    }
    String name = data.keySet().iterator().next();
    String hashedName = Hashing.sha256().newHasher().putString(name, StandardCharsets.UTF_8).hash()
        .toString();
    byte[] bytes = data.get(name);
    try (FileOutputStream fos = new FileOutputStream(hashedName)) {
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(bytes);
    } catch (IOException e) {
      log.error("error while writing object", e);
      return false;
    }
    return true;
  }
}
