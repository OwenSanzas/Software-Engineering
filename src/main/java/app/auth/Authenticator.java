package app.auth;

import app.services.SessionService;

import java.util.List;

public class Authenticator {

    private final SessionService sessionService;

    public Authenticator() {
        this.sessionService = new SessionService();
    }

    public Authenticator(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public boolean validateSession(String sessionToken) {
        return sessionService.validateSession(sessionToken);
    }

}