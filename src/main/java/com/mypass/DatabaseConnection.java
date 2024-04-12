package com.mypass;

import java.io.File;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;

public class DatabaseConnection implements Closeable {

    private static final String BD_NAME = "mypass"; // Replace with your actual filename
    private static final Logger logger = LogManager.getLogger(Password.class);

    Connection connection = null;
    private int currentUserId = -1; // Store the currently logged-in user's ID

    
    public DatabaseConnection() throws SQLException, ClassNotFoundException {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            // Check if the database already exists
            boolean databaseExists = new File(BD_NAME + ".db").exists();
            // Get the connection to the database
            connection = DriverManager.getConnection("jdbc:sqlite:" + BD_NAME + ".db");
            if (!databaseExists) {
                // If it doesn't exist, create it and create tables
                createTables();
                //System.out.println("Database created successfully.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            // Log the error using a logging library
            System.err.println("Error occurred while connecting to database:");
            e.printStackTrace();
            throw e;
        }
    }

    // Close the database connection
    @Override
    public void close() throws IOException {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            throw new IOException("Error closing database connection", e);
        }
    }

    public void createTables() throws SQLException {
        Statement statement = this.connection.createStatement();

        // Create table for users (modify as needed)
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username VARCHAR(30) NOT NULL UNIQUE,"  // Ensure usernames are unique
                + "password VARCHAR(200) NOT NULL)";  // Store hashed and salted password
        statement.executeUpdate(sql);


        // Create table for passwords (modify as needed)
        sql = "CREATE TABLE IF NOT EXISTS passwords ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "namekey VARCHAR(30) NOT NULL,"
                + "password VARCHAR(200) NOT NULL,"
                + "FOREIGN KEY (user_id) REFERENCES users(id))";  // Consider using a separate table for encrypted passwords
        statement.executeUpdate(sql);

    }

    public boolean isUsersTableEmpty() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");
            resultSet.next();
            int count = resultSet.getInt(1);
            return count == 0;
        }
    }

    /*public static boolean validatePassword(String password1, String password2) {
        return password1.equals(password2);
        //falta agregar logica de hash todavia
      }*/

    public int loginUser(String username, String password) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT id, password FROM users WHERE username = ?");
        statement.setString(1, username);
        ResultSet results = statement.executeQuery();

        if (results.next()) {
            String storedPassword = results.getString("password");
            if (validatePassword(password, storedPassword)) {
                currentUserId = results.getInt("id");
                // Log successful login attempt
                logger.info("Successful login: User '" + username + "' logged in");
                return currentUserId;
            } else {
                // Log failed login attempt
                logger.info("Failed login attempt for user: " + username);
            }
        } else {
            // Log username not found
            logger.info("Username not found: " + username);
            }
        return -1; // Incorrect credentials
        }

    //Logica HASH MD5

    public String setEncryptedPasswordString(String pass) throws IllegalArgumentException {
        pass = MD5Util.hash(pass);
        return pass;
    }
    public boolean validatePassword(String passwordToVerify, String savedpassword) {
        return savedpassword.equals(MD5Util.hash(passwordToVerify));
    }

    static class MD5Util {
        public static String hash(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(input.getBytes());
                byte[] bytes = md.digest();
                StringBuilder sb = new StringBuilder();
                for (byte aByte : bytes) {
                    sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("MD5 algorithm not available", e);
            }
        }
    }

    public void addPassword(Password password, int user_id) throws SQLException {
        // Ensure a user is logged in
        
        if (currentUserId == -1) {
            throw new SQLException("No user is logged in");
        }

        PreparedStatement insert = null;
        String query = "INSERT INTO passwords (user_id, namekey, password) VALUES (?, ?, ?)";
        try {
            insert = this.connection.prepareStatement(query);
            insert.setInt(1, currentUserId);
            insert.setString(2, password.getPasswordName());
            insert.setString(3, password.getPasswordString());
            insert.executeUpdate();
        }
        catch (SQLException e) {
            throw e;
            // Handle the SQLException (log, inform user, etc.)
            // You can re-throw the exception for further handling
        }
        finally {
            if (insert != null)
                insert.close();
        }

    }


    public Password getPassword(String nameOfPass) throws SQLException {
        // Ensure a user is logged in
        if (currentUserId == -1) {
            throw new SQLException("No user is logged in");
        }
        ResultSet results;
        PreparedStatement getPassword;
        String query = "SELECT * from passwords where namekey = ?";
        getPassword = this.connection.prepareStatement(query);
        getPassword.setString(1, nameOfPass);
        results = getPassword.executeQuery();
        String name = results.getString("namekey");
        String password = results.getString("password");
        if (getPassword != null)
            getPassword.closeOnCompletion();
        return new Password(name, password);
    }


    public void removePassword(String name) throws SQLException {
       // Ensure a user is logged in
        if (currentUserId == -1) {
            throw new SQLException("No user is logged in");
        }
        PreparedStatement removePassword;
        String query = "DELETE FROM passwords where namekey= ? and user_id = ?";
        removePassword = this.connection.prepareStatement(query);
        removePassword.setString(1, name);
        removePassword.setInt(2, currentUserId);
        removePassword.executeUpdate();
        if (removePassword != null)
            removePassword.close();
    }

    public void renamePassword(String oldName, String newName) throws SQLException {
        // Ensure a user is logged in
        if (currentUserId == -1) {
            throw new SQLException("No user is logged in");
        }
        PreparedStatement renamePassword;
        String query = "UPDATE passwords set namekey = ? where namekey = ? and user_id = ?";
        renamePassword = this.connection.prepareStatement(query);
        renamePassword.setString(1, newName);
        renamePassword.setString(2, oldName);
        renamePassword.setInt(3, currentUserId);
        renamePassword.executeUpdate();
        if (renamePassword != null)
            renamePassword.close();
    }

    public void updatePassword(Password newP) throws SQLException {
        // Ensure a user is logged in
        if (currentUserId == -1) {
            throw new SQLException("No user is logged in");
        }
        PreparedStatement update;
        String query = "UPDATE passwords set password = ? where namekey = ?";
        update = this.connection.prepareStatement(query);
        update.setString(1, newP.getPasswordString());
        update.setString(2, newP.getPasswordString());
        update.executeUpdate();
    }

    public boolean passwordNameExists(String name, int user_id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM passwords WHERE namekey = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, user_id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false; // Return false if no result or error occurred
    }    

    public void createUserAccount(String username, String password){
        PreparedStatement insert = null;
        ResultSet resultSet = null;
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try {
            // Check if the username already exists
            insert = this.connection.prepareStatement(query);
            insert.setString(1, username);
            resultSet = insert.executeQuery();
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                System.err.println("Username already exists. Please choose a different username.");
                return; // Exit the method if the username already exists
            }

        query = "INSERT INTO users (username, password) VALUES (?, ?)";
        insert = this.connection.prepareStatement(query);
        insert.setString(1, username);
        insert.setString(2, setEncryptedPasswordString(password)); //aplicamos HASH antes de guardar contraseña
        insert.executeUpdate();
        }
        catch (SQLException e) {
            System.err.println("Error occurred while creating user account: " + e.getMessage());
            // Handle the SQLException (log, inform user, etc.)
            // You can re-throw the exception for further handling
        }
        finally {
            try {
                if (insert != null)
                    insert.close();
            } catch (SQLException e) {
                // Handle any errors that occur while closing the statement
                // For example, you can print the error message to the console
                System.err.println("Error occurred while closing PreparedStatement: " + e.getMessage());
            }
    }

    }
}


