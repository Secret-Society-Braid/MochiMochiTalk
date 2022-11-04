package MochiMochiTalk.lib.datatype;

import lombok.Data;

@Data(staticConstructor = "of")
public class LicenseData {
    private final String deps;
    private final String description;
    private final String license;
    private final String url;
}
