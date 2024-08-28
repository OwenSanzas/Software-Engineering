package app.services;

import app.models.Person;
import app.DBConnection.Repository;
import app.services.utils.MsgDisplay;
import java.util.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class UserService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss" );
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;

    private final MsgDisplay msgDisplay;
    private final SessionService sessionService;
    private final Repository repository;

    public UserService() {
        this.msgDisplay = new MsgDisplay();
        this.sessionService = new SessionService();
        this.repository = new Repository();
    }

    public UserService(Repository repository) {
        this.msgDisplay = new MsgDisplay();
        this.sessionService = new SessionService(repository);
        this.repository = repository;
    }

    public void handleSort(List<String> commandArgs) {
        List<Person> people = repository.getPersonsList();

        String sortField = "updated";
        boolean ascending = false;

        if (!commandArgs.isEmpty()) {
            sortField = commandArgs.get(0).toLowerCase();

            if (commandArgs.size() > 1) {
                String sortOrder = commandArgs.get(1).toLowerCase();
                if ("asc".equals(sortOrder)) {
                    ascending = true;
                } else if (!"desc".equals(sortOrder)) {
                    System.out.println("not found");
                    return;
                }
            } else {
                ascending = !sortField.equals("updated");
            }
        }

        Comparator<Person> comparator;
        switch (sortField) {
            case "username" -> comparator = Comparator.comparing(Person::getUsername, String.CASE_INSENSITIVE_ORDER);
            case "name" -> comparator = Comparator.comparing(Person::getName, String.CASE_INSENSITIVE_ORDER);
            case "status" -> comparator = Comparator.comparing(Person::getStatus, String.CASE_INSENSITIVE_ORDER);
            case "updated" -> comparator = Comparator.comparing(Person::getUpdated);
            default -> {
                System.out.println("not found");
                return;
            }
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        people.sort(comparator);

        String sortDirection = switch (sortField) {
            case "updated" -> ascending ? "oldest" : "newest";
            default -> ascending ? "a-z" : "z-a";
        };
        System.out.printf("People (sorted by %s, %s)%n", sortField, sortDirection);
        System.out.println("----------------------------");

        for (Person person : people) {
            System.out.printf("%s @%s (./app 'show %s')%n", person.getName(), person.getUsername(), person.getUsername());
            System.out.println("  " + person.getStatus());
            System.out.println("  @ " + person.getUpdated());
        }

        printPeopleFooter();
    }

    public void handleEditPerson(String sessionToken) {
        Person person = repository.findUserBySessionToken(sessionToken);

        if (person == null) {
            System.out.println("invalid request: invalid session token");
            System.out.println("home: ./app");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("Edit Person");
        System.out.println("-----------");
        System.out.println("leave blank to keep [current value]");
        System.out.print("name [" + person.getName() + "]: ");
        String newName = scanner.nextLine().trim();

        System.out.print("status [" + person.getStatus() + "]: ");
        String newStatus = scanner.nextLine().trim();

        boolean nameUpdated = false;
        boolean statusUpdated = false;

        if (!newName.isEmpty() && !newName.equals(person.getName())) {
            if (!doubleQuoteAndLengthCheck(newName, "name")) {
                return;
            }
            person.setName(newName);
            nameUpdated = true;
        }

        if (!newStatus.isEmpty() && !newStatus.equals(person.getStatus())) {
            if (!doubleQuoteAndLengthCheck(newStatus, "status")) {
                return;
            }
            person.setStatus(newStatus);
            statusUpdated = true;
        }

        if (!nameUpdated && !statusUpdated) {
            System.out.println("No changes made.");
            displayPersonWithOptions(person, sessionToken);
            return;
        }

        person.setUpdated(LocalDateTime.now());
        repository.updatePerson(person);

        if (nameUpdated && statusUpdated) {
            System.out.println("[name and status updated]");
        } else if (nameUpdated) {
            System.out.println("[name updated]");
        } else if (statusUpdated) {
            System.out.println("[status updated]");
        }

        displayPersonWithOptions(person, sessionToken);
    }


    public void handleUpdatePerson(String sessionToken, List<String> commandArgs) {
        Person person = repository.findUserBySessionToken(sessionToken);

        if (person == null) {
            System.out.println("invalid request: invalid session token");
            System.out.println("home: ./app");
            return;
        }

        if (commandArgs.isEmpty()) {
            System.out.println("failed to update: missing name and status");
            return;
        }

        Map<String, String> updates = new HashMap<>();
        for (String arg : commandArgs) {
            String[] parts = arg.split("=", 2);
            if (parts.length == 2) {
                String field = parts[0].toLowerCase();
                String value = parts[1].replaceAll("^\"|\"$", "");
                updates.put(field, value);
            }
        }

        boolean nameUpdated = false;
        boolean statusUpdated = false;
        boolean nameChanged = updates.containsKey("name") && !person.getName().equals(updates.get("name"));
        boolean statusChanged = updates.containsKey("status") && !person.getStatus().equals(updates.get("status"));

        // Check and update the name if it's changed and valid
        if (nameChanged) {
            String newName = updates.get("name");
            if (!doubleQuoteAndLengthCheck(newName, "name-update")) {
                return;
            }
            person.setName(newName);
            nameUpdated = true;
        }

        // Check and update the status if it's changed and valid
        if (statusChanged) {
            String newStatus = updates.get("status");
            if (!doubleQuoteAndLengthCheck(newStatus, "status-update")) {
                return;
            }
            person.setStatus(newStatus);
            statusUpdated = true;
        }

        // If nothing has been updated
        if (!nameUpdated && !statusUpdated) {
            System.out.println("name: " + person.getName());
            System.out.println("status: " + person.getStatus());
            System.out.println("updated: " + person.getUpdated());
            displayPersonWithOptions(person, sessionToken);
            return;
        }

        // Update the timestamp only if there was a change
        person.setUpdated(LocalDateTime.now());
        repository.updatePerson(person);

        // Display appropriate message based on what was updated
        if (nameUpdated && statusUpdated) {
            System.out.println("[name and status updated]");
        } else if (nameUpdated) {
            System.out.println("[name updated]");
        } else if (statusUpdated) {
            System.out.println("[status updated]");
        }

        // Display the updated person with available options
        displayPersonWithOptions(person, sessionToken);
    }


    public void displayPersonWithOptions(Person person, String sessionToken) {
        System.out.println("Person");
        System.out.println("------");
        System.out.println("name: " + person.getName());
        System.out.println("username: " + person.getUsername());
        System.out.println("status: " + person.getStatus());
        System.out.println("updated: " + person.getUpdated());
        System.out.println();

        System.out.println("edit: ./app 'session " + sessionToken + " edit'");
        System.out.println("update: ./app 'session " + sessionToken + " update (name=\"<value>\"|status=\"<value>\")+'");
        System.out.println("delete: ./app 'session " + sessionToken + " delete'");
        System.out.println("logout: ./app 'session " + sessionToken + " logout'");
        System.out.println("people: ./app '[session " + sessionToken + " ]people'");
    }

    public void handleFind(String pattern) {
        System.out.println("original pattern: " + pattern);
        List<Person> people = repository.getPersonsList();
        HashMap<String, Integer> sessionTokenMap = repository.getSessionTokens();

        if (pattern.isEmpty() || sessionTokenMap.containsKey(pattern)) {
            System.out.println("People (find all)");
        } else if (pattern.contains(":")) {
            String[] parts = pattern.split(":", 2);
            String field = parts[0].trim().toLowerCase();
            String value = "";

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            } else {
                value = parts[1].trim();
            }

            System.out.println("Searching field: " + field + " with value: " + value);

            switch (field) {
                case "username":
                    String finalValue = value;
                    people = people.stream()
                            .filter(p -> p.getUsername().contains(finalValue))
                            .collect(Collectors.toList());
                    System.out.println("People (find \"" + value + "\" in username)");
                    break;
                case "name":
                    String finalValue1 = value;
                    people = people.stream()
                            .filter(p -> p.getName().contains(finalValue1))
                            .collect(Collectors.toList());
                    System.out.println("People (find \"" + value + "\" in name)");
                    break;
                case "status":
                    String finalValue2 = value;
                    people = people.stream()
                            .filter(p -> p.getStatus().contains(finalValue2))
                            .collect(Collectors.toList());
                    System.out.println("People (find \"" + value + "\" in status)");
                    break;
                case "updated":
                    String finalValue3 = value;
                    people = people.stream()
                            .filter(p -> p.getUpdated().contains(finalValue3))
                            .collect(Collectors.toList());
                    System.out.println("People (find \"" + value + "\" in updated)");
                    break;
                default:
                    System.out.println("People (find \"" + pattern + "\" in any)");
                    String finalValue4 = pattern;
                    people = people.stream()
                            .filter(p -> p.getUsername().contains(finalValue4) ||
                                    p.getName().contains(finalValue4) ||
                                    p.getStatus().contains(finalValue4) ||
                                    p.getUpdated().contains(finalValue4))
                            .collect(Collectors.toList());
            }
        } else {
            String searchPattern = pattern.trim();
            people = people.stream()
                    .filter(p -> p.getUsername().contains(searchPattern) ||
                            p.getName().contains(searchPattern) ||
                            p.getStatus().contains(searchPattern) ||
                            p.getUpdated().contains(searchPattern))
                    .collect(Collectors.toList());

            System.out.println("People (find \"" + searchPattern + "\" in any)");
        }

        if (people.isEmpty()) {
            System.out.println("No one is here...");
        } else {
            printPeople(people);
        }

        printPeopleFooter();
    }


    private void printPeople(List<Person> people) {
        System.out.println("------");
        for (Person person : people) {
            System.out.println(person.getName() + " @" + person.getUsername() + " (./app 'show " + person.getUsername() + "')");
            System.out.println("  " + person.getStatus());
            System.out.println("  @ " + person.getUpdated());
        }
    }

    private void printPeopleFooter() {
        System.out.println();
        System.out.println("find: ./app 'find <pattern>'");
        System.out.println("sort: ./app 'sort[ username|name|status|updated[ asc|desc]]'");
        System.out.println("people: ./app 'people'");
        System.out.println("join: ./app 'join'");
        System.out.println("create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'");
        System.out.println("home: ./app");
    }

    public void handleShowPerson(String username) {
        Person person = repository.findUserByUsername(username);

        if (person == null) {
            System.out.println("Person not found: " + username);
            System.out.println("home: ./app");
            return;
        }

        System.out.println("Person");
        System.out.println("------");
        System.out.println("name: " + person.getName());
        System.out.println("username: " + person.getUsername());
        System.out.println("status: " + person.getStatus());
        System.out.println("updated: " + person.getUpdated());
        System.out.println();
        System.out.println("people: ./app 'people'");
        System.out.println("home: ./app");
    }

    public void handleShowPersonWithSession(String sessionToken, String username) {
        Person person = repository.findUserByUsername(username);
        Person user = repository.findUserBySessionToken(sessionToken);

        if (person == null) {
            System.out.println("Person not found: " + username);
            System.out.println("home: ./app");
            return;
        }

        // Check if the session token belongs to the person being viewed
        if (!sessionToken.equals(person.getSessionToken())) {
            displayPersonInfoWhenSessionNotMatch(user, person); // Display basic info without session features
            return;
        }

        // If session token matches, display full options
        System.out.println("Person");
        System.out.println("------");
        System.out.println("name: " + person.getName());
        System.out.println("username: " + person.getUsername());
        System.out.println("status: " + person.getStatus());
        System.out.println("updated: " + person.getUpdated());
        System.out.println();
        System.out.println("edit: ./app 'session " + sessionToken + " edit'");
        System.out.println("update: ./app 'session " + sessionToken + " update (name=\"<value>\"|status=\"<value>\")+\'");
        System.out.println("delete: ./app 'session " + sessionToken + " delete'");
        System.out.println("logout: ./app 'session " + sessionToken + " logout'");
        System.out.println("people: ./app '[session " + sessionToken + " ]people'");
        System.out.println("home: ./app ['session " + sessionToken + "']");
    }

    private void displayPersonInfoWhenSessionNotMatch(Person user, Person person) {
        System.out.println("Person");
        System.out.println("------");
        System.out.println("name: " + person.getName());
        System.out.println("username: " + person.getUsername());
        System.out.println("status: " + person.getStatus());
        System.out.println("updated: " + person.getUpdated());
        System.out.println();
        System.out.println("people: ./app '[session " + user.getSessionToken() + " ]people'");
        System.out.println("logout: ./app 'session " + user.getSessionToken() + " logout'");
        System.out.println("home: ./app ['session " + user.getSessionToken() + "']");
    }

    public void handleHomeWithSession(String sessionToken) {
        Person user = repository.findUserBySessionToken(sessionToken);
        if (user == null) {
            System.out.println("invalid request: invalid session token");
            System.out.println("home: ./app");
            return;
        }

        System.out.println("Welcome back to the App, " + user.getName() + "!");
        System.out.println("\"" + user.getStatus() + "\"");
        System.out.println();
        System.out.println("edit: ./app 'session " + sessionToken + " edit'");
        System.out.println("update: ./app 'session " + sessionToken + " update (name=\"<value>\"|status=\"<value>\")+\'");
        System.out.println("logout: ./app 'session " + sessionToken + " logout'");
        System.out.println("people: ./app '[session " + sessionToken + " ]people'");
    }

    public void handlePeopleWithSession(String sessionToken) {
        List<Person> people = repository.getPersonsList();

        if (people.isEmpty()) {
            System.out.println("People");
            System.out.println("No one is here...");
            printPeopleFooter(sessionToken);
            return;
        }

        System.out.println("People");
        System.out.println("------");
        for (Person person : people) {
            System.out.println(person.getName() + " @" + person.getUsername() + " (./app 'show " + person.getUsername() + "')");
            System.out.println("  " + person.getStatus());
            System.out.println("  @ " + person.getUpdated());
            // Adding edit option for the person associated with the current session
            if (person.getSessionToken().equals(sessionToken)) {
                System.out.println("  edit: ./app 'session " + sessionToken + " edit'");
            }
        }
        printPeopleFooter(sessionToken);
    }

    public void handlePeople() {
        List<Person> people = repository.getPersonsList();

        people.sort(Comparator.comparing(Person::getUpdated).reversed());

        if (people.isEmpty()) {
            System.out.println("People");
            System.out.println("No one is here...");
            printPeopleFooter("");
            return;
        }

        System.out.println("People");
        System.out.println("------");
        for (Person person : people) {
            System.out.println(person.getName() + " @" + person.getUsername() + " (./app 'show " + person.getUsername() + "')");
            System.out.println("  " + person.getStatus());
            System.out.println("  @ " + person.getUpdated());
        }
        printPeopleFooter("");
    }

    private void printPeopleFooter(String sessionToken) {
        if (sessionToken.length() == 0) {
            System.out.println();
            System.out.println("find: ./app 'find <pattern>'");
            System.out.println("sort: ./app 'sort[ username|name|status|updated[ asc|desc]]'");
            System.out.println("join: ./app 'join'");
            System.out.println("create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'");
            System.out.println("home: ./app");
            return;
        }

        System.out.println();
        System.out.println("find: ./app 'find <pattern>'");
        System.out.println("sort: ./app 'sort[ username|name|status|updated[ asc|desc]]'");
        System.out.println("update: ./app 'session " + sessionToken + " update (name=\"<value>\"|status=\"<value>\")+\'");
        System.out.println("home: ./app ['session " + sessionToken + "']");
    }

    public Person createUser(String username, String password, String name, String status) {
        if (!isUsernameValid(username)) {
            System.out.println("try harder.");
            System.out.println("failed to create: invalid username");
            System.out.println("home: ./app");
            return null;
        }


        if (!doubleQuoteAndLengthCheck(name, "name") || !doubleQuoteAndLengthCheck(password, "password") ||
                !doubleQuoteAndLengthCheck(status, "status")) {

            return null;
        }

        if (repository.findUserByUsername(username) != null) {
            System.out.println("try harder." );
            System.out.println("failed to create: " + username + " is already registered" );
            System.out.println("home: ./app" );
            return null;
        }

        String sessionToken = sessionService.createSession();
        Person newUser = new Person(sessionToken, username.toLowerCase(), password, name, status, LocalDateTime.now());

        repository.addPerson(newUser);
        msgDisplay.createUserDisplay(newUser);

        return newUser;
    }

    public boolean deleteUser(String sessionToken) {
        repository.deletePerson(sessionToken);
        sessionService.deleteSession(sessionToken);

        System.out.println("[account deleted]");
        System.out.println("Welcome to the App!");
        System.out.println();
        System.out.println("login: ./app 'login <username> <password>'");
        System.out.println("join: ./app 'join'");
        System.out.println("create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'");
        System.out.println("show people: ./app 'people'");

        return true;
    }

    private boolean isUsernameValid(String username) {
        if (username.length() < MIN_USERNAME_LENGTH) {
            System.out.println("failed to create: username is too short");
            System.out.println("home: ./app");
            return false;
        }

        if (username.length() > MAX_USERNAME_LENGTH) {
            System.out.println("failed to create: username is too long");
            System.out.println("home: ./app");
            return false;
        }

        String illegalChars = " @.,^,-,&,+,'<,>,!,%,|,\\,\"";
        for (char c : illegalChars.toCharArray()) {
            if (username.indexOf(c) >= 0 || username.contains(" ")) {
                return false;
            }
        }
        return true;
    }

    private boolean doubleQuoteAndLengthCheck(String value, String field) {
        if (value.contains("\"")) {
            System.out.println("failed to create: " + field + " contains double quote");
            return false;
        }

        switch (field) {
            case "name":
                if (value.length() < 1) {
                    System.out.println("failed to create: name is too short");
                    return false;
                }
                if (value.length() > 30) {
                    System.out.println("failed to create: name is too long");
                    return false;
                }
                break;
            case "name-update":
                if (value.length() < 1) {
                    System.out.println("failed to update: name is too short");
                    return false;
                }
                if (value.length() > 30) {
                    System.out.println("failed to update: name is too long");
                    return false;
                }
                break;
            case "password":
                if (value.length() <= 3) {
                    System.out.println("failed to create: password is too short");
                    return false;
                }

                if (value.length() > 50) {
                    System.out.println("failed to create: password is too long");
                    return false;
                }

                break;
            case "status":
                if (value.length() < 1) {
                    System.out.println("failed to create: status is too short");
                    return false;
                }
                if (value.length() > 100) {
                    System.out.println("failed to create: status is too long");
                    return false;
                }
                break;
            case "status-update":
                if (value.length() < 1) {
                    System.out.println("failed to update: status is too short");
                    return false;
                }
                if (value.length() > 100) {
                    System.out.println("failed to update: status is too long");
                    return false;
                }
                break;
            default:
                break;
        }

        return true;
    }


    public Person joinUser(String username, String password, String confirmPassword, String name, String status) {
        if (!isUsernameValid(username)) {
            System.out.println("try harder.");
            System.out.println("failed to create: invalid username");
            System.out.println("home: ./app");
            return null;
        }

        if (!doubleQuoteAndLengthCheck(name, "name") || !doubleQuoteAndLengthCheck(password, "password") ||
                !doubleQuoteAndLengthCheck(status, "status")) {

            return null;
        }

        if (repository.findUserByUsername(username) != null) {
            System.out.println("try harder." );
            System.out.println("failed to create: " + username + " is already registered" );
            System.out.println("home: ./app" );
            return null;
        }

        if (!password.equals(confirmPassword)) {
            System.out.println("try harder." );
            System.out.println("failed to join: passwords do not match" );
            System.out.println("home: ./app" );
            return null;
        }

        String sessionToken = sessionService.createSession();
        Person newUser = new Person(sessionToken, username.toLowerCase(), password, name, status, LocalDateTime.now());

        repository.addPerson(newUser);
        msgDisplay.createUserDisplay(newUser);
        return newUser;
    }

    public boolean loginUser(String username, String password) {
        Person user = repository.findUserByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            return false;
        }

        System.out.println("Welcome back to the App, " + user.getName() + "!");
        System.out.println("\"" + user.getStatus() + "\"");

        String sessionToken = user.getSessionToken();
        sessionService.activeSession(sessionToken);

        System.out.println("edit: ./app 'session " + sessionToken + " edit'");
        System.out.println("update: ./app 'session " + sessionToken + " update (name=\"<value>\"|status=\"<value>\")+\'");
        System.out.println("delete: ./app 'session " + sessionToken + " delete'");
        System.out.println("logout: ./app 'session " + sessionToken + " logout'");
        System.out.println("people: ./app '[session " + sessionToken + " ]people'");

        return true;
    }

    public void logoutUser(String sessionToken) {
        if (sessionToken == null) {
            System.out.println("invalid request: missing session token" );
            return;
        }

        sessionService.disableSession(sessionToken);
        System.out.println("[you are now logged out]" );
        msgDisplay.displayHome();
    }

}
