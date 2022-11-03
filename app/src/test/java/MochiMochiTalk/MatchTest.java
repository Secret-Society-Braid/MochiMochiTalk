package MochiMochiTalk;

import static MochiMochiTalk.mocks.GeneralExpressionMocks.detectCodeBlock;
import static MochiMochiTalk.mocks.GeneralExpressionMocks.detectCommand;
import static MochiMochiTalk.mocks.GeneralExpressionMocks.emoteExpression;
import static MochiMochiTalk.mocks.GeneralExpressionMocks.generalExpression;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MatchTest {
    
    @Test
    public void urlMatchTest() {
        String testBetween = "テストhttps://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String testbegin = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

        assertTrue(generalExpression(testbegin, "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
        assertTrue(generalExpression(testBetween, "\\b.+(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));

    }

    @Test
    public void anyUrlMatchTest() {
        String testBetween = "テストhttps://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String testbegin = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String escapeTest = "テスト\nhttps://www.youtube.com/watch?v=dQw4w9WgXcQ";

        assertTrue(generalExpression(testbegin, ".*(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
        assertTrue(generalExpression(escapeTest, ".*\\s?(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
        assertTrue(generalExpression(testBetween, ".*(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
        assertTrue(generalExpression(escapeTest, ".*\\s.*(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
    }

    @Test
    public void urlRegexTest() {
        String regex = "(.|\\s)*(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]*(.|\\s)";
        String testBetween = "テストhttps://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String testbegin = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String escapeTest = "テスト\nhttps://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String escapeDoubleTest = "テスト\n\nhttps://www.youtube.com/watch?v=dQw4w9WgXcQ\n";

        assertTrue(generalExpression(testBetween, regex));
        assertTrue(generalExpression(escapeTest, regex));
        assertTrue(generalExpression(testbegin, regex));
        assertTrue(generalExpression(escapeDoubleTest, regex));
    }

    @Test
    public void emoteMatchTest() {
        String testBetween = "テスト<:test:123456789012345678>";
        String testbegin = "<:test:123456789012345678>";
        String testEnd = "<:test:123456789012345678>テスト";

        assertTrue(emoteExpression(testbegin));
        assertTrue(emoteExpression(testBetween));
        assertTrue(emoteExpression(testEnd));
    }

    @Test
    public void commandMatchTest() {
        String testString = "!!test";
        String testBetween = "テスト!!test";

        assertTrue(detectCommand(testString));
        assertFalse(detectCommand(testBetween));
    }

    @Test
    public void codeBlockMatchTest() {
        String testString = "``` Here comes the code block ```";

        assertTrue(detectCodeBlock(testString));
    }

}
