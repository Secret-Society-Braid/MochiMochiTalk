package MochiMochiTalk.lib.global.types;

import MochiMochiTalk.lib.global.InvokeMethod;
import java.util.Objects;
import lombok.Data;

@Data
public class ResponseSchema {

  private String invokeMethod;
  private boolean exist;
  private boolean update;

  private static ResponseSchema EMPTY;

  public ResponseSchema(String invokeMethod, boolean exist, boolean update) {
    this.invokeMethod = invokeMethod;
    this.exist = exist;
    this.update = update;
  }

  public ResponseSchema(InvokeMethod invokeMethod, boolean exist, boolean update) {
    this.invokeMethod = invokeMethod.toString();
    this.exist = exist;
    this.update = update;
  }

  public static ResponseSchema createEmpty() {
    if (Objects.isNull(EMPTY)) {
      EMPTY = new ResponseSchema("unknown", false, false);
    }
    return EMPTY;
  }
}
