package app.services;

import app.DBConnection.Repository;
import app.models.Person;
import app.services.utils.MsgDisplay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private SessionService sessionService;
    private UserService realUserService;
    private SessionService realSessionService;

    private Repository repository;
    private Repository realRepository;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        repository = new Repository("src/main/resources/database/test-db-load.csv",
                "src/main/resources/database/test-session-load.csv");
        userService = new UserService(repository);
        sessionService = new SessionService(repository);

        realUserService = new UserService();
        realSessionService = new SessionService();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testHandlePeople() {
        // get output of sout
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        userService.handlePeople();

        String expectedOutput = "testuser @test1";

        String expectedOutput2 = "find: ./app 'find";

        // should contain the expected output
        assertTrue(outContent.toString().contains(expectedOutput));
        assertTrue(outContent.toString().contains(expectedOutput2));
    }

    @Test
    public void testCreateAndDeleteUser_Success() {
        Random random = new Random();
        String username = "newUser" + random.nextInt(1000);
        String password = "password";
        String name = "Test User";
        String status = "active";

        userService.createUser(username, password, name, status);
        Person realNewUser = realUserService.createUser(username, password, name, status);

        Person newUser = repository.findUserByUsername(username);

        assertNotNull(realNewUser, "User should be added successfully");
        assertEquals(username.toLowerCase(), realNewUser.getUsername(), "Username should match");
        assertEquals(name, realNewUser.getName(), "Name should match");
        assertEquals(status, realNewUser.getStatus(), "Status should match");

        assertNotNull(newUser, "User should be added successfully");
        assertEquals(username.toLowerCase(), newUser.getUsername(), "Username should match");
        assertEquals(name, newUser.getName(), "Name should match");
        assertEquals(status, newUser.getStatus(), "Status should match");

        userService.deleteUser(newUser.getSessionToken());
        assertTrue(realUserService.deleteUser(realNewUser.getSessionToken()));

        assertNull(repository.findUserByUsername(username), "User should be deleted successfully");
    }

    @Test
    public void testCreateUser_UsernameAlreadyExists() {
        String existingUsername = "test1";
        String password = "password";
        String name = "Test User";
        String status = "active";

        assertNull(userService.createUser(existingUsername, password, name, status));

        long userCount = repository.getPersonsList().stream()
                .filter(person -> person.getUsername().equals(existingUsername))
                .count();
        assertEquals(1, userCount, "User should not be created if username already exists");
    }

    @Test
    public void testCreateUser_StatusTooLong() {
        String username = "userWithLongStatus";
        String password = "password";
        String name = "TestUser";
        String longStatus = "ThisStatusIsWayTooLongForThaskdasgdhagjhgdjahdgajdhagdjasdjadkasdasdasdasdasjhqweApplicationToHandleCorrectly";

        assertNull(userService.createUser(username, password, name, longStatus));

        assertNull(repository.findUserByUsername(username), "User should not be created if status is too long");
    }

    @Test
    public void testJoinUser_Success() {
        String username = "joinUser";
        String password = "password";
        String confirmPassword = "password";
        String name = "Join User";
        String status = "active";

        userService.joinUser(username, password, confirmPassword, name, status);

        Person newUser = repository.findUserByUsername(username);
        assertNotNull(newUser, "User should be added successfully");
        assertEquals(username.toLowerCase(), newUser.getUsername(), "Username should match");
        assertEquals(name, newUser.getName(), "Name should match");
        assertEquals(status, newUser.getStatus(), "Status should match");


        userService.deleteUser(newUser.getSessionToken());
    }

    @Test
    public void testJoinUserWithInvalidUsername() {
        String username = "joinUser with space";
        String password = "password";
        String confirmPassword = "password";
        String name = "Join User";
        String status = "active";

        assertNull(userService.joinUser(username, password, confirmPassword, name, status));
    }

    @Test
    public void testCreateUserWithInvalidUsername() {
        String username = "joinUser with space";
        String password = "password";
        String name = "Join User";
        String status = "active";

        assertNull(userService.createUser(username, password, name, status));
    }

    @Test
    public void testCreateUserWithInvalidUsername2() {
        String username = "q\"ote";
        String password = "password";
        String name = "Join User";
        String status = "active";

        assertNull(userService.createUser(username, password, name, status));
    }

    @Test
    public void testJoinUserWithInvalidUsername2() {
        String username = "q\"ote";
        String password = "password";
        String confirmPassword = "password";
        String name = "Joinuser";
        String status = "active";

        assertNull(userService.joinUser(username, password, confirmPassword, name, status));
    }

    @Test
    public void testHandleEditPerson() {
        userService.handleEditPerson("invalid");

        String expectedOutput = "invalid request: invalid session token";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testHandleUpdatePerson() {
        userService.handleEditPerson("invalid");

        String expectedOutput = "invalid request: invalid session token";
        assertTrue(outContent.toString().contains(expectedOutput));
    }

    @Test
    public void testJoinUser_Exist() {
        String username = "test1";
        String password = "123123";
        String confirmPassword = "password";
        String name = "Joinuser";
        String status = "active";


        assertNull(userService.joinUser(username, password, confirmPassword, name, status));

        String expectedOutput = "already registered";
        System.out.println(outContent.toString());
        assertTrue(outContent.toString().contains(expectedOutput));

    }

    @Test
    public void testJoinUser_LongStatus() {
        String username = "joinUser";
        String password = "password";
        String confirmPassword = "password";
        String name = "Joinuser";
        String status = "activedjkalsdjlasjdladjlasdjalkdjaslkdjasldjasdasdas" +
                "dasdadasdasdasdasdasldjasldjsldajdlakjdalksjdlaskjdlaksjdsal";

        assertNull(userService.joinUser(username, password, confirmPassword, name, status));
    }

    @Test
    public void testJoinUser_PasswordMismatch() {
        String username = "userWithMismatchedPassword";
        String password = "password";
        String confirmPassword = "differentPassword";
        String name = "Testuser";
        String status = "active";

        assertNull(userService.joinUser(username, password, confirmPassword, name, status));

        assertNull(repository.findUserByUsername(username), "User should not be created if passwords do not match");
    }

    @Test
    public void testLoginUser_Success() {
        String username = "test1";
        String password = "123";

        sessionService.disableSession(repository.findUserByUsername(username).getSessionToken());
        assertFalse(sessionService.validateSession(repository.findUserByUsername(username).getSessionToken()));

        userService.loginUser(username, password);
        assertTrue(sessionService.validateSession(repository.findUserByUsername(username).getSessionToken()));
    }

    @Test
    public void testLoginUser_InvalidUsernameOrPassword() {
        String username = "test1";
        String password = "wrongPassword";

        sessionService.disableSession(repository.findUserByUsername(username).getSessionToken());
        assertFalse(sessionService.validateSession(repository.findUserByUsername(username).getSessionToken()));

        userService.loginUser(username, password);
        assertFalse(sessionService.validateSession(repository.findUserByUsername(username).getSessionToken()));

        sessionService.activeSession(repository.findUserByUsername(username).getSessionToken());
        assertTrue(sessionService.validateSession(repository.findUserByUsername(username).getSessionToken()));

        username = "nonexistentUser";

        userService.loginUser(username, password);
        assertNull(repository.findUserByUsername(username));
    }

    @Test
    public void testLogoutUser() {
        String username = "test1";
        String password = "123";
        String token = repository.findUserByUsername(username).getSessionToken();


        userService.loginUser(username, password);
        assertTrue(sessionService.validateSession(token));

        userService.logoutUser(token);
        assertFalse(sessionService.validateSession(token));
    }
}
