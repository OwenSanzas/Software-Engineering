package app.DBConnection;

import app.models.Person;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DBLoader {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String dbFilePath;
    private String sessionFilePath;

    private String userData;
    private String sessionData;
    private List<Person> personsList;
    private HashMap<String, Integer> sessionTokens;

    public DBLoader() {
        this.dbFilePath = "src/main/resources/database/db.csv";
        this.sessionFilePath = "src/main/resources/database/session.csv";
        sessionTokens = new HashMap<>();
        personsList = new ArrayList<>();
        loadSessionInfo();
        loadDatabase();
        parseCSVData();
    }

    public DBLoader(String dbFilePath, String sessionFilePath) {
        this.dbFilePath = dbFilePath;
        this.sessionFilePath = sessionFilePath;
        sessionTokens = new HashMap<>();
        personsList = new ArrayList<>();
        loadSessionInfo();
        loadDatabase();
        parseCSVData();
    }

    private void loadSessionInfo() {
        try {
            File dbFile = new File(sessionFilePath);
            Scanner reader = new Scanner(dbFile);
            StringBuilder csvBuilder = new StringBuilder();

            while (reader.hasNextLine()) {
                csvBuilder.append(reader.nextLine()).append("\n");
            }

            reader.close();
            sessionData = csvBuilder.toString();

            String[] rows = sessionData.split("\n");
            for (int i = 1; i < rows.length; i++) {
                String row = rows[i];
                String[] fields = row.split(",");
                if (fields.length == 2) {
                    sessionTokens.put(fields[0].trim(), Integer.parseInt(fields[1].trim()));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Session file not found: " + e.getMessage());
        }
    }

    private void loadDatabase() {
        try {
            File dbFile = new File(dbFilePath);
            Scanner reader = new Scanner(dbFile);
            StringBuilder csvBuilder = new StringBuilder();
            while (reader.hasNextLine()) {
                csvBuilder.append(reader.nextLine()).append("\n");
            }
            reader.close();
            userData = csvBuilder.toString();
        } catch (FileNotFoundException e) {
            System.out.println("Database file not found: " + e.getMessage());
        }
    }

    private void parseCSVData() {
        if (userData != null && !userData.trim().isEmpty()) {
            String[] rows = userData.split("\n");

            for (int i = 1; i < rows.length; i++) {
                String row = rows[i];
                String[] fields = row.split(",");
                if (fields.length == 6) {
                    LocalDateTime updatedTime = LocalDateTime.parse(fields[5].trim(), formatter);
                    Person person = new Person(fields[0].trim(), fields[1].trim(), fields[2].trim(),
                            fields[3].trim(), fields[4].trim(), updatedTime);
                    personsList.add(person);
                }
            }
        }
    }

    public String getRepo() {
        return userData;
    }

    public List<Person> getPersonsList() {
        return personsList;
    }

    public HashMap<String, Integer> getSessionTokens() {
        return sessionTokens;
    }

    public String getSessionData() {
        return sessionData;
    }
}
