package app.controller;

import app.DBConnection.Repository;
import app.auth.*;
import app.models.Person;
import app.services.*;
import app.controller.utils.DisplayUtils;

import java.util.*;

public class Controller {

    private final UserService userService;
    private final SessionService sessionService;
    private final Authenticator auth;
    private final Repository repository;

    public Controller() {
        this.repository = new Repository();
        userService = new UserService();
        sessionService = new SessionService();
        auth = new Authenticator();
    }

    public Controller(Repository repository) {
        this.repository = repository;
        userService = new UserService(repository);
        sessionService = new SessionService(repository);
        auth = new Authenticator(sessionService);
    }

    public void handleRequest(List<String> commandArgs) {
        if (commandArgs.isEmpty()) {
            DisplayUtils.displayHome();
            return;
        }

        String command = commandArgs.get(0).toLowerCase();

        if ("session".equals(command) && commandArgs.size() == 1) {
            System.out.println("access denied: missing session token" );
            System.out.println("home: ./app" );
            return;
        }

        if ("session".equals(command) && commandArgs.size() >= 2) {
            String sessionToken = commandArgs.get(1);

            // If only the session token is provided without any command
            if (commandArgs.size() == 2) {
                if (auth.validateSession(sessionToken)) {
                    userService.handleHomeWithSession(sessionToken);
                } else {
                    System.out.println("try harder." );
                    System.out.println("invalid request: invalid session token" );
                    System.out.println("home: ./app" );
                }
                return;
            }

            // If session command and token are present
            String sessionCommand = commandArgs.get(2).toLowerCase();
            if (auth.validateSession(sessionToken)) {
                handleSessionCommand(sessionToken, sessionCommand, commandArgs.subList(3, commandArgs.size()));
                return;
            } else {
                System.out.println("try harder." );
                System.out.println("invalid request: invalid session token" );
                System.out.println("home: ./app" );
                return;
            }
        }

        switch (command) {
            case "home":
                DisplayUtils.displayHome();
                break;
            case "create":
                handleCreate(commandArgs);
                break;
            case "join":
                handleJoin();
                break;
            case "login":
                handleLogin(commandArgs);
                break;
            case "logout":
                System.out.println("invalid request: missing session token" );
                System.out.println("home: ./app" );
                break;
            case "delete":
                System.out.println("invalid request: missing session token" );
                System.out.println("home: ./app" );
                break;
            case "update":
                System.out.println("invalid request: missing session token" );
                System.out.println("home: ./app" );
                break;
            case "edit":
                System.out.println("invalid request: missing session token" );
                System.out.println("home: ./app" );
                break;
            case "people":
                userService.handlePeople();
                break;
            case "show":
                if (commandArgs.size() < 2) {
                    System.out.println("invalid request: missing username" );
                    System.out.println("home: ./app" );
                } else {
                    userService.handleShowPerson(commandArgs.get(1));
                }
                break;
            case "find":
                handleFind(commandArgs);
                break;
            case "sort":
                userService.handleSort(commandArgs.subList(1, commandArgs.size()));
                break;
            default:
                DisplayUtils.displayInvalidCommand(commandArgs);
        }
    }


    private void handleFind(List<String> commandArgs) {
        if (commandArgs.size() == 1) {
            userService.handleFind("");
            return;
        }

        String pattern = String.join(" ", commandArgs.subList(1, commandArgs.size()));
        pattern = pattern.trim();

        if (pattern.startsWith("\"") && pattern.endsWith("\"")) {
            pattern = pattern.substring(1, pattern.length() - 1).trim();
        }

        System.out.println("Pattern: " + pattern);
        userService.handleFind(pattern);
    }



    private void handleCreate(List<String> commandArgs) {
        String[] requiredFields = {"username", "password", "name", "status"};
        Map<String, String> fieldValues = new HashMap<>();

        for (int i = 1; i < commandArgs.size(); i++) {
            String[] parts = commandArgs.get(i).split("=", 2);
            if (parts.length == 2) {
                String field = parts[0].toLowerCase();
                String value = parts[1].replaceAll("^\"|\"$", "" );
                fieldValues.put(field, value);
            }
        }

        for (String field : requiredFields) {
            if (!fieldValues.containsKey(field)) {
                System.out.println("failed to create: missing " + field);
                return;
            }
        }

        userService.createUser(
                fieldValues.get("username" ),
                fieldValues.get("password" ),
                fieldValues.get("name" ),
                fieldValues.get("status" )
        );
    }

    private void handleJoin() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("New Person" );
        System.out.println("----------" );

        System.out.print("username: " );
        String username = scanner.nextLine();

        System.out.print("password: " );
        String password = scanner.nextLine();

        System.out.print("confirm password: " );
        String confirmPassword = scanner.nextLine();

        System.out.print("name: " );
        String name = scanner.nextLine();

        System.out.print("status: " );
        String status = scanner.nextLine();


        userService.joinUser(username, password, confirmPassword, name, status);
    }

    private void handleLogin(List<String> commandArgs) {
        // print commandArgs
        System.out.println("Command Arguments: " + commandArgs);


        if (commandArgs.size() < 2) {
            System.out.println("invalid request: missing username and password" );
            System.out.println("home: ./app" );
            return;
        }

        String username = commandArgs.get(1);
        String password = commandArgs.size() > 2 ? commandArgs.get(2) : "";

        // Debug output for the password
        System.out.println("Input Password: " + password);

        if (password.isEmpty()) {
            System.out.println("try harder." );
            System.out.println("incorrect username or password" );
            System.out.println("home: ./app" );
            return;
        }

        boolean loginSuccess = userService.loginUser(username, password);

        if (!loginSuccess) {
            System.out.println("access denied: incorrect username or password" );
            System.out.println("home: ./app" );
        }
    }

    private void handleSessionCommand(String sessionToken, String sessionCommand, List<String> commandArgs) {
        switch (sessionCommand) {
            case "logout":
                userService.logoutUser(sessionToken);
                break;
            case "delete":
                userService.deleteUser(sessionToken);
                break;
            case "people":
                userService.handlePeopleWithSession(sessionToken);
                break;
            case "home":
                userService.handleHomeWithSession(sessionToken);
                break;
            case "show":
                if (commandArgs.isEmpty()) {
                    System.out.println("invalid request: missing username" );
                    System.out.println("home: ./app" );
                } else {
                    userService.handleShowPersonWithSession(sessionToken, commandArgs.get(0));
                }
                break;
            case "edit":
                userService.handleEditPerson(sessionToken);
                break;
            case "update":
                System.out.println(commandArgs);
                userService.handleUpdatePerson(sessionToken, commandArgs);
                break;
            case "find":
                if (commandArgs.isEmpty()) {
                    userService.handleFind("");
                } else {
                    String pattern = String.join(" ", commandArgs);
                    userService.handleFind(pattern);
                }
            default:
                System.out.println("Invalid session command: " + sessionCommand);
                System.out.println("access denied: missing session token" );
                DisplayUtils.displayInvalidCommand(commandArgs);
        }
    }
}