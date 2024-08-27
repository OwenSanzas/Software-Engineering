package app.DBConnection;

import app.models.Person;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Repository {
    private final int ACTIVE = 1;
    private final int INACTIVE = 0;
    private final int DELETE = -1;

    private final DBLoader dbLoader;
    HashMap<String, Integer> sessionTokens;
    List<Person> personsList;
    String userData;
    String sessionData;
    String sessionFilePath;
    String dbFilePath;



    public Repository() {
        this.dbLoader = new DBLoader();
        this.dbFilePath = "src/main/resources/database/db.csv";
        this.sessionFilePath = "src/main/resources/database/session.csv";
        dbInit();
    }

    public Repository(String dbFilePath, String sessionFilePath) {
        this.dbLoader = new DBLoader(dbFilePath, sessionFilePath);
        this.dbFilePath = dbFilePath;
        this.sessionFilePath = sessionFilePath;
        dbInit();
    }

    private void dbInit() {
        sessionTokens = dbLoader.getSessionTokens();
        personsList = dbLoader.getPersonsList();
        userData = dbLoader.getRepo();
        sessionData = dbLoader.getSessionData();
    }

    public boolean activeSession(String sessionToken) {
        sessionTokens.put(sessionToken, 1);
        updateSessionFile(sessionToken, ACTIVE);

        return true;
    }

    public boolean disableSession(String sessionToken) {
        sessionTokens.put(sessionToken, 0);
        updateSessionFile(sessionToken, INACTIVE);

        return true;
    }

    public boolean deleteSession(String sessionToken) {
        sessionTokens.remove(sessionToken);
        updateSessionFile(sessionToken, DELETE);

        return true;
    }


    public void updateSessionFile(String sessionToken, int operation) {
        StringBuilder updatedCsv = new StringBuilder();
        boolean found = false;

        String[] rows = sessionData.split("\n");

        updatedCsv.append(rows[0]).append("\n");

        for (int i = 1; i < rows.length; i++) {
            String line = rows[i];
            if (line.startsWith(sessionToken + ",")) {
                if (operation != DELETE) {
                    updatedCsv.append(sessionToken).append(",").append(operation).append("\n");
                    sessionTokens.put(sessionToken, operation);
                }
                found = true;
            } else {
                updatedCsv.append(line).append("\n");
            }
        }

        if (!found && operation != DELETE) {
            updatedCsv.append(sessionToken).append(",").append(operation).append("\n");
        }

        try (FileWriter fileWriter = new FileWriter(sessionFilePath)) {
            fileWriter.write(updatedCsv.toString());
            sessionData = updatedCsv.toString();
        } catch (IOException e) {
            System.out.println("Error updating session file: " + e.getMessage());
        }
    }


    public boolean validateSession(String sessionToken) {
        if (!sessionTokens.containsKey(sessionToken)) {
            return false;
        }

        return sessionTokens.get(sessionToken) == 1;
    }

    public Person findUserByUsername(String username) {
        for (Person person : personsList) {
            if (person.getUsername().equalsIgnoreCase(username)) {
                return person;
            }
        }
        return null;
    }

    public void addPerson(Person person) {
        personsList.add(person);

        List<String> userList = new ArrayList<>();
        if (userData != null && !userData.trim().isEmpty()) {
            String[] rows = userData.split("\n");
            userList.addAll(Arrays.asList(rows));
        }

        String newPersonCsv = person.getSessionToken() + "," +
                person.getUsername() + "," +
                person.getPassword() + "," +
                person.getName() + "," +
                person.getStatus() + "," +
                person.getUpdated();

        userList.add(newPersonCsv);

        StringBuilder finalCsv = new StringBuilder();
        for (String personCsv : userList) {
            finalCsv.append(personCsv).append("\n");
        }

        try (FileWriter fileWriter = new FileWriter(dbFilePath)) {
            fileWriter.write(finalCsv.toString());
        } catch (IOException e) {
            System.out.println("Error writing to database: " + e.getMessage());
        }
    }



}
