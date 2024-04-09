package com.mypass;

import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PasswordManagerCLI {

    private DatabaseConnection connection;
    private static final Logger logger = LogManager.getLogger(PasswordManagerCLI.class);

    public PasswordManagerCLI(DatabaseConnection connection) {
        this.connection = connection;
    }


    public void processCommands(String userInput, int user_id) throws SQLException {
        String[] args = userInput.split("\\s+");

        if (args.length < 2) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();
        String name = args[1];

        switch (command) {
            case "add":
                if (args.length < 3) {
                    System.out.println("Error: Missing password value for add command.");
                    return;
                }
                String password = args[2];
                processAddPassword(name, password, user_id);
                break;
            case "get":
                processGetPassword(name);
                break;
            case "remove":
                processRemovePassword(name);
                break;
            case "rename":
                if (args.length < 3) {
                    System.out.println("Error: Missing new name for rename command.");
                    return;
                }
                String newName = args[2];
                processRenamePassword(name, newName);
                break;
            default:
                System.out.println("Error: Invalid command. Use 'help' for usage information.");
        }
    }

    private void validatePasswordName(String name) throws IllegalArgumentException {
        try {
          if (name.isEmpty()) {
            throw new IllegalArgumentException("Palabra clave de la contrasena no puede ser vacia.");
          }
          if (name.contains(" ")) {
            throw new IllegalArgumentException("Palabra clave de la contrasena no puede tener espacios.");
          }
        } catch (IllegalArgumentException e) {
          logger.error("Palabra clave de contrasena no valida.", e);
          throw e; // Re-throw the exception for further handling
        }
      }    
    
      /* 
      private void validatePasswordString(String pass) throws IllegalArgumentException {
        try {
            if (pass.isEmpty()) {
                throw new IllegalArgumentException("Contrasena no puede ser vacia.");
            }
            if (pass.length() < 8) {
                throw new IllegalArgumentException("Contrasena debe tener al menos 8 caracteres.");
            }
            if (pass.contains(" ")) {
                throw new IllegalArgumentException("Contrasena no puede contener espacios vacios.");
              }
            if (!pass.matches("^[a-zA-Z0-9@#$%^&+=]+$")) {
                throw new IllegalArgumentException("Contrasena debe contener solo letras, numeros y los siguientes caracteres especiales: @#$%^&+=");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Contrasena ingresada no valida", e);
            throw e; // Re-throw the exception for further handling
        }
    }
    */

    private void processGetPassword(String name) throws SQLException {
        Password password = connection.getPassword(name);
        
        if (password != null) {
            //System.out.println("Password retrieved:");
            //System.out.println("  Name: " + password.getPasswordName());
            System.out.println("  Contrasena: " + password.getPasswordString()); // Consider masking retrieved password for security
        } else {
            System.out.println("Contrasena con palabra clave '" + name + "' no fue encontrada.");
        }
    }

    private void processRenamePassword(String name, String newName) throws SQLException {
        connection.renamePassword(name, newName);
        System.out.println("Contrasena actualizada con exito.");
    }

    private void processRemovePassword(String name) throws SQLException {
        connection.removePassword(name);
        System.out.println("Contrasena borrada con exito.");
    }



    
    private void processAddPassword(String name, String password, int user_id) throws SQLException {
        try {
            // Validate password name (throws exception for invalid names)
            validatePasswordName(name);
            // AGREGAR VALIDACION DE LA CONSTRASENA
            // Luego se revisa si ya existe una password con ese nombre

            if (connection.passwordNameExists(name, user_id)) {
                System.out.println("Error: Ya existe una contrasena con la palabra clave '" + name + "' asociada.");
                return;
            }
            // Create and add Password object (unchanged)
            Password newPassword = new Password(name, password);
            try {
                connection.addPassword(newPassword, user_id);
                System.out.println("Contrasena fue agregada con exito!");
            } catch (SQLException e) {
                // Handle SQL exception if needed
                // Loggin pendiente y etc
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid password name exception
            System.out.println(e.getMessage());  // Print the error message from the exception
        }
    }




    public void printHelp() {
        System.out.println("MyPass Manager CLI Usage:");
        System.out.println("  add <name> <password>     - Agregar una nueva contrasena");
        System.out.println("  get <name>                - Recuperar una contrasena a traves de la palabra clave");
        System.out.println("  remove <name>             - Eliminar una contrasena");
        System.out.println("  rename <name> <new_name>  - Actualizar una contrasena");
        System.out.println("  exit                      - Salir");

    }
}
