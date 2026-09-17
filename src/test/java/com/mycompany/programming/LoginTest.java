package com.mycompany.programming;

import com.mycompany.programming.Login;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoginTest {

    Login login = new Login();

    // ---------- Username ----------
    
    public void testUsernameCorrectlyFormatted() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    
    public void testUsernameIncorrectlyFormatted() {
        assertFalse(login.checkUserName("kyle!!!!!!!"));
    }

    // ---------- Password ----------
    
    public void testPasswordMeetsComplexity() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    
    public void testPasswordDoesNotMeetComplexity() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    // ---------- Cell phone ----------
    
    public void testCellPhoneCorrectlyFormatted() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    
    public void testCellPhoneIncorrectlyFormatted() {
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    // ---------- Registration messages (assertEquals) ----------
    
    public void testRegisterUser_UsernameCorrect() {
        String result = login.registerUser("kyl_1", "Ch&&sec@ke99!",
                                           "+27838968976", "Kyle", "Smith");
        assertTrue(result.contains("Username successfully captured"));
    }

   
    public void testRegisterUser_UsernameIncorrect() {
        String result = login.registerUser("kyle!!!!!!!", "Ch&&sec@ke99!",
                                           "+27838968976", "Kyle", "Smith");
        assertEquals("Username is not correctly formatted; please ensure that your "
                   + "username contains an underscore and is no more than five "
                   + "characters in length.", result);
    }

    
    public void testRegisterUser_PasswordIncorrect() {
        String result = login.registerUser("kyl_1", "password",
                                           "+27838968976", "Kyle", "Smith");
        assertEquals("Password is not correctly formatted; please ensure that the "
                   + "password contains at least eight characters, a capital letter, "
                   + "a number, and a special character.", result);
    }

    
    public void testRegisterUser_CellIncorrect() {
        String result = login.registerUser("kyl_1", "Ch&&sec@ke99!",
                                           "08966553", "Kyle", "Smith");
        assertEquals("Cell number is incorrectly formatted or does not contain an "
                   + "international code; please correct the number and try again.", result);
    }

    // ---------- Login ----------
    
    public void testLoginSuccessful() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    
    public void testLoginFailed() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        assertFalse(login.loginUser("wrong", "wrong"));
    }

    
    public void testReturnLoginStatus_Success() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        String msg = login.returnLoginStatus("kyl_1", "Ch&&sec@ke99!");
        assertEquals("Welcome Kyle, Smith it is great to see you again.", msg);
    }

    
    public void testReturnLoginStatus_Failed() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Smith");
        String msg = login.returnLoginStatus("wrong", "wrong");
        assertEquals("Username or password incorrect, please try again.", msg);
    
    }
}