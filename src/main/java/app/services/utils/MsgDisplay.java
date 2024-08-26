package app.services.utils;

import app.models.Person;

import java.util.List;

public class MsgDisplay {

    public MsgDisplay() {
    }


    public void createUserDisplay(Person newUser) {
        String sessionToken = newUser.getSessionToken();
        System.out.println("[account created]");
        System.out.println("Person");
        System.out.println("------");
        System.out.println("name: " + newUser.getName());
        System.out.println("username: " + newUser.getUsername());
        System.out.println("status: " + newUser.getStatus());
        System.out.println("updated: " + newUser.getUpdated());
        System.out.println();
        System.out.println("edit: ./app 'session " + sessionToken + " edit'");
        System.out.println("update: ./app 'session " + sessionToken + " update (name=\"<value>\"|status=\"<value>\")+\'");
        System.out.println("delete: ./app 'session " + sessionToken + " delete'");
        System.out.println("logout: ./app 'session " + sessionToken + " logout'");
        System.out.println("people: ./app '[session " + sessionToken + " ]people'");
        System.out.println("home: ./app ['session " + sessionToken + "']");
    }

    public void displayHome() {
        System.out.println("Welcome to the App!");
        System.out.println();
        System.out.println("login: ./app 'login <username> <password>'");
        System.out.println("join: ./app 'join'");
        System.out.println("create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'");
        System.out.println("show people: ./app 'people'");
    }

    public void displayInvalidCommand(List<String> commandArgs) {
        System.out.println("try harder.");
        System.out.println("resource not found");
        System.out.println("home: ./app");
    }

}
