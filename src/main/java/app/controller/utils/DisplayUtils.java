package app.controller.utils;
import java.util.*;

public class DisplayUtils {
    public static void displayHome() {
        System.out.println("Welcome to the App!");
        System.out.println();
        System.out.println("login: ./app 'login <username> <password>'");
        System.out.println("join: ./app 'join'");
        System.out.println("create: ./app 'create username=\"<value>\" password=\"<value>\" name=\"<value>\" status=\"<value>\"'");
        System.out.println("show people: ./app 'people'");
    }

    public static void displayInvalidCommand(List<String> commandArgs) {
        System.out.println("try harder.");
        System.out.println("resource not found");
        System.out.println("home: ./app");
    }
}