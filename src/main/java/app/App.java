package app;

import app.controller.Controller;
import app.auth.Authenticator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {
        Controller controller = new Controller();
        Authenticator authenticator = new Authenticator();
        List<String> commandArgs;

        if (args.length == 1) {
            String commandInput = args[0];
            if (commandInput.startsWith("login")) {
                String[] loginArgs = commandInput.split(" ", 3);
                commandArgs = Arrays.asList(loginArgs);
            } else if (commandInput.startsWith("create")) {
                String[] splitArgs = commandInput.split(" ");
                List<String> createArgs = new ArrayList<>();

                for(String arg : splitArgs) {
                    // combine arguments that divied by space
                    if (arg.startsWith("username=") || arg.startsWith("password=") ||
                            arg.startsWith("name=") || arg.startsWith("status=")) {
                        createArgs.add(arg);
                    } else {
                        if (!createArgs.isEmpty()) {
                            createArgs.set(createArgs.size() - 1, createArgs.get(createArgs.size() - 1) + " " + arg);
                        }
                    }
                }

                // count the number of double quotes in the value
                for (String arg : createArgs) {
                    String value = arg.split("=", 2)[1];
                    // if current arg is username
                    if (arg.startsWith("username=")) {
                        if (value.chars().filter(ch -> ch == '\"').count() > 2) {
                            System.out.println("failed to create: invalid username");
                            return;
                        }

                        if (arg.contains(" ")) {
                            System.out.println("failed to create: invalid username");
                            return;
                        }
                    }

                    // if current arg is password
                    if (arg.startsWith("password=")) {
                        if (value.chars().filter(ch -> ch == '\"').count() > 2) {
                            System.out.println("failed to create: password contains double quote");
                            return;
                        }
                    }

                    // if current arg is name
                    if (arg.startsWith("name=")) {
                        if (value.chars().filter(ch -> ch == '\"').count() > 2) {
                            System.out.println("failed to create: name contains double quote");
                            return;
                        }
                    }

                    // if current arg is status
                    if (arg.startsWith("status=")) {
                        if (value.chars().filter(ch -> ch == '\"').count() > 2) {
                            System.out.println("failed to create: status contains double quote");
                            return;
                        }
                    }

                }


                createArgs.add(0, "create");
                commandArgs = createArgs;



            } else {
                String[] splitArgs = commandInput.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                commandArgs = Arrays.asList(splitArgs);
            }

            controller.handleRequest(commandArgs);
        } else if (args.length == 0) {
            controller.handleRequest(commandArgs = List.of());
        }
    }
}
