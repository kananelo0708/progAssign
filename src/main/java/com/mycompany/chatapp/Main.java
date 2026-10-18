package com.mycompany.ChatApp;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Login login = new Login();
        Scanner sc = new Scanner(System.in);

        // ---------- Registration (repeat until successful) ----------
        System.out.println("=== REGISTRATION ===");
        boolean registered = false;
        while (!registered) {
            System.out.print("Enter first name: ");
            String first = sc.nextLine();
            System.out.print("Enter last name: ");
            String last = sc.nextLine();
            System.out.print("Enter username: ");
            String user = sc.nextLine();
            System.out.print("Enter password: ");
            String pass = sc.nextLine();
            System.out.print("Enter cell (+27...): ");
            String cell = sc.nextLine();

            System.out.println(login.registerUser(user, pass, cell, first, last));

            // Credentials are only stored when registration succeeds
            registered = login.loginUser(user, pass);
            if (!registered) {
                System.out.println("Registration unsuccessful. Please try again.\n");
            }
        }

        // ---------- Login (repeat until successful) ----------
        System.out.println("\n=== LOGIN ===");
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Username: ");
            String loginUser = sc.nextLine();
            System.out.print("Password: ");
            String loginPass = sc.nextLine();

            System.out.println(login.returnLoginStatus(loginUser, loginPass));
            loggedIn = login.loginUser(loginUser, loginPass);
        }

        // Messaging is only available after a successful login
        runQuickChat(sc);
        sc.close();
    }

    // ---------- QuickChat menu ----------

    private static void runQuickChat(Scanner sc) {
        System.out.println("\nWelcome to QuickChat.");

        int maxMessages = readInt(sc,
                "How many messages do you wish to enter? ", 1, Integer.MAX_VALUE);
        int processed = 0;

        boolean running = true;
        while (running) {
            System.out.println("\nPlease choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");

            int choice = readInt(sc, "Option: ", 1, 3);

            switch (choice) {
                case 1:
                    processed = sendMessages(sc, maxMessages, processed);
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    running = false;
                    System.out.println("Goodbye.");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Lets the user enter messages until the number they set at the start
     * has been used. Returns how many messages have been processed so far.
     */
    private static int sendMessages(Scanner sc, int maxMessages, int processed) {
        if (processed >= maxMessages) {
            System.out.println("You have already entered the " + maxMessages
                    + " message(s) you set at the start.");
            return processed;
        }

        while (processed < maxMessages) {
            System.out.println("\n--- Message " + (processed + 1)
                    + " of " + maxMessages + " ---");

            String recipient = promptRecipient(sc);
            String text = promptMessageText(sc);

            Message message = new Message(recipient, text, processed);
            System.out.println("Message ID generated: " + message.getMessageID());

            System.out.println("\nWhat would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int action = readInt(sc, "Option: ", 1, 3);

            System.out.println(message.sentMessage(action));

            if (action == 1) {
                System.out.println("\n" + message.getDetails());
            } else if (action == 2) {
                String input = sc.nextLine().trim();
                if (input.equals("0")) {
                    System.out.println("Message deleted.");
                } else {
                    System.out.println("Message disregarded.");
                }
            }

            processed++;
        }

        System.out.println("\nTotal number of messages sent: "
                + Message.returnTotalMessages());
        return processed;
    }

    // ---------- Input helpers ----------

    /** Keeps asking until a valid recipient cell number is entered. */
    private static String promptRecipient(Scanner sc) {
        while (true) {
            System.out.print("Enter recipient cell number (with international code): ");
            String recipient = sc.nextLine().trim();
            if (Message.isValidRecipient(recipient)) {
                System.out.println(Message.CELL_SUCCESS);
                return recipient;
            }
            System.out.println(Message.CELL_FAILURE);
        }
    }

    /** Keeps asking until a message of 1-250 characters is entered. */
    private static String promptMessageText(Scanner sc) {
        while (true) {
            System.out.print("Enter your message (max 250 characters): ");
            String text = sc.nextLine();
            if (text.trim().isEmpty()) {
                System.out.println("The message cannot be empty.");
            } else if (text.length() > Message.MAX_MESSAGE_LENGTH) {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(Message.checkMessageLength(text));
            } else {
                System.out.println(Message.checkMessageLength(text));
                return text;
            }
        }
    }

    /** Reads a whole number between min and max (inclusive). */
    private static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            if (max == Integer.MAX_VALUE) {
                System.out.println("Please enter a whole number of " + min + " or more.");
            } else {
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            }
        }
    }
}
