package app.services;

import app.models.Person;
import app.DBConnection.Repository;
import app.services.utils.MsgDisplay;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_STATUS_LENGTH = 50;

    private final MsgDisplay msgDisplay;
    private final SessionService sessionService;
    private final Repository repository;

    public UserService() {
        this.msgDisplay = new MsgDisplay();
        this.sessionService = new SessionService();
        this.repository = new Repository();
    }

    public void createUser(String username, String password, String name, String status) {
        if (repository.findUserByUsername(username) != null) {
            System.out.println("try harder.");
            System.out.println("failed to create: " + username + " is already registered");
            System.out.println("home: ./app");
            return;
        }

        if (status.length() > MAX_STATUS_LENGTH) {
            System.out.println("try harder.");
            System.out.println("failed to create: status is too long");
            System.out.println("home: ./app");
            return;
        }

        String sessionToken = sessionService.createSession();
        Person newUser = new Person(sessionToken, username, password, name, status, LocalDateTime.now());

        repository.addPerson(newUser);
        msgDisplay.createUserDisplay(newUser);
    }


    public void joinUser(String username, String password, String confirmPassword, String name, String status) {
        if (repository.findUserByUsername(username) != null) {
            System.out.println("try harder.");
            System.out.println("failed to join: " + username + " is already registered");
            System.out.println("home: ./app");
            return;
        }

        if (!password.equals(confirmPassword)) {
            System.out.println("try harder.");
            System.out.println("failed to join: passwords do not match");
            System.out.println("home: ./app");
            return;
        }

        if (status.length() > MAX_STATUS_LENGTH) {
            System.out.println("try harder.");
            System.out.println("failed to join: status is too long");
            System.out.println("home: ./app");
            return;
        }

        String sessionToken = sessionService.createSession();
        Person newUser = new Person(sessionToken, username, password, name, status, LocalDateTime.now());

        repository.addPerson(newUser);
        msgDisplay.createUserDisplay(newUser);
    }

    public void loginUser(String username, String password) {
        Person user = repository.findUserByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("try harder.");
            System.out.println("failed to login: invalid username or password");
            System.out.println("home: ./app");
            return;
        }

        System.out.println("Welcome back to the App, " + user.getName() + "!");
        System.out.println("\"" + user.getStatus() + "\"");

        String sessionToken = user.getSessionToken();

        System.out.println("edit: ./app 'session " + sessionToken + " edit'");
        System.out.println("update: ./app 'session " + sessionToken + " update (name=\"<value>\"|status=\"<value>\")+\'");
        System.out.println("delete: ./app 'session " + sessionToken + " delete'");
        System.out.println("logout: ./app 'session " + sessionToken + " logout'");
        System.out.println("people: ./app '[session " + sessionToken + " ]people'");
    }

    public void logoutUser(String sessionToken) {
        System.out.println("[you are now logged out]");
        msgDisplay.displayHome();
    }

}
