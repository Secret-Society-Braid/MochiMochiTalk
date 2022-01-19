package MochiMochiTalk.mocks;

public class GeneralExpressionMocks {

    public static boolean generalExpression(String testString, String generalExpression) {
        return testString.matches(generalExpression);
    }

    public static boolean emoteExpression(String testString) {
        return testString.contains("<:") && testString.contains(">");
    }

    public static boolean detectCommand(String testString) {
        return testString.startsWith("!!");
    }
    
    public static boolean detectCodeBlock(String testString) {
        return testString.startsWith("```");
    }
}
