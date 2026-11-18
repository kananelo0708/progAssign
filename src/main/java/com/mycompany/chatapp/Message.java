package com.mycompany.ChatApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Message class for QuickChat.
 * Handles message creation, validation, hashing, sending, storing (JSON),
 * and keeps the arrays required for Part 3:
 * sent messages, disregarded messages, stored messages, message hashes
 * and message IDs. All arrays are populated while the program runs
 * (nothing is hard-coded).
 */
public final class Message {

    // ---------- Constants ----------
    public static final int MAX_MESSAGE_LENGTH = 250;

    public static final String CELL_SUCCESS =
            "Cell phone number successfully captured.";
    public static final String CELL_FAILURE =
            "Cell phone number is incorrectly formatted or does not contain an "
          + "international code. Please correct the number and try again.";

    public static final String NO_SENT_MESSAGES = "No messages have been sent.";
    public static final String NO_STORED_MESSAGES = "There are no stored messages.";

    // Starts with '+', then a 1-3 digit international code, then up to 10 digits.
    private static final Pattern CELL_PATTERN =
            Pattern.compile("^\\+\\d{1,3}\\d{1,10}$");

    // ---------- Shared (static) arrays ----------
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIDs = new ArrayList<>();

    // Where the JSON file is written / read
    private static Path storagePath = Paths.get("messages.json");

    // Name of the logged-in user; used as the sender of new messages
    private static String defaultSender = "Sender";

    // ---------- Instance fields ----------
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;
    private final String sender;

    // ---------- Constructors ----------

    /** Creates a message and auto-generates a random ten-digit message ID. */
    public Message(String recipient, String messageText, int messageNumber) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    /** Creates a message with a supplied ID (useful for unit testing). */
    public Message(String messageID, int messageNumber,
                   String recipient, String messageText) {
        this(messageID, messageNumber, recipient, messageText, defaultSender);
    }

    /** Creates a message with a supplied ID and sender. */
    public Message(String messageID, int messageNumber,
                   String recipient, String messageText, String sender) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText == null ? "" : messageText;
        this.sender = sender == null ? defaultSender : sender;
        this.messageHash = createMessageHash();
    }

    // =====================================================================
    //  PART 2 METHODS
    // =====================================================================

    /** Ensures the message ID is not more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null
                && !messageID.isEmpty()
                && messageID.length() <= 10;
    }

    /**
     * Ensures the recipient cell number starts with an international code
     * and is not too long. Returns the success or failure message.
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
     * The message is also added to the matching array.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                if (!sentMessages.contains(this)) {
                    sentMessages.add(this);
                    track(this);
                }
                return "Message successfully sent.";
            case 2:
                if (!disregardedMessages.contains(this)) {
                    disregardedMessages.add(this);
                    track(this);
                }
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
            return NO_SENT_MESSAGES;
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
     * Stores this message in the stored-messages array and writes the whole
     * array to the JSON file. Returns true if the file was written.
     */
    public boolean storeMessage() {
        if (!storedMessages.contains(this)) {
            storedMessages.add(this);
            track(this);
        }
        return writeStoredMessagesToJson();
    }

    // =====================================================================
    //  PART 3 METHODS - arrays
    // =====================================================================

    /** Copy of the Sent Messages array. */
    public static List<Message> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    /** Copy of the Disregarded Messages array. */
    public static List<Message> getDisregardedMessages() {
        return new ArrayList<>(disregardedMessages);
    }

    /** Copy of the Stored Messages array. */
    public static List<Message> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    /** Copy of the Message Hash array. */
    public static List<String> getMessageHashes() {
        return new ArrayList<>(messageHashes);
    }

    /** Copy of the Message ID array. */
    public static List<String> getMessageIDs() {
        return new ArrayList<>(messageIDs);
    }

    /** The text of every sent message, in the order they were sent. */
    public static List<String> getSentMessageTexts() {
        return textsOf(sentMessages);
    }

    /** The text of every disregarded message. */
    public static List<String> getDisregardedMessageTexts() {
        return textsOf(disregardedMessages);
    }

    /** The text of every stored message. */
    public static List<String> getStoredMessageTexts() {
        return textsOf(storedMessages);
    }

    /**
     * Reads the JSON file into the Stored Messages array (replacing what is
     * currently in it). Returns how many messages were loaded; returns 0 and
     * changes nothing if the file does not exist or cannot be read.
     */
    public static int loadStoredMessages() {
        if (storagePath == null || !Files.exists(storagePath)) {
            return 0;
        }

        List<Message> loaded = new ArrayList<>();
        try {
            String json = new String(Files.readAllBytes(storagePath), StandardCharsets.UTF_8);
            List<Map<String, String>> objects = new JsonReader(json).readArrayOfObjects();

            for (Map<String, String> obj : objects) {
                String id = obj.get("messageID");
                String recipientValue = obj.get("recipient");
                String text = obj.get("message");
                if (id == null || recipientValue == null || text == null) {
                    continue;
                }

                int number = 0;
                try {
                    number = Integer.parseInt(obj.get("messageNumber"));
                } catch (NumberFormatException e) {
                    number = 0;
                }
                loaded.add(new Message(id, number, recipientValue, text, obj.get("sender")));
            }
        } catch (IOException | IllegalArgumentException e) {
            return 0;
        }

        for (Message old : storedMessages) {
            untrack(old);
        }
        storedMessages.clear();
        for (Message m : loaded) {
            storedMessages.add(m);
            track(m);
        }
        return loaded.size();
    }

    // =====================================================================
    //  PART 3 METHODS - Stored Messages menu
    // =====================================================================

    /** (a) The sender and recipient of every stored message. */
    public static String displayStoredSendersAndRecipients() {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            lines.add((i + 1) + ") Sender: " + m.sender + " | Recipient: " + m.recipient);
        }
        return String.join(System.lineSeparator(), lines);
    }

    /** (b) The text of the longest stored message ("" if there are none). */
    public static String longestStoredMessage() {
        return longestMessage(storedMessages);
    }

    /** The text of the longest message in the given list ("" if empty). */
    public static String longestMessage(List<Message> messages) {
        String longest = "";
        if (messages == null) {
            return longest;
        }
        for (Message m : messages) {
            if (m.messageText.length() > longest.length()) {
                longest = m.messageText;
            }
        }
        return longest;
    }

    /** Finds a message by ID in the sent, stored and disregarded arrays. */
    public static Message findByMessageID(String id) {
        if (id == null) {
            return null;
        }
        String wanted = id.trim();
        Message found = findIn(sentMessages, wanted);
        if (found == null) {
            found = findIn(storedMessages, wanted);
        }
        if (found == null) {
            found = findIn(disregardedMessages, wanted);
        }
        return found;
    }

    /** (c) Search for a message ID and display the recipient and message. */
    public static String searchByMessageID(String id) {
        Message m = findByMessageID(id);
        if (m == null) {
            return "No message found with the message ID: " + (id == null ? "" : id.trim());
        }
        return "Recipient: " + m.recipient + System.lineSeparator()
             + "Message: " + m.messageText;
    }

    /** Messages sent or stored for a recipient (sent array first, then stored). */
    public static List<String> searchByRecipient(String recipientNumber) {
        List<String> results = new ArrayList<>();
        addMatches(sentMessages, recipientNumber, results);
        addMatches(storedMessages, recipientNumber, results);
        return results;
    }

    /** (d) All the messages stored for a particular recipient. */
    public static List<String> searchStoredByRecipient(String recipientNumber) {
        List<String> results = new ArrayList<>();
        addMatches(storedMessages, recipientNumber, results);
        return results;
    }

    /** (e) Deletes a stored message using its message hash. */
    public static String deleteByHash(String hash) {
        String wanted = hash == null ? "" : hash.trim();

        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.messageHash.equalsIgnoreCase(wanted)) {
                storedMessages.remove(i);
                untrack(m);
                boolean saved = writeStoredMessagesToJson();
                String result = "Message: \"" + m.messageText + "\" successfully deleted.";
                if (!saved) {
                    result += " (Warning: the JSON file could not be updated.)";
                }
                return result;
            }
        }
        return "No stored message found with the message hash: " + wanted;
    }

    /** Report of all the sent messages: hash, recipient and message. */
    public static String displayReport() {
        if (sentMessages.isEmpty()) {
            return NO_SENT_MESSAGES;
        }
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("=== SENT MESSAGES REPORT ===").append(nl);
        for (Message m : sentMessages) {
            sb.append(nl)
              .append("Message Hash: ").append(m.messageHash).append(nl)
              .append("Recipient: ").append(m.recipient).append(nl)
              .append("Message: ").append(m.messageText).append(nl);
        }
        return sb.toString();
    }

    /** (f) Report listing the full details of all the stored messages. */
    public static String storedMessagesReport() {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("=== STORED MESSAGES REPORT ===").append(nl);
        for (Message m : storedMessages) {
            sb.append(nl)
              .append("Message ID: ").append(m.messageID).append(nl)
              .append("Message Hash: ").append(m.messageHash).append(nl)
              .append("Sender: ").append(m.sender).append(nl)
              .append("Recipient: ").append(m.recipient).append(nl)
              .append("Message: ").append(m.messageText).append(nl);
        }
        return sb.toString();
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

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

    /** Removes everything from every array (used by unit tests). */
    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
    }

    public static void setStoragePath(Path path) {
        storagePath = path;
    }

    public static Path getStoragePath() {
        return storagePath;
    }

    /** Sets the sender (logged-in user) used for new messages. */
    public static void setDefaultSender(String name) {
        if (name != null && !name.trim().isEmpty()) {
            defaultSender = name.trim();
        }
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

    public String getSender() {
        return sender;
    }

    // ---------- Private helpers ----------

    /** Adds the message's hash and ID to the Message Hash and Message ID arrays. */
    private static void track(Message m) {
        messageHashes.add(m.messageHash);
        messageIDs.add(m.messageID);
    }

    /** Removes the message's hash and ID from the Message Hash and Message ID arrays. */
    private static void untrack(Message m) {
        messageHashes.remove(m.messageHash);
        messageIDs.remove(m.messageID);
    }

    private static List<String> textsOf(List<Message> source) {
        List<String> texts = new ArrayList<>();
        for (Message m : source) {
            texts.add(m.messageText);
        }
        return texts;
    }

    private static Message findIn(List<Message> source, String id) {
        for (Message m : source) {
            if (m.messageID != null && m.messageID.equals(id)) {
                return m;
            }
        }
        return null;
    }

    private static void addMatches(List<Message> source, String recipientNumber, List<String> out) {
        if (recipientNumber == null) {
            return;
        }
        String wanted = recipientNumber.trim();
        for (Message m : source) {
            if (m.recipient != null && m.recipient.equals(wanted)) {
                out.add(m.messageText);
            }
        }
    }

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
            sb.append("    \"sender\": \"").append(escapeJson(m.sender)).append("\",\n");
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

    // =====================================================================
    //  Small JSON reader (reads the file written by storeMessage)
    //  Supports an array of flat objects with string / number values.
    // =====================================================================

    private static final class JsonReader {
        private final String text;
        private int index = 0;

        JsonReader(String text) {
            this.text = text;
        }

        List<Map<String, String>> readArrayOfObjects() {
            List<Map<String, String>> list = new ArrayList<>();
            skipWhitespace();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                index++;
                return list;
            }
            while (true) {
                list.add(readObject());
                skipWhitespace();
                char c = next();
                if (c == ']') {
                    break;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("Expected ',' or ']' in JSON");
                }
            }
            return list;
        }

        private Map<String, String> readObject() {
            Map<String, String> map = new LinkedHashMap<>();
            skipWhitespace();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                index++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                String value = readValue();
                map.put(key, value);
                skipWhitespace();
                char c = next();
                if (c == '}') {
                    break;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("Expected ',' or '}' in JSON");
                }
            }
            return map;
        }

        private String readValue() {
            if (peek() == '"') {
                return readString();
            }
            int start = index;
            while (index < text.length()
                    && ",}]".indexOf(text.charAt(index)) < 0
                    && !Character.isWhitespace(text.charAt(index))) {
                index++;
            }
            return text.substring(start, index);
        }

        private String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    char e = next();
                    switch (e) {
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'u':
                            if (index + 4 > text.length()) {
                                throw new IllegalArgumentException("Bad unicode escape in JSON");
                            }
                            sb.append((char) Integer.parseInt(text.substring(index, index + 4), 16));
                            index += 4;
                            break;
                        default:
                            sb.append(e); // handles \" \\ and \/
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private char peek() {
            if (index >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON");
            }
            return text.charAt(index);
        }

        private char next() {
            char c = peek();
            index++;
            return c;
        }

        private void expect(char expected) {
            char actual = next();
            if (actual != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' in JSON");
            }
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }
    }
}
