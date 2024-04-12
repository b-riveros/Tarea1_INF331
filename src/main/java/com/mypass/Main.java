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
            System.out.println("Bienvenido a MyPass!");

            if (connection.isUsersTableEmpty()) {
                System.out.println("No existen usuarios. Por favor crear una nueva cuenta.");
                boolean created = false;
                while (!created) {
                    // Prompt the user to enter username and password
                    String username = console.readLine("Cree un nombre de usuario: ").trim();
                    char[] passwordArray = console.readPassword("Cree una contraseña maestra: ");
                    String password = new String(passwordArray);

                    connection.createUserAccount(username, password);
                    System.out.println("Usuario creado con exito\n");
                    created = true; // Set created to true to exit the loop after successful creation
                }
            }

            // Keep the login process going until the user logs in successfully
            while (true) {
                try {
                    String username = console.readLine("Ingrese nombre de usuario: ").trim();
                    char[] passwordArray = console.readPassword("Ingrese contraseña: ");
                    String password = new String(passwordArray);
                    int user_id = connection.loginUser(username, password);

                    if (user_id != -1) {
                        System.err.println("Sesion iniciada.");
                        PasswordManagerCLI passwordManager = new PasswordManagerCLI(connection);
                        passwordManager.printHelp();

                        boolean running = true;
                        while (running) {
                            String command = console.readLine("Ingrese comando: ").trim();

                            if (command.equalsIgnoreCase("exit")) {
                                running = false;
                                System.out.println("Abandonando MyPass. Adios!!");
                                return; // Exit the main method

                            } else {
                                passwordManager.processCommands(command, user_id);
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    System.err.println("Inicio de sesión falló. Intente nuevamente.");

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
