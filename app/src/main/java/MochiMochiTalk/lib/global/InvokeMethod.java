package MochiMochiTalk.lib.global;

public enum InvokeMethod {

  SEARCH_GUILD("searchGuild"),

  APPEND_INFORMATION("appendInformation"),

  DELETE_ROW("deleteRow");

  final String value;

  InvokeMethod(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
