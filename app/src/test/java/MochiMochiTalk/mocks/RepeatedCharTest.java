package MochiMochiTalk.mocks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

public class RepeatedCharTest {

    /** regex pattern*/
    private static final String REGEX_STRING = "(!|w|！|ｗ)";

    @Test
    public void matchTest() {
        Pattern pattern = Pattern.compile(REGEX_STRING);
        Matcher trueMatcher = pattern.matcher("おはようございます！！！！！");
        Matcher falseMatcher = pattern.matcher("おやすみなさい");
        
        assertTrue(trueMatcher::find);

        assertFalse(falseMatcher::find);

    }
}