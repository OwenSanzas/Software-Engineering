package app.DBConnection;

import app.models.Person;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DBLoader {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DB_FILE_PATH = "src/main/resources/database/db.csv";
    private String csvData;
    private List<Person> personsList;
    private Set<String> sessionTokens;

    public DBLoader() {
        sessionTokens = new HashSet<>();
        personsList = new ArrayList<>();
        loadDatabase();
        parseCSVData();
    }

    private void loadDatabase() {
        try {
            File dbFile = new File(DB_FILE_PATH);
            Scanner reader = new Scanner(dbFile);
            StringBuilder csvBuilder = new StringBuilder();
            while (reader.hasNextLine()) {
                csvBuilder.append(reader.nextLine()).append("\n");
            }
            reader.close();
            csvData = csvBuilder.toString();
        } catch (FileNotFoundException e) {
            System.out.println("Database file not found: " + e.getMessage());
        }
    }

    private void parseCSVData() {
        if (csvData != null && !csvData.trim().isEmpty()) {
            String[] rows = csvData.split("\n");

            for (int i = 1; i < rows.length; i++) {
                String row = rows[i];
                String[] fields = row.split(",");
                if (fields.length == 6) {
                    LocalDateTime updatedTime = LocalDateTime.parse(fields[5].trim(), formatter);
                    Person person = new Person(fields[0].trim(), fields[1].trim(), fields[2].trim(),
                            fields[3].trim(), fields[4].trim(), updatedTime);
                    personsList.add(person);
                    sessionTokens.add(person.getSessionToken());  // 添加 sessionToken 到 set 中
                }
            }
        }
    }

    public String getRepo() {
        return csvData;
    }

    public List<Person> getPersonsList() {
        return personsList;
    }

    public Set<String> getSessionTokens() {
        return sessionTokens;
    }
}
