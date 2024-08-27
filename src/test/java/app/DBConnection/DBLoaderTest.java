package app.DBConnection;

import app.DBConnection.DBLoader;
import app.models.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DBLoaderTest {

    private DBLoader dbLoader;

    @BeforeEach
    public void setUp() {
        dbLoader = new DBLoader("src/main/resources/database/test-db-load.csv",
                "src/main/resources/database/test-session-load.csv");
    }

    @Test
    public void testLoadDatabase() {
        String userData = dbLoader.getRepo();
        assertNotNull(userData, "User data should not be null");

        assertTrue(userData.contains("sessionToken,username,password,name,status,updated"),
                "User data should contain header");

        assertTrue(userData.contains("test-token,test1,123,testuser,cnm,2024-08-27 00:14:21"),
                "User data should contain correct user information");
    }

    @Test
    public void testParseCSVData() {
        List<Person> personsList = dbLoader.getPersonsList();
        assertNotNull(personsList, "Persons list should not be null");

        Person firstPerson = personsList.get(0);
        assertEquals("test-token", firstPerson.getSessionToken(),
                "Session token should match");
        assertEquals("test1", firstPerson.getUsername(), "Username should match");
        assertEquals("123", firstPerson.getPassword(), "Password should match");
        assertEquals("testuser", firstPerson.getName(), "Name should match");
        assertEquals("cnm", firstPerson.getStatus(), "Status should match");

    }

    @Test
    public void testLoadSessionInfo() {
        HashMap<String, Integer> tokens = dbLoader.getSessionTokens();
        String sessionData = dbLoader.getSessionData();

        // first row is header
        assertTrue(sessionData.contains("sessionToken, isActive"), "Session data should contain header");

        assertEquals(2, tokens.size(), "There should be 2 tokens in the list");
        assertEquals(1, tokens.get("test-token"), "Token should be active");
        assertEquals(1, tokens.get("test-token2"), "Token should be active");
    }

    @Test
    public void testLoadDatabase_FileNotFound() {
        DBLoader dbLoaderWithWrongPath = new DBLoader("src/main/resources/database/nonexistent-db.csv",
                "src/main/resources/database/nonexistent-session.csv");

        assertNull(dbLoaderWithWrongPath.getRepo(), "User data should be null when file is not found");
        assertNull(dbLoaderWithWrongPath.getSessionData(), "Session data should be null when file is not found");

        assertTrue(dbLoaderWithWrongPath.getPersonsList().isEmpty(), "Persons list should be empty when file is not found");
        assertTrue(dbLoaderWithWrongPath.getSessionTokens().isEmpty(), "Session tokens should be empty when file is not found");
    }

    @Test
    public void testParseCSVData_IncorrectFormat() {

        DBLoader realDBLoader = new DBLoader();

        String userData = realDBLoader.getRepo();
        assertNotNull(userData, "User data should not be null");

        assertTrue(userData.contains("sessionToken,username,password,name,status,updated"),
                "User data should contain header");

        String sessionData = realDBLoader.getSessionData();

        assertTrue(sessionData.contains("sessionToken, isActive"), "Session data should contain header");

    }

}
