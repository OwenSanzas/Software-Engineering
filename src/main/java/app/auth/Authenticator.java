package app.auth;

import app.services.SessionService;

public class Authenticator {

    private final SessionService sessionService;

    public Authenticator() {
        this.sessionService = new SessionService();
    }

    public boolean validateSession(String sessionToken) {
        return sessionService.validateSession(sessionToken);
    }
}