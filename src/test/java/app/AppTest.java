package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testApp_Home() {
        String[] args = {"home"};
        App.main(args);

        String expectedOutput = "Welcome to the App!\n\n" +
                "login: ./app 'login <username> <password>'\n" +
                "join: ./app 'join'\n" +
                "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n" +
                "show people: ./app 'people'\n";
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testApp_Home2() {
        String[] args = {};
        App.main(args);

        String expectedOutput = "Welcome to the App!\n\n" +
                "login: ./app 'login <username> <password>'\n" +
                "join: ./app 'join'\n" +
                "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n" +
                "show people: ./app 'people'\n";
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testApp_CreateWithInvalidUsername() {
        String[] args = {"create username=\"quo\\\"te\" password=\"password\" name=\"Test User\" status=\"active\""};
        App.main(args);

        String expectedOutput = "failed to create: invalid username";
        assertTrue(outContent.toString().contains(expectedOutput));
    }


    @Test
    public void testApp_CreateWithInvalidPassword() {
        String[] args = {"create username=\"quote\" password=\"pas\"sword\" name=\"Test User\" status=\"active\""};
        App.main(args);

        String expectedOutput = "password contains double quote";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testApp_CreateWithInvalidPassword2() {
        String[] args = {"create username=\"quote\" password=\"asdjkashdkajhdkajdhaskdjahdkajsh" +
                "daksdhaskdjhadkajhdkasjdhaskdjhaskdjashdkasjhdaskjdhsakjdh\" name=\"Test User\" status=\"active\""};
        App.main(args);

        String expectedOutput = "password is too long";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testApp_CreateWithInvalidPassword3() {
        String[] args = {"create username=\"quote\" password=\"12\" name=\"Test User\" status=\"active\""};
        App.main(args);

        String expectedOutput = "password is too short";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testApp_CreateWithInvalidName() {
        String[] args = {"create username=\"quote\" password=\"password\" name=\"Test\"User\" status=\"active\""};
        App.main(args);

        String expectedOutput = "name contains double quote";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testApp_CreateWithInvalidName2() {
        String[] args = {"create username=\"quote\" password=\"password\" name=\"\" status=\"active\""};
        App.main(args);

        String expectedOutput = "name is too short";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testApp_CreateWithInvalidName3() {
        String[] args = {"create username=\"quote\" password=\"password\" name=\"Testasklsadashjdasdhajdakdjahd" +
                "User\" status=\"active\""};
        App.main(args);

        String expectedOutput = "name is too long";
        assertTrue(outContent.toString().contains(expectedOutput));
    }


    @Test
    public void testApp_CreateWithInvalidStatus() {
        String[] args = {"create username=\"quote\" password=\"password\" name=\"Test User\" status=\"act\"sive\""};
        App.main(args);

        String expectedOutput = "status contains double quote";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testApp_CreateWithInvalidStatus2() {
        String[] args = {"create username=\"quote\" password=\"password\" name=\"Test User\" status=\"\""};
        App.main(args);

        String expectedOutput = "status is too short";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testApp_CreateWithInvalidStatus3() {
        String[] args = {"create username=\"quote\" password=\"password\" name=\"Test User\" status=\"ThisStatusIsWayT" +
                "ooLongForTasdadasdhaskdasgdhagjhgdjahdgajdhagdjasdjadkjhqweApplicationToHandleCorrectly\""};
        App.main(args);

        String expectedOutput = "status is too long";
        assertTrue(outContent.toString().contains(expectedOutput));
    }


}
