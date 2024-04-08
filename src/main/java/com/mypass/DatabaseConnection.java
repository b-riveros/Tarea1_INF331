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
                System.out.println("Database created successfully.");
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
                + "password VARHCAR(200) NOT NULL,"  // Store hashed and salted password
                + ")";
        statement.executeUpdate(sql);


        // Create table for passwords (modify as needed)
        sql = "CREATE TABLE IF NOT EXISTS passwords ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "namekey VARCHAR(30) NOT NULL,"
                + "password VARCHAR(200) NOT NULL,"
                + "FOREIGN KEY (user_id) REFERENCES users(id),"  // Consider using a separate table for encrypted passwords
                + ")";
        statement.executeUpdate(sql);

    }

    public static boolean validatePassword(String password1, String password2) {
        return password1.equals(password2);
        //falta agregar logica de hash todavia
      }

    public boolean loginUser(String username, String password) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT id, password FROM users WHERE username = ?");
        statement.setString(1, username);
        ResultSet results = statement.executeQuery();

        if (results.next()) {
            String storedPassword = results.getString("password");
            if (validatePassword(password, storedPassword)) {
                currentUserId = results.getInt("id");
                return true;
            } else {
                // Log failed login attempt
                logger.info("Failed login attempt for user: " + username);
            }
        } else {
            // Log username not found
            logger.info("Username not found: " + username);
            }
        return false; // Incorrect credentials
        }

    public void addPassword(Password password) throws SQLException {
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


}


