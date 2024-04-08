package com.mypass;

import java.util.Random;
import java.util.Comparator;
import java.util.Objects;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

public class Password {

    private static final char[] ALPHABET
            = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
                'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
                'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a',
                'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'ñ', 'o', 'p', 'q', 'r', 's',
                't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1',
                '2', '3', '4', '5', '6', '7', '8', '9'};

    private String passName;
    private String pass;

    static public class PasswordComparer implements Comparator<Password> {

        @Override
        public int compare(Password password1, Password password2) throws NullPointerException, ClassCastException {
            return password1.getPasswordName().compareTo(password2.getPasswordName());
        }
    }

    /*Generates a new random password:
     *   int size: User requested password length
     *   String name: User requested password name
     */
    public Password generate(int size, String name) throws IllegalArgumentException {
        name = name.trim();
        //validatePasswordName(name);  // Validate password name

        char[] temp = new char[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++)
            temp[i] = ALPHABET[rand.nextInt(ALPHABET.length)];
        String password = new String(temp);
        this.passName = name;
        this.pass = password;
        return this;
    }

 
    public Password(String name, String pass) throws IllegalArgumentException{
        this.passName = name;
        this.pass = pass;
    }

    //Copy constructor
    public Password(Password p) {
        this.passName = p.passName;
        this.pass = p.pass;
    }

    public void setPasswordString(String pass) throws IllegalArgumentException {
        this.pass = pass;
    }

    public String getPasswordString() {
        return this.pass;
    }

    public String getPasswordName() {
        return this.passName;
    }

    public void setPasswordName(String name) throws IllegalArgumentException {
        this.passName = name;
    }


    @Override
    public String toString() {
        return "Name: " + this.getPasswordName() + ", Password: " + this.getPasswordString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        Password p = (Password) obj;
        if (!(p instanceof Password))
            throw new ClassCastException();

        return p.pass.equals(this.pass) && p.passName.equals(this.passName); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.passName);
        hash = 53 * hash + Objects.hashCode(this.pass);
        return hash;
    }

}