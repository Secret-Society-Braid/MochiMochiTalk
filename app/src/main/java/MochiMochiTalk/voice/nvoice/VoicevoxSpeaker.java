package MochiMochiTalk.voice.nvoice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString
public class VoicevoxSpeaker {

  private final static ObjectMapper MAPPER = new ObjectMapper();
  private final static String VOICEVOX_LEGAL_NOTE_RESOURCE = "voicevox_legal.json";
  private static List<LegalNote> legalNotes = null;

  private final String name;
  @JsonProperty("speaker_uuid")
  private final String speakerUuid;
  private final List<Style> styles;
  private final String version;
  @JsonProperty("supported_features")
  private final SupportedFeatures supportedFeatures;

  private static void loadLegalNotes() {
    try {
      legalNotes = List.of(MAPPER.readValue(
        VoicevoxSpeaker.class.getResourceAsStream(VOICEVOX_LEGAL_NOTE_RESOURCE),
        MAPPER.getTypeFactory().constructCollectionType(List.class, LegalNote.class)));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load legal notes", e);
    }
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor(force = true)
  @ToString
  public static class SupportedFeatures {

    @JsonProperty("permitted_synthesis_morphing")
    private final PermittedSynthesisMorphing permittedSynthesisMorphing;

    enum PermittedSynthesisMorphing {
      ALL,
      SELF_ONLY,
      NOTHING
    }
  }

  public LegalNote getLegalNote(String speakerUuid) {
    if (legalNotes == null) {
      loadLegalNotes();
    }
    return legalNotes.stream()
      .filter(note -> note.getSpeakerUuid().equals(speakerUuid))
      .findFirst()
      .orElseThrow(
        () -> new IllegalArgumentException("No legal note found for speaker UUID: " + speakerUuid));
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor(force = true)
  @ToString
  public static class Style {

    private final int id;
    private final String name;
    private final String type;
  }

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor(force = true)
  @ToString
  public static class LegalNote {

    private final String name;
    @JsonProperty("speaker_uuid")
    private final String speakerUuid;
    @JsonProperty("legal_text")
    private final String legalText;
  }
}


