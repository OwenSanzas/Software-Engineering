package app;
import app.controller.Controller;

import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {

        if (args.length == 1) {
            String[] splitArgs = args[0].split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            List<String> commandArgs = Arrays.asList(splitArgs);

            Controller controller = new Controller();
            controller.handleRequest(commandArgs);
        } else {
            System.out.println("No command provided.");
        }
    }
}