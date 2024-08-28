package app.controller.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testDisplayHome() {
        DisplayUtils.displayHome();

        String expectedOutput = "Welcome to the App!\n\n" +
                "login: ./app 'login <username> <password>'\n" +
                "join: ./app 'join'\n" +
                "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n" +
                "show people: ./app 'people'\n";

        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testDisplayInvalidCommand() {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add("invalidCommand");

        DisplayUtils.displayInvalidCommand(commandArgs);

        String expectedOutput = "try harder.\n" +
                "resource not found\n" +
                "home: ./app\n";

        assertEquals(expectedOutput, outContent.toString());
    }
}
