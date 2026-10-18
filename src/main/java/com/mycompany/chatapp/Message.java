package com.mycompany.ChatApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Message class for QuickChat.
 * Handles message creation, validation, hashing, sending, storing (JSON)
 * and keeping track of every message sent while the program is running.
 */
public final class Message {

    // ---------- Constants ----------
    public static final int MAX_MESSAGE_LENGTH = 250;

    public static final String CELL_SUCCESS =
            "Cell phone number successfully captured.";
    public static final String CELL_FAILURE =
            "Cell phone number is incorrectly formatted or does not contain an "
          + "international code. Please correct the number and try again.";

    // Starts with '+', then a 1-3 digit international code, then up to 10 digits.
    private static final Pattern CELL_PATTERN =
            Pattern.compile("^\\+\\d{1,3}\\d{1,10}$");

    // ---------- Shared (static) state ----------
    // All messages sent while the program is running
    private static final List<Message> sentMessages = new ArrayList<>();
    // All messages stored to send later
    private static final List<Message> storedMessages = new ArrayList<>();
    // Where the JSON file is written
    private static Path storagePath = Paths.get("messages.json");

    // ---------- Instance fields ----------
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    // ---------- Constructors ----------

    /** Creates a message and auto-generates a random ten-digit message ID. */
    public Message(String recipient, String messageText, int messageNumber) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    /** Creates a message with a supplied ID (useful for unit testing). */
    public Message(String messageID, int messageNumber,
                   String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText == null ? "" : messageText;
        this.messageHash = createMessageHash();
    }

    // ---------- Required methods ----------

    /** Ensures the message ID is not more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null
                && !messageID.isEmpty()
                && messageID.length() <= 10;
    }

    /**
     * Ensures the recipient cell number is no more than ten digits long
     * (after the international code) and starts with an international code.
     * Returns the success or failure message.
     */
    public String checkRecipientCell() {
        return isValidRecipient(recipient) ? CELL_SUCCESS : CELL_FAILURE;
    }

    /**
     * Creates and returns the message hash:
     * first two digits of the ID : message number : FIRSTWORDLASTWORD (all caps).
     * Example: 00:0:HITONIGHT
     */
    public String createMessageHash() {
        String idPart = "";
        if (messageID != null) {
            idPart = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        }

        String[] words = messageText.trim().split("\\s+");
        String first = cleanWord(words[0]);
        String last = cleanWord(words[words.length - 1]);

        return (idPart + ":" + messageNumber + ":" + first + last)
                .toUpperCase(Locale.ROOT);
    }

    /**
     * Lets the user choose to send, disregard or store the message.
     * 1 = Send, 2 = Disregard, 3 = Store to send later.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                if (!sentMessages.contains(this)) {
                    sentMessages.add(this);
                }
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                if (storeMessage()) {
                    return "Message successfully stored.";
                }
                return "Message could not be stored.";
            default:
                return "Invalid option selected.";
        }
    }

    /** Returns all the messages sent while the program is running. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            if (i > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append(sentMessages.get(i).getDetails());
        }
        return sb.toString();
    }

    /** Returns the total number of messages sent. */
    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    /**
     * Stores this message in a JSON file.
     * The whole list of stored messages is written as a JSON array.
     * Returns true if the file was written successfully.
     */
    public boolean storeMessage() {
        if (!storedMessages.contains(this)) {
            storedMessages.add(this);
        }
        return writeStoredMessagesToJson();
    }

    // ---------- Helper methods ----------

    /** True if the number starts with '+', has an international code and is not too long. */
    public static boolean isValidRecipient(String cell) {
        return cell != null && CELL_PATTERN.matcher(cell).matches();
    }

    /** Returns "Message ready to send." or the "exceeds by X" error. */
    public static String checkMessageLength(String text) {
        int length = (text == null) ? 0 : text.length();
        if (length <= MAX_MESSAGE_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (length - MAX_MESSAGE_LENGTH)
                + "; please reduce the size.";
    }

    /** Generates a random ten-digit message ID (leading zeros allowed). */
    public static String generateMessageID() {
        long number = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", number);
    }

    /** Full details in the required order: ID, Hash, Recipient, Message. */
    public String getDetails() {
        String nl = System.lineSeparator();
        return "Message ID: " + messageID + nl
             + "Message Hash: " + messageHash + nl
             + "Recipient: " + recipient + nl
             + "Message: " + messageText + nl;
    }

    /** Removes everything from the sent/stored lists (used by unit tests). */
    public static void resetAll() {
        sentMessages.clear();
        storedMessages.clear();
    }

    public static void setStoragePath(Path path) {
        storagePath = path;
    }

    public static Path getStoragePath() {
        return storagePath;
    }

    // ---------- Getters ----------

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }

    // ---------- Private helpers ----------

    /** Removes punctuation so "tonight?" becomes "tonight". */
    private static String cleanWord(String word) {
        return word.replaceAll("[^\\p{L}\\p{N}]", "");
    }

    /** Writes every stored message to the JSON file (no external library needed). */
    private static boolean writeStoredMessagesToJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            sb.append("  {\n");
            sb.append("    \"messageID\": \"").append(escapeJson(m.messageID)).append("\",\n");
            sb.append("    \"messageNumber\": ").append(m.messageNumber).append(",\n");
            sb.append("    \"messageHash\": \"").append(escapeJson(m.messageHash)).append("\",\n");
            sb.append("    \"recipient\": \"").append(escapeJson(m.recipient)).append("\",\n");
            sb.append("    \"message\": \"").append(escapeJson(m.messageText)).append("\"\n");
            sb.append("  }");
            if (i < storedMessages.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");

        try {
            Files.write(storagePath, sb.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Escapes characters that are not allowed raw inside a JSON string. */
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }
}
