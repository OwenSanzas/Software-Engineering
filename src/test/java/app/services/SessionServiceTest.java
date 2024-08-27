package app.services;

import app.DBConnection.Repository;
import app.DBConnection.RepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SessionServiceTest {

    private SessionService sessionService;

    @BeforeEach
    public void setUp() {
        sessionService = new SessionService();
    }

    @Test
    public void testCreateAndDeleteSession() {
        String sessionToken = sessionService.createSession();
        assertNotNull(sessionToken, "Session token should not be null");

        assertTrue(sessionService.deleteSession(sessionToken));
    }

    @Test
    public void testDisableAndActivateSession() {
        String sessionToken = sessionService.createSession();

        assertTrue(sessionService.disableSession(sessionToken));
        assertFalse(sessionService.validateSession(sessionToken), "Session should be disabled");

        assertTrue(sessionService.activeSession(sessionToken));
        assertTrue(sessionService.validateSession(sessionToken), "Session should be active");

        assertTrue(sessionService.deleteSession(sessionToken));
    }

    @Test
    public void testValidateSessionAndInvalidSession() {
        String sessionToken = sessionService.createSession();
        assertTrue(sessionService.validateSession(sessionToken));

        String invalidSession = "invalid-session";
        assertFalse(sessionService.validateSession(invalidSession));

        assertTrue(sessionService.disableSession(sessionToken));
        assertFalse(sessionService.validateSession(sessionToken));

        assertTrue(sessionService.deleteSession(sessionToken));
    }

}
