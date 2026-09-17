package com.mycompany.programming;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Login login = new Login();
        Scanner sc = new Scanner(System.in);

        System.out.println("=== REGISTRATION ===");
        System.out.print("Enter first name: ");
        String first = sc.nextLine();
        System.out.print("Enter last name: ");
        String last = sc.nextLine();
        System.out.print("Enter username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();
        System.out.print("Enter cell (+27…): ");
        String cell = sc.nextLine();

        String regMsg = login.registerUser(user, pass, cell, first, last);
        System.out.println(regMsg);

        System.out.println("\n=== LOGIN ===");
        System.out.print("Username: ");
        String loginUser = sc.nextLine();
        System.out.print("Password: ");
        String loginPass = sc.nextLine();

        System.out.println(login.returnLoginStatus(loginUser, loginPass));
        sc.close();
    }
}
