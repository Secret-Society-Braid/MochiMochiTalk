package MochiMochiTalk.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WhatsnewTest {
    
    static final String DESCRIPTION_FILE_NAME = "description.json";
    static CommandWhatsNew instance = null;
    static Field[] fields = null;
    static Constructor<?>[] constructors = null;

    @BeforeAll
    public static void setup() {
        instance = CommandWhatsNew.getInstance();
        try {
            Class<?> clazz = Class.forName("MochiMochiTalk.commands.CommandWhatsNew");
            fields = clazz.getDeclaredFields();
            constructors = clazz.getDeclaredConstructors();
        } catch (ClassNotFoundException e) {
            fail(e);
        }
    }

    @Test
    public void nonnullCheck() {
        // Declared constructor must be one
        assertEquals(1, constructors.length);

        assertNotNull(constructors[0]);

        assertNotNull(fields);
        for(Field field : fields) {
            assertNotNull(field);
            field.setAccessible(true);
            try {
                assertNotNull(field.get(instance));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                fail(e);
            }
        }
    }

    @Test
    public void testInstance() {
        assertSame(instance, CommandWhatsNew.getInstance());

        Field singletonField = null;
        for(Field field : fields) {
            if(field.getName().equals("singleton")) {
                singletonField = field;
                break;
            }
        }
        assertNotNull(singletonField);
        try {
            constructors[0].setAccessible(true);
            singletonField.setAccessible(true);
            assertSame(instance, singletonField.get(constructors[0].newInstance()));
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            fail(e);
        }
    }
}
