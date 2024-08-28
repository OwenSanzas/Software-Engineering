package app.services.utils;

import app.models.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private MsgDisplay msgDisplay;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        msgDisplay = new MsgDisplay();
    }

    @Test
    public void testCreateUserDisplay() {
        Person newUser = new Person("test-token", "testuser", "password", "Test User", "active", LocalDateTime.now());
        msgDisplay.createUserDisplay(newUser);

        String expectedOutput = "[account created]\n" +
                "Person\n" +
                "------\n" +
                "name: Test User\n" +
                "username: testuser\n" +
                "status: active\n" +
                "updated: " + newUser.getUpdated() + "\n\n" +
                "edit: ./app 'session test-token edit'\n" +
                "update: ./app 'session test-token update (name=\"<value>\"|status=\"<value>\")+\'\n" +
                "delete: ./app 'session test-token delete'\n" +
                "logout: ./app 'session test-token logout'\n" +
                "people: ./app '[session test-token ]people'\n" +
                "home: ./app ['session test-token']\n";

        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testDisplayHome() {
        msgDisplay.displayHome();

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

        msgDisplay.displayInvalidCommand(commandArgs);

        String expectedOutput = "try harder.\n" +
                "resource not found\n" +
                "home: ./app\n";

        assertEquals(expectedOutput, outContent.toString());
    }
}
