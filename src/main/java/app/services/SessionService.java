package app.services;

import app.services.utils.*;
import app.DBConnection.Repository;


public class SessionService {

    private final Repository repository;

    public SessionService() {
        this.repository = new Repository();
    }

    public String createSession() {
        String newSessionToken = ShortIdGenerator.generateShortId();
        repository.activeSession(newSessionToken);
        return newSessionToken;
    }

    public void disableSession(String sessionToken) {
        repository.disableSession(sessionToken);
    }

    public boolean validateSession(String sessionToken) {
        return repository.validateSession(sessionToken);
    }

}
