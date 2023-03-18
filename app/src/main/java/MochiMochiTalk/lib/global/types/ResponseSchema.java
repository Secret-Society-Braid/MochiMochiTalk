package MochiMochiTalk.lib.global.types;

import lombok.Data;

@Data
public class ResponseSchema {

  private String invokeMethod;
  private boolean exist;
  private boolean update;

  public ResponseSchema(String invokeMethod, boolean exist, boolean update) {
    this.invokeMethod = invokeMethod;
    this.exist = exist;
    this.update = update;
  }
}
