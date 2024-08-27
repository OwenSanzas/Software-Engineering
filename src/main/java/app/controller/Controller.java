package app.controller;

import app.auth.*;
import app.services.*;
import app.controller.utils.DisplayUtils;

import java.util.*;

public class Controller {

    private final UserService userService;
    private final SessionService sessionService;
    private final Authenticator auth;

    public Controller() {
        userService = new UserService();
        sessionService = new SessionService();
        auth = new Authenticator();
    }

    public void handleRequest(List<String> commandArgs) {
        if (commandArgs.isEmpty()) {
            DisplayUtils.displayHome();
            return;
        }

        String command = commandArgs.get(0).toLowerCase();

        if ("session".equals(command)) {
            if (commandArgs.size() >= 3) {
                String sessionToken = commandArgs.get(1);
                String sessionCommand = commandArgs.get(2).toLowerCase();
                if (auth.validateSession(sessionToken)) {
                    handleSessionCommand(sessionToken, sessionCommand, commandArgs.subList(3, commandArgs.size()));
                } else {
                    System.out.println("try harder.");
                    System.out.println("invalid request: invalid session token");
                    System.out.println("home: ./app");
                }
            } else {
                DisplayUtils.displayInvalidCommand(commandArgs);
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
            default:
                DisplayUtils.displayInvalidCommand(commandArgs);
        }
    }

    private void handleCreate(List<String> commandArgs) {
        String[] requiredFields = {"username", "password", "name", "status"};
        Map<String, String> fieldValues = new HashMap<>();

        for (int i = 1; i < commandArgs.size(); i++) {
            String[] parts = commandArgs.get(i).split("=", 2);
            if (parts.length == 2) {
                String field = parts[0].toLowerCase();
                String value = parts[1].replaceAll("^\"|\"$", "");
                fieldValues.put(field, value);
            }
        }

        for (String field : requiredFields) {
            if (!fieldValues.containsKey(field)) {
                System.out.println("Missing required field: " + field);
                System.out.println("Usage: create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"");
                return;
            }
        }

        userService.createUser(
                fieldValues.get("username"),
                fieldValues.get("password"),
                fieldValues.get("name"),
                fieldValues.get("status")
        );
    }

    private void handleJoin() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("New Person");
        System.out.println("----------");

        System.out.print("username: ");
        String username = scanner.nextLine();

        System.out.print("password: ");
        String password = scanner.nextLine();

        System.out.print("confirm password: ");
        String confirmPassword = scanner.nextLine();

        System.out.print("name: ");
        String name = scanner.nextLine();

        System.out.print("status: ");
        String status = scanner.nextLine();

        userService.joinUser(username, password, confirmPassword, name, status);
    }

    private void handleLogin(List<String> commandArgs) {
        if (commandArgs.size() < 3) {
            System.out.println("Usage: login <username> <password>");
            return;
        }

        String username = commandArgs.get(1);
        String password = commandArgs.get(2);

        userService.loginUser(username, password);
    }

    private void handleSessionCommand(String sessionToken, String sessionCommand, List<String> commandArgs) {
        if (!sessionService.validateSession(sessionToken)) {
            System.out.println("try harder.");
            System.out.println("invalid request: invalid session token");
            System.out.println("home: ./app");
            return;
        }


        switch (sessionCommand) {
            case "logout":
                userService.logoutUser(sessionToken);
                break;
            default:
                System.out.println("Invalid session command: " + sessionCommand);
                DisplayUtils.displayInvalidCommand(commandArgs);
        }
    }
}