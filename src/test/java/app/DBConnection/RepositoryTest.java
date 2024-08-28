package app.DBConnection;

import app.models.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {
    private Repository repository;

    @BeforeEach
    public void setUp() throws IOException {
        repository = new Repository("src/main/resources/database/test-db-load.csv", "src/main/resources/database/test-session-load.csv");
    }

    @Test
    public void testDBInit() {
        assertNotNull(repository.sessionTokens);
        assertNotNull(repository.personsList);
        assertNotNull(repository.userData);
        assertNotNull(repository.sessionData);

        HashMap<String, Integer> sessionTokens = repository.sessionTokens;
        List<Person> personsList = repository.personsList;
        String userData = repository.userData;
        String sessionData = repository.sessionData;

        assertTrue(sessionTokens.containsKey("test-token"));
        assertEquals(1, sessionTokens.get("test-token"));

        assertTrue(userData.contains("sessionToken,username,password,name,status,updated"));
        assertTrue(userData.contains("test-token,test1,123,testuser,cnm"));

        assertTrue(sessionData.contains("sessionToken, isActive"));

        Repository realRepository = new Repository();
        assertNotNull(realRepository.sessionTokens);
        assertNotNull(realRepository.personsList);
        assertNotNull(realRepository.userData);
        assertNotNull(realRepository.sessionData);
    }

    @Test
    public void testActiveSession() {
        String sessionToken = "new-test-token";

        assertTrue(repository.activeSession(sessionToken));

        HashMap<String, Integer> sessionTokens = repository.sessionTokens;
        assertTrue(sessionTokens.containsKey(sessionToken));

        assertTrue(repository.validateSession(sessionToken));

        assertTrue(repository.deleteSession(sessionToken));
    }

    @Test
    public void testDisableSession() {
        String sessionToken = "new-test-token";

        // Activating the session first
        repository.activeSession(sessionToken);
        assertTrue(repository.validateSession(sessionToken));

        // Disabling the session
        repository.disableSession(sessionToken);
        assertFalse(repository.validateSession(sessionToken));

        assertTrue(repository.deleteSession(sessionToken));
    }

    @Test
    public void testDeleteSession() {
        String sessionToken = "testSessionToken";

        // Activating the session first
        repository.activeSession(sessionToken);
        assertTrue(repository.validateSession(sessionToken));

        // Deleting the session
        repository.deleteSession(sessionToken);
        assertFalse(repository.validateSession(sessionToken));
    }

    @Test
    public void testUpdateSessionFile() {
        int operation = 0;
        String sessionToken = "test-token";

        // Updating the session file
        repository.updateSessionFile(sessionToken, operation);

        HashMap<String, Integer> sessionTokens = repository.sessionTokens;
        assertTrue(sessionTokens.containsKey(sessionToken));
        assertEquals(0, sessionTokens.get(sessionToken));

        // Updating the session file again
        operation = 1;
        repository.updateSessionFile(sessionToken, operation);

        assertTrue(sessionTokens.containsKey(sessionToken));
        assertEquals(1, sessionTokens.get(sessionToken));

    }

    @Test
    public void testFindUserByUsername() {
        // test-token,test1,123,testuser,caonima,2024-08-27 00:14:21

        String username = "test1";

        // Finding the user by username
        Person foundPerson = repository.findUserByUsername(username);
        assertNotNull(foundPerson);

        // all fields should match
        assertEquals("test-token", foundPerson.getSessionToken());
        assertEquals("test1", foundPerson.getUsername());
        assertEquals("123", foundPerson.getPassword());
        assertEquals("testuser", foundPerson.getName());
        assertEquals("cnm", foundPerson.getStatus());

        // Finding a non-existent user
        assertNull(repository.findUserByUsername("nonexistent"));
    }

    @Test
    public void testValidateSession() {
        String sessionToken = "test-token";

        // Validating an active session
        assertTrue(repository.activeSession(sessionToken));
        assertTrue(repository.validateSession(sessionToken));

        // Validating an inactive session
        assertTrue(repository.disableSession(sessionToken));
        assertFalse(repository.validateSession(sessionToken));
        assertTrue(repository.activeSession(sessionToken));

        // Validating a non-existent session
        assertFalse(repository.validateSession("nonexistent"));
    }

    @Test
    public void testAddAndPerson() {
        String time = "2024-08-27 00:14:21";
        LocalDateTime updated = LocalDateTime.parse(time, DBLoader.formatter);

        Person person = new Person("new-test-token", "new-test1", "123", "new-testuser", "new-cnm", updated);

        List<Person> personsList = repository.personsList;

        int initialSize = personsList.size();

        // Adding a new person
        repository.addPerson(person);
        assertTrue(personsList.contains(person));
        assertEquals(initialSize + 1, personsList.size());

        repository.deletePerson(person.getSessionToken());
        assertFalse(personsList.contains(person));
        assertEquals(initialSize, personsList.size());
    }

}
