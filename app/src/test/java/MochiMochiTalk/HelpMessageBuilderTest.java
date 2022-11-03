package MochiMochiTalk;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import MochiMochiTalk.commands.CommandHelp;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class HelpMessageBuilderTest {

    static CommandHelp instance;

    @BeforeAll
    public static void setUp() {
        instance = new CommandHelp();
    }

    @Test
    public void checkVaildEmbedMessageReturns() {
        try {
            Class<?> clazz = Class.forName("MochiMochiTalk.commands.CommandHelp");
            final String normalMethod = "buildNormal";
            final String advancedMethod = "buildAdvanced";
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(normalMethod)) {
                    method.setAccessible(true);
                    MessageEmbed embed = (MessageEmbed) method.invoke(instance);
                    assertNotNull(embed);
                } else if (method.getName().equals(advancedMethod)) {
                    method.setAccessible(true);
                    MessageEmbed embed = (MessageEmbed) method.invoke(instance);
                    assertNotNull(embed);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            fail(e);
        }
    }
    
}
