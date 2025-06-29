package MochiMochiTalk.voice.nvoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class VoicevoxSpeaker {

  private final static ObjectMapper MAPPER = new ObjectMapper();
  private final static String VOICEVOX_LEGAL_NOTE_RESOURCE = "voicevox_legal.json";
  private static List<LegalNote> legalNotes = null;

  private final String name;
  private final String speakerUuid;
  private final List<Style> styles;

  private static void loadLegalNotes() {
    try {
      legalNotes = List.of(MAPPER.readValue(
        VoicevoxSpeaker.class.getResourceAsStream(VOICEVOX_LEGAL_NOTE_RESOURCE),
        MAPPER.getTypeFactory().constructCollectionType(List.class, LegalNote.class)));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load legal notes", e);
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
  @RequiredArgsConstructor
  @ToString
  public static class Style {

    private final int id;
    private final String name;
    private final String type;
  }

  @Getter
  @RequiredArgsConstructor
  @ToString
  public static class LegalNote {

    private final String name;
    private final String speakerUuid;
    private final String legalText;
  }
}


