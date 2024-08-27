package app;
import app.controller.Controller;

import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {
        Controller controller = new Controller();
        List<String> commandArgs;

        if (args.length == 1) {
            String[] splitArgs = args[0].split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            commandArgs = Arrays.asList(splitArgs);

            controller.handleRequest(commandArgs);
        } else if (args.length == 0) {
            controller.handleRequest(commandArgs = List.of());
        }
    }
}