package com.mypass;

import java.io.Console;
import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (DatabaseConnection connection = new DatabaseConnection()) {
            Console console = System.console();
            if (console == null) {
                System.err.println("Console not available. Exiting.");
                return;
            }
            System.out.println("Welcome to MyPass Manager CLI!");

            if (connection.isUsersTableEmpty()) {
                System.out.println("No users found. Please create a new account.");
                boolean created = false;
                while (!created) {
                    // Prompt the user to enter username and password
                    String username = console.readLine("Enter username: ").trim();
                    char[] passwordArray = console.readPassword("Enter password: ");
                    String password = new String(passwordArray);

                    connection.createUserAccount(username, password);
                    System.out.println("Account created successfully\n.");
                    created = true; // Set created to true to exit the loop after successful creation
                }
            }

            // Keep the login process going until the user logs in successfully
            while (true) {
                try {
                    String username = console.readLine("Enter username: ").trim();
                    char[] passwordArray = console.readPassword("Enter password: ");
                    String password = new String(passwordArray);
                    int user_id = connection.loginUser(username, password);

                    if (user_id != -1) {
                        System.err.println("Sesion iniciada.");
                        PasswordManagerCLI passwordManager = new PasswordManagerCLI(connection);

                        boolean running = true;
                        while (running) {
                            passwordManager.printHelp();
                            String command = console.readLine("Enter command: ").trim();

                            if (command.equalsIgnoreCase("exit")) {
                                running = false;
                                System.out.println("Exiting MyPass Manager CLI. Goodbye!");
                            } else {
                                // For all other commands, including "use", process them using the processCommands method
                                passwordManager.processCommands(command, user_id);
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    System.err.println("Login failed. Please try again.");
                }
            }

        } catch (IOException e) {
            // Handle the IOException
            // Logging?
            e.printStackTrace();
        } catch (SQLException | ClassNotFoundException e) {
            // Handle other exceptions if needed
            // Logging?
            e.printStackTrace();
        }
    }
}
