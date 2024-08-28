package app.controller;

import app.DBConnection.Repository;
import app.models.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private Controller controller;
    private Repository repository;

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        repository = new Repository("src/main/resources/database/test-db-load.csv",
                "src/main/resources/database/test-session-load.csv");

        System.setOut(new PrintStream(outContent));
        controller = new Controller(repository);
    }

    @Test
    public void testInvalidCommand() {
        controller.handleRequest(Arrays.asList("12313123123"));
        String expectedOutput = "resource not found";

        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testInvalidSession() {
        controller.handleRequest(Arrays.asList("session", "12313123123", "delete"));
        String expectedOutput = "invalid request: invalid session token";

        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testHandleRequest_Home() {
        controller.handleRequest(Arrays.asList("home"));
        String expectedOutput = "Welcome to the App!\n\n" +
                "login: ./app 'login <username> <password>'\n" +
                "join: ./app 'join'\n" +
                "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n" +
                "show people: ./app 'people'\n";
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testHandleRequest_HomeEmpty() {
        controller.handleRequest(Arrays.asList());
        String expectedOutput = "Welcome to the App!\n\n" +
                "login: ./app 'login <username> <password>'\n" +
                "join: ./app 'join'\n" +
                "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n" +
                "show people: ./app 'people'\n";
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testHandleRequest_Session() {
        controller.handleRequest(Arrays.asList("session", "test-token"));
        assertTrue(outContent.toString().contains("Welcome back to the App, testuser!"));
    }

    @Test
    public void testHandleRequest_Session2() {
        controller.handleRequest(Arrays.asList("session", "test-token", "home"));
        assertTrue(outContent.toString().contains("Welcome back to the App, testuser!"));
    }

    @Test
    public void testHandleRequest_CreateAndDelete() {
        List<String> commandArgs = Arrays.asList(
                "create",
                "username=\"testuser\"",
                "password=\"1234\"",
                "name=\"Test User\"",
                "status=\"Active\""
        );

        controller.handleRequest(commandArgs);
        String expectedOutput = "[account created]\n" +
                "Person\n" +
                "------\n" +
                "name: Test User\n" +
                "username: testuser\n" +
                "status: Active\n" +
                "updated: ";

        System.out.println(outContent);
        assertTrue(outContent.toString().contains(expectedOutput));

        Person user = repository.findUserByUsername("testuser");
        assertNotNull(user);

        String sessionToken = user.getSessionToken();
        System.out.println(sessionToken);
        List<String> deleteCommandArgs = Arrays.asList("session", sessionToken, "delete");
        controller.handleRequest(deleteCommandArgs);

        String expectedDeleteOutput = "[account deleted]\n" +
                "Welcome to the App!\n\n" +
                "login: ./app 'login <username> <password>'\n" +
                "join: ./app 'join'\n" +
                "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n" +
                "show people: ./app 'people'\n";

        assertTrue(outContent.toString().contains(expectedDeleteOutput));
    }

    @Test
    public void testHandleRequest_Login() {
        List<String> createCommand = Arrays.asList(
                "create",
                "username=\"testuser\"",
                "password=\"1234\"",
                "name=\"Test User\"",
                "status=\"Active\""
        );
        controller.handleRequest(createCommand);

        List<String> loginCommand = Arrays.asList("login", "testuser", "1234");
        outContent.reset();
        controller.handleRequest(loginCommand);
        String expectedOutput = "Welcome back to the App, Test User!\n" +
                "\"Active\"\n";
        assertFalse(outContent.toString().startsWith(expectedOutput));

        Person user = repository.findUserByUsername("testuser");
        assertNotNull(user);

        String sessionToken = user.getSessionToken();
        System.out.println(sessionToken);
        List<String> deleteCommandArgs = Arrays.asList("session", sessionToken, "delete");
        controller.handleRequest(deleteCommandArgs);
    }

    @Test
    public void testHandleRequest_InvalidCommand() {
        List<String> commandArgs = Arrays.asList("invalidCommand");
        controller.handleRequest(commandArgs);
        String expectedOutput = "try harder.\n" +
                "resource not found\n" +
                "home: ./app\n";
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    public void testHandleRequest_SessionLogout() {
        // Create and login a user first
        List<String> createCommand = Arrays.asList(
                "create",
                "username=\"testuser\"",
                "password=\"1234\"",
                "name=\"Test User\"",
                "status=\"Active\""
        );
        controller.handleRequest(createCommand);
        List<String> loginCommand = Arrays.asList("login", "testuser", "1234");
        outContent.reset();
        controller.handleRequest(loginCommand);

        // Extract session token from the output (mock token handling in actual test cases)
        String output = outContent.toString();
        String sessionToken = output.split("'")[1].split(" ")[1];

        // Test logout
        outContent.reset();
        controller.handleRequest(Arrays.asList("session", sessionToken, "logout"));
        String expectedOutput = "[you are now logged out]\nWelcome to the App!\n\n";
        assertTrue(outContent.toString().startsWith(expectedOutput));
    }

    @Test
    public void testHandleShowPersonWithoutSession() {
        controller.handleRequest(Arrays.asList("show", "test1"));
        assertTrue(outContent.toString().contains("Person"));
        assertTrue(outContent.toString().contains("------"));
        assertTrue(outContent.toString().contains("name: testuser"));
        assertTrue(outContent.toString().contains("people: ./app 'people'\n" +
                "home: ./app"));
    }

    @Test
    public void testHandleShowPersonWithSession() {
        controller.handleRequest(Arrays.asList("session", "test-token", "show", "test1"));
        assertTrue(outContent.toString().contains("Person"));
        assertTrue(outContent.toString().contains("------"));
        assertTrue(outContent.toString().contains("name: testuser"));
        assertTrue(outContent.toString().contains("edit: ./app 'session test-token edit'\n" +
                "update: ./app 'session test-token update (name=\"<value>\"|status=\"<value>\")+'\n" +
                "delete: ./app 'session test-token delete'\n" +
                "logout: ./app 'session test-token logout'\n" +
                "people: ./app '[session test-token ]people'\n" +
                "home: ./app ['session test-token']"));
    }

    @Test
    public void testHandleShowPersonWithOtherToken() {
        controller.handleRequest(Arrays.asList("session", "test-token2", "show", "test1"));
        assertTrue(outContent.toString().contains("Person"));
        assertTrue(outContent.toString().contains("------"));
        assertTrue(outContent.toString().contains("name: testuser"));

        assertTrue(outContent.toString().contains("people: ./app '[session test-token2 ]people'\n"));
        assertTrue(outContent.toString().contains("home: ./app ['session test-token2']"));
    }

    @Test
    public void testHandlePeople() {
        List<String> peopleCMD = Arrays.asList("people");

        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        controller.handleRequest(peopleCMD);

        String expectedOutput = "testuser @test1";

        String expectedOutput2 = "find: ./app 'find";

        // should contain the expected output
        assertTrue(outContent.toString().contains(expectedOutput));
        assertTrue(outContent.toString().contains(expectedOutput2));

        List<String> peopleCMD2 = Arrays.asList("session", "test-token", "people");
        controller.handleRequest(peopleCMD2);

        assertTrue(outContent.toString().contains("home: ./app ['session"));
    }

    @Test
    public void testHandleJoin() {
        String simulatedInput = "testuser\n1234\n1234\nTest User\nActive\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        controller.handleRequest(List.of("join"));

        String expectedOutput = "[account created]\n" +
                "Person\n" +
                "------\n" +
                "name: Test User\n" +
                "username: testuser\n" +
                "status: Active\n" +
                "updated: ";
        assertTrue(outContent.toString().contains(expectedOutput));

        Person user = repository.findUserByUsername("testuser");
        assertNotNull(user);

        String sessionToken = user.getSessionToken();
        controller.handleRequest(List.of("session", sessionToken, "delete"));

        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    public void testCreateTwoIdenticalUsers() {
        // "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n"
        controller.handleRequest(Arrays.asList("create", "username=\"testuser\"", "password=\"1234\"", "name=\"Testuser\"", "status=\"Active\""));
        assertTrue(outContent.toString().contains("[account created]"));

        controller.handleRequest(Arrays.asList("create", "username=\"testuser\"", "password=\"1234\"", "name=\"Testuser\"", "status=\"Active\""));
        String expectedOutput = "already registered";
        assertTrue(outContent.toString().contains(expectedOutput));

        String sessionToken = repository.findUserByUsername("testuser").getSessionToken();
        controller.handleRequest(Arrays.asList("session", sessionToken, "delete"));
    }


    @Test
    public void testSearchWithNoField() {
        // "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n"
        controller.handleRequest(Arrays.asList("find"));
        assertTrue(outContent.toString().contains("People (find all)"));

    }

    @Test
    public void testSearchWithField() {
        // "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n"
        controller.handleRequest(Arrays.asList("find", "testuser"));
        assertTrue(outContent.toString().contains("People (find \"testuser\" in any)"));

    }

    @Test
    public void testSearchWithFieldAndValue() {
        // "create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'\n"
        controller.handleRequest(Arrays.asList("find", "username: testuser"));
        assertTrue(outContent.toString().contains("People (find \"testuser\" in username)"));

    }

    @Test
    public void testSearchWithFieldAndValue2() {
        // "create: ./app '
        controller.handleRequest(Arrays.asList("find", "fake: Testuser" ));
        assertTrue(outContent.toString().contains("People (find \"fake: Testuser\" in any)" ));
    }


    @Test
    public void testUpdateWithNoSession() {
        controller.handleRequest(Arrays.asList("update"));
        assertTrue(outContent.toString().contains("invalid request: missing session token" ));
    }

    @Test
    public void testUpdateWithInvalidSession() {
        controller.handleRequest(Arrays.asList("session", "invalid-token", "update"));
        assertTrue(outContent.toString().contains("token"));
    }


    @Test
    public void testUpdateSuccessWithOneArg() {
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"testuser\""));
        assertFalse(outContent.toString().contains("[name updated]"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"newtestuser\""));
        assertTrue(outContent.toString().contains("[name updated]"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"testuser\""));
        assertTrue(outContent.toString().contains("[name updated]"));
    }

    @Test
    public void testUpdateSuccessWithOneArg2() {
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "status=\"cnm\""));
        assertFalse(outContent.toString().contains("[status updated]"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "status=\"newstatus\""));
        assertTrue(outContent.toString().contains("[status updated]"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "status=\"cnm\""));
        assertTrue(outContent.toString().contains("[status updated]"));
    }

    @Test
    public void testUpdateSuccessWithMultipleArgs() {
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"testuser\" status=\"cnm\""));
        assertFalse(outContent.toString().contains("[name updated]"));
        assertFalse(outContent.toString().contains("[status updated]"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"newtestuser\"", "status=\"cnm\""));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"testuser\"", "status=\"cnm\""));
        assertTrue(outContent.toString().contains("[name updated]"));
    }

    @Test
    public void testUpdateLengthWithMultipleArgs() {
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"testuser\" status=\"cnm\""));
        assertFalse(outContent.toString().contains("[name updated]"));
        assertFalse(outContent.toString().contains("[status updated]"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"\"", "status=\"cnm\""));
        assertTrue(outContent.toString().contains("too short"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"das" +
                "kjdlasjdkajdlaskjdalsdjasldjadjasldjasdashdjgasjdgasjdgasjdhasg" +
                "djashgdasjhdgasjdgsajdgasjhdgasjdg\"", "status=\"cnm\""));
        assertTrue(outContent.toString().contains("too long"));
    }

    @Test
    public void testUpdateLengthWithMultipleArgs2() {
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"testuser\" status=\"cnm\""));
        assertFalse(outContent.toString().contains("[name updated]"));
        assertFalse(outContent.toString().contains("[status updated]"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"testuser\"", "status=\"\""));
        assertTrue(outContent.toString().contains("too short"));
        controller.handleRequest(Arrays.asList("session", "test-token", "update", "name=\"testuser\"", "status=\"asdlka" +
                "sdkasdka" +
                "sasdhgasjhdgasjhdgasjhdgajdgtqwydshagdjashgd" +
                "jsahdgjashdgasjdhasgdjashdgasjdgasjdhgasjdhgasjdashgdjash" +
                "gdasjhdgasjdhasgdjashdgasjhdgasjdhsgadjahsgdjasgdjashgda\""));
        assertTrue(outContent.toString().contains("too long"));
    }

    @Test
    public void testHandleEditPerson() {
        String simulatedInput = "\n" + "gig 'em, aggies\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        controller.handleRequest(Arrays.asList("create",
                "username=\"pcr\"",
                "password=\"1234\"",
                "name=\"prof. ritchey\"",
                "status=\"demonstrating\""));
        Person person = repository.findUserByUsername("pcr");
        assertNotNull(person);

        String sessionToken = person.getSessionToken();

        outContent.reset();
        controller.handleRequest(Arrays.asList("session", sessionToken, "edit"));

        String expectedOutput = "Edit Person\n" +
                "-----------\n" +
                "leave blank to keep [current value]\n" +
                "name [prof. ritchey]: \n" +
                "status [demonstrating]: \n" +
                "[status updated]\n" +
                "Person\n" +
                "------\n" +
                "name: prof. ritchey\n" +
                "username: pcr\n" +
                "status: gig 'em, aggies\n" +
                "updated: ";

        assertTrue(outContent.toString().contains("[status updated]"));
        assertTrue(outContent.toString().contains("name: prof. ritchey"));
        assertTrue(outContent.toString().contains("status: gig 'em, aggies"));
        assertTrue(outContent.toString().contains("edit: ./app 'session " + sessionToken + " edit'"));
        assertTrue(outContent.toString().contains("update: ./app 'session " + sessionToken + " update (name=\"<value>\"|status=\"<value>\")+'"));
        assertTrue(outContent.toString().contains("delete: ./app 'session " + sessionToken + " delete'"));
        assertTrue(outContent.toString().contains("logout: ./app 'session " + sessionToken + " logout'"));

        controller.handleRequest(Arrays.asList("session", sessionToken, "delete"));
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    public void testDefaultSortByUpdatedDesc() {
        controller.handleRequest(Arrays.asList("people"));
        String output = outContent.toString();

        assertTrue(output.contains("People\n------"));
        assertTrue(output.matches("(?s).*testuser @test1.*testuser2 @test2.*"));
    }

    @Test
    public void testSortByNameAsc() {
        controller.handleRequest(Arrays.asList("sort", "name"));
        String output = outContent.toString();

        assertTrue(output.matches("(?s).*testuser @test1.*testuser2 @test2.*"));
    }

    @Test
    public void testSortByUsernameAsc() {
        controller.handleRequest(Arrays.asList("sort", "username", "asc"));
        String output = outContent.toString();

        assertTrue(output.matches("(?s).*testuser @test1.*testuser2 @test2.*"));
    }

    @Test
    public void testSortByStatusAsc() {
        controller.handleRequest(Arrays.asList("sort", "status", "asc"));
        String output = outContent.toString();

        assertFalse(output.contains("People (sorted by status, ascending)"));
    }

    @Test
    public void testSortByUpdatedAsc() {
        controller.handleRequest(Arrays.asList("sort", "updated", "asc"));
        String output = outContent.toString();

        assertTrue(output.matches("(?s).*testuser2 @test2.*testuser @test1.*"));
    }

    @Test
    public void testSortByUpdatedDesc() {
        controller.handleRequest(Arrays.asList("sort", "updated", "desc"));
        String output = outContent.toString();

        assertTrue(output.matches("(?s).*testuser @test1.*testuser2 @test2.*"));
    }


    @Test
    public void testFindWithSession() {
        controller.handleRequest(Arrays.asList("session", "test-token", "find"));
        String output = outContent.toString();

        assertTrue(outContent.toString().contains("People (find all)"));
    }

    @Test
    public void testOnlyInvalidToken() {
        controller.handleRequest(Arrays.asList("session", "invalid"));
        String output = outContent.toString();

        assertTrue(outContent.toString().contains("invalid request: invalid session token"));
    }

    @Test
    public void testCallFunctionWithNoSession1() {
        controller.handleRequest(Arrays.asList("logout"));
        String output = outContent.toString();
        assertTrue(outContent.toString().contains("invalid request: missing session token"));
    }

    @Test
    public void testCallFunctionWithNoSession2() {
        controller.handleRequest(Arrays.asList("edit"));
        String output = outContent.toString();
        assertTrue(outContent.toString().contains("invalid request: missing session token"));
    }

    @Test
    public void testCallFunctionWithNoSession3() {
        controller.handleRequest(Arrays.asList("delete"));
        String output = outContent.toString();
        assertTrue(outContent.toString().contains("invalid request: missing session token"));
    }

}
