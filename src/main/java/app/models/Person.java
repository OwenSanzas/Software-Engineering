package app.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Person {
    private String sessionToken;
    private String username;
    private String password;
    private String name;
    private String status;
    private LocalDateTime updated;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Person(String sessionToken, String username, String password, String name, String status, LocalDateTime updated) {
        this.sessionToken = sessionToken;
        this.username = username;
        this.password = password;
        this.name = name;
        this.status = status;
        this.updated = updated;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getUpdated() {
        return updated.format(formatter);
    }

    public String getPassword() { return password; }

    public void setName(String name) {  this.name = name; }

    public void setStatus(String status) {  this.status = status; }

    public void setUpdated(LocalDateTime updated) {  this.updated = updated; }

    public void setPassword(String password) {  this.password = password; }

    public void setSessionToken(String sessionToken) {  this.sessionToken = sessionToken; }
}
