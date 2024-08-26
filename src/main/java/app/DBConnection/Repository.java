package app.DBConnection;

import app.models.Person;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Repository {

    private final DBLoader dbLoader;

    public Repository() {
        this.dbLoader = new DBLoader();
    }

    public boolean validateSession(String sessionToken) {
        Set<String> sessionTokens = dbLoader.getSessionTokens();
        return sessionTokens.contains(sessionToken);
    }

    public Person findUserByUsername(String username) {
        List<Person> personsList = dbLoader.getPersonsList();
        for (Person person : personsList) {
            if (person.getUsername().equalsIgnoreCase(username)) {
                return person;
            }
        }
        return null;
    }

    public void addPerson(Person person) {
        String csvData = dbLoader.getRepo();
        List<String> personsList = new ArrayList<>();

        if (csvData != null && !csvData.trim().isEmpty()) {
            String[] rows = csvData.split("\n");
            personsList.addAll(Arrays.asList(rows));
        }

        String newPersonCsv = person.getSessionToken() + "," +
                person.getUsername() + "," +
                person.getPassword() + "," +
                person.getName() + "," +
                person.getStatus() + "," +
                person.getUpdated();

        personsList.add(newPersonCsv);

        StringBuilder finalCsv = new StringBuilder();
        for (String personCsv : personsList) {
            finalCsv.append(personCsv).append("\n");
        }

        try (FileWriter fileWriter = new FileWriter("src/main/resources/database/db.csv")) {
            fileWriter.write(finalCsv.toString());
            System.out.println("New person added to the database.");
        } catch (IOException e) {
            System.out.println("Error writing to database: " + e.getMessage());
        }
    }

}
