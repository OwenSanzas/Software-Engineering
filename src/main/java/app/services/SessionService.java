package app.services;

import app.services.utils.*;
import app.DBConnection.Repository;


public class SessionService {

    private final Repository repository;

    public SessionService() {
        this.repository = new Repository();
    }

    public SessionService(Repository repository) {
        this.repository = repository;
    }

    public String createSession() {
        String newSessionToken = ShortIdGenerator.generateShortId();
        repository.activeSession(newSessionToken);
        return newSessionToken;
    }

    public boolean activeSession(String sessionToken) {return repository.activeSession(sessionToken);}

    public boolean disableSession(String sessionToken) {
        return repository.disableSession(sessionToken);
    }

    public boolean validateSession(String sessionToken) {
        return repository.validateSession(sessionToken);
    }

    public boolean deleteSession(String sessionToken) { return repository.deleteSession(sessionToken); }

}
