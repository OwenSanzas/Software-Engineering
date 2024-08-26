package app.services;

import app.services.utils.*;
import app.DBConnection.Repository;


public class SessionService {

    private final Repository repository;

    public SessionService() {
        this.repository = new Repository();
    }

    // 创建新的 session，返回自定义的 session token
    public String createSession(String username) {
        return ShortIdGenerator.generateShortId();
    }

    public boolean validateSession(String sessionToken) {
        return repository.validateSession(sessionToken);
    }

}
