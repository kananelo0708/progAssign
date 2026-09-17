package com.mycompany.programming;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Login class that handles registration and authentication.
 * Cell-phone validation uses a regular expression based on
 * South African international format (+27 followed by 9 digits).
 * Reference for regex approach: Oracle Java Pattern documentation
 * and common SA mobile number patterns.
 */
public class Login {

    // Stored credentials after successful registration
    private String storedUsername;
    private String storedPassword;
    private String firstName;
    private String lastName;

    // ---------- Validation methods ----------

    /**
     * Username must contain an underscore and be ≤ 5 characters long.
     */
    public boolean checkUserName(String username) {
        return username != null
                && username.contains("_")
                && username.length() <= 5;
    }

    /**
     * Password complexity rules:
     * - at least 8 characters
     * - at least one capital letter
     * - at least one digit
     * - at least one special character
     */
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        return hasUpper && hasDigit && hasSpecial;
    }

    /**
     * South African cell number:
     * Must start with the international code +27 and be followed by
     * exactly 9 digits (total length after + is 11 characters).
     * Example of valid number: +27838968976
     */
    public boolean checkCellPhoneNumber(String cell) {
        if (cell == null) return false;
        // Regex: starts with +27, followed by exactly 9 digits
        Pattern pattern = Pattern.compile("^\\+27\\d{9}$");
        Matcher matcher = pattern.matcher(cell);
        return matcher.matches();
    }

    // ---------- Registration ----------

    /**
     * Registers a user. Returns the appropriate message according
     * to the specification tables.
     */
    public String registerUser(String username, String password, String cell,
                               String firstName, String lastName) {

        // Username check
        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your "
                 + "username contains an underscore and is no more than five "
                 + "characters in length.";
        }

        // Password check
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the "
                 + "password contains at least eight characters, a capital letter, "
                 + "a number, and a special character.";
        }

        // Cell-phone check
        if (!checkCellPhoneNumber(cell)) {
            return "Cell number is incorrectly formatted or does not contain an "
                 + "international code; please correct the number and try again.";
        }

        // All good – store the details
        this.storedUsername = username;
        this.storedPassword = password;
        this.firstName = firstName;
        this.lastName = lastName;

        return "Username successfully captured.\n"
             + "Password successfully captured.\n"
             + "Cell phone number successfully added.";
    }

    // ---------- Login ----------

    /**
     * Verifies that the supplied username and password match the
     * stored credentials.
     */
    public boolean loginUser(String username, String password) {
        return storedUsername != null
                && storedUsername.equals(username)
                && storedPassword != null
                && storedPassword.equals(password);
    }

    /**
     * Returns the welcome / failure message required by the brief.
     */
    public String returnLoginStatus(String username, String password) {
        if (loginUser(username, password)) {
            return "Welcome " + firstName + ", " + lastName
                 + " it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }
}

