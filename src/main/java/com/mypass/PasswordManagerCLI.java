package com.mypass;

import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//Para encriptar y desencriptar contrasenaa
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class PasswordManagerCLI {

    private DatabaseConnection connection;
    private static final Logger logger = LogManager.getLogger(PasswordManagerCLI.class);
    //Para encriptar y desencriptar:
    private static final String SECRET_KEY = "my_super_secret_key";
    private static final String SALT = "my_salt";
    private static final String INIT_VECTOR = "encryptionIntVec";

    public PasswordManagerCLI(DatabaseConnection connection) {
        this.connection = connection;
    }

    static class EncryptionUtil {
        // Métodos de encriptación y desencriptación aquí
        public static String encrypt(String password) {
            try {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(INIT_VECTOR.getBytes());
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                KeySpec keySpec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
                SecretKey secretKey = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
                return Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    
        public static String decrypt(String encryptedPassword) {
            try {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(INIT_VECTOR.getBytes());
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                KeySpec keySpec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
                SecretKey secretKey = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
                return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedPassword)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
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
            case "generate":
                //int largo = Integer.valueOf(args[2]);????
                String caracteres = "jbfgu12=";
                int largo = 10;
                processGeneratePassword(name, user_id, caracteres, largo); 
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
            boolean containsDigit = false;
            boolean containsSpecialChar = false;
            for (char c : pass.toCharArray()) {
                if (Character.isDigit(c)) {
                    containsDigit = true;
                } else if ("@#$%^&+=".indexOf(c) != -1) {
                    containsSpecialChar = true;
                }
            }
            if (!containsDigit) {
                throw new IllegalArgumentException("Contrasena debe contener al menos un numero.");
            }
            if (!containsSpecialChar) {
                throw new IllegalArgumentException("Contrasena debe contener al menos un caracter especial: @#$%^&+=");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Contrasena ingresada no valida", e);
            throw e; // Re-throw the exception for further handling
        }
    }

    private void processGetPassword(String name) throws SQLException {
        Password password = connection.getPassword(name);
        
        if (password != null) {
            //System.out.println("Password retrieved:");
            //System.out.println("  Name: " + password.getPasswordName());
            String decryptedPassword = EncryptionUtil.decrypt(password.getPasswordString());
            System.out.println("  Contrasena: " + decryptedPassword); // Consider masking retrieved password for security
        } else {
            System.out.println("Contrasena con palabra clave '" + name + "' no fue encontrada.");
        }
    }

    public static boolean contieneNumero(String str) {
        // Iterar sobre cada carácter de la cadena
        for (char c : str.toCharArray()) {
            // Verificar si el carácter es un dígito
            if (Character.isDigit(c)) {
                return true; // Si se encuentra un dígito, devuelve true
            }
        }
        return false; // Si no se encuentra ningún dígito, devuelve false
    }

    public static boolean contieneCaracterEspecial(String str) {
        // Caracteres especiales a buscar
        String caracteresEspeciales = "@#$%^&+=";

        // Iterar sobre cada carácter de la cadena
        for (char c : str.toCharArray()) {
            // Verificar si el carácter es uno de los caracteres especiales
            if (caracteresEspeciales.indexOf(c) != -1) {
                return true; // Si se encuentra un caracter especial, devuelve true
            }
        }
        return false; // Si no se encuentra ningún caracter especial, devuelve false
    }

    private void processGeneratePassword(String name, int user_id, String caracteres, int largo) throws SQLException {
        try { 
            if (connection.passwordNameExists(name, user_id)) {
                System.out.println("Error: Ya existe una contrasena con la palabra clave '" + name + "' asociada.");
                return;
            }
            validatePasswordName(name);

            try {
                if (!contieneNumero(caracteres)){
                    throw new IllegalArgumentException("Los caracteres deben incluir al menos un numero.");
                }
                if (!contieneCaracterEspecial(caracteres)) {
                    throw new IllegalArgumentException("Los caracteres deben incluir al menos un simbolo: @#$%^&+=.");
                }
            }catch (IllegalArgumentException e) {
                // Handle invalid password name exception
                System.out.println(e.getMessage());  // Print the error message from the exception
            }

            try {
                if (largo<8){
                    throw new IllegalArgumentException("La contraseña debe tener un largo de minimo 8");
                }
            }catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());  // Print the error message from the exception
            }
            
            String password = generateRandomString(caracteres, largo);

            System.out.println("Contraseña generada: "+ password);

            String encryptedPassword = EncryptionUtil.encrypt(password);
            Password newPassword = new Password(name, encryptedPassword);
            try {
                connection.addPassword(newPassword, user_id);
                System.out.println("Contrasena fue agregada con exito!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());  // Print the error message from the exception
        }
    }

    public static String generateRandomString(String characters, int length) {
        if (length <= 7) {
            throw new IllegalArgumentException("La longitud debe ser mayor que 7");
        }

        // Convierte la cadena de caracteres en una lista de caracteres
        List<Character> charList = new ArrayList<>();
        for (char c : characters.toCharArray()) {
            charList.add(c);
        }

        // Inicializa el generador de números aleatorios
        Random random = new Random();

        // Mezcla los caracteres aleatoriamente
        Collections.shuffle(charList);

        // Construye la cadena aleatoria
        StringBuilder stringBuilder = new StringBuilder(length);
        int index = 0;
        while (stringBuilder.length() < length) {
            stringBuilder.append(charList.get(index));
            index = (index + 1) % charList.size(); // Recorre la lista de caracteres
        }

        // Devuelve la cadena aleatoria generada
        return stringBuilder.toString();
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
            // Luego se revisa si ya existe una password con ese nombre
            if (connection.passwordNameExists(name, user_id)) {
                System.out.println("Error: Ya existe una contrasena con la palabra clave '" + name + "' asociada.");
                return;
            }

            // Validate password name (throws exception for invalid names)
            validatePasswordName(name);
            validatePasswordString(password);

            // Encrypt the password before saving it
            String encryptedPassword = EncryptionUtil.encrypt(password);

            // Create and add Password object (unchanged)
            Password newPassword = new Password(name, encryptedPassword);
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
        System.out.println("  generate <name> <caracteres deseados> <largo> - Generar automáticamente una nueva contrasena");
        System.out.println("  get <name>                - Recuperar una contrasena a traves de la palabra clave");
        System.out.println("  remove <name>             - Eliminar una contrasena");
        System.out.println("  rename <name> <new_name>  - Actualizar una contrasena");
        System.out.println("  exit                      - Salir");

    }
}
