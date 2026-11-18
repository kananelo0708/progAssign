package com.mycompany.ChatApp;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Part 3 unit tests - arrays, search, delete and reports.
 * The arrays are populated by the program itself (sentMessage / storeMessage),
 * using the test data from the brief.
 */
public class StoredMessagesTest {

    // Test data from the brief
    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    private static final String RECIPIENT_1 = "+27834557896";
    private static final String RECIPIENT_2 = "+27838884567";
    private static final String RECIPIENT_3 = "+27834484567";
    private static final String RECIPIENT_4 = "Developer";
    private static final String RECIPIENT_5 = "+27838884567";

    private static final String ID_4 = "0838884567"; // Test Data Message 4 (developer entry)

    private Message m1, m2, m3, m4, m5;
    private Path tempJson;

    @Before
    public void setUp() throws IOException {
        Message.resetAll();
        tempJson = Files.createTempFile("quickchat-part3", ".json");
        Message.setStoragePath(tempJson);
        Message.setDefaultSender("kyl_1");

        m1 = new Message("2345678901", 0, RECIPIENT_1, TEXT_1);
        m2 = new Message("3456789012", 1, RECIPIENT_2, TEXT_2);
        m3 = new Message("4567890123", 2, RECIPIENT_3, TEXT_3);
        m4 = new Message(ID_4, 3, RECIPIENT_4, TEXT_4);
        m5 = new Message("5678901234", 4, RECIPIENT_5, TEXT_5);

        // Populate the arrays using the flags from the brief
        m1.sentMessage(1); // Sent
        m2.sentMessage(3); // Stored
        m3.sentMessage(2); // Disregard
        m4.sentMessage(1); // Sent
        m5.sentMessage(3); // Stored
    }

    // ---------- Arrays ----------

   
    public void testSentMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList("Did you get the cake?", "It is dinner time!"),
                     Message.getSentMessageTexts());
    }

   
    public void testDisregardedMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList("Yohoooo, I am at your gate."),
                     Message.getDisregardedMessageTexts());
    }

   
    public void testStoredMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList(TEXT_2, TEXT_5), Message.getStoredMessageTexts());
    }

   
    public void testMessageHashArrayCorrectlyPopulated() {
        List<String> hashes = Message.getMessageHashes();
        assertEquals(5, hashes.size());
        assertTrue(hashes.contains(m1.getMessageHash()));
        assertTrue(hashes.contains(m5.getMessageHash()));
    }

   
    public void testMessageIDArrayCorrectlyPopulated() {
        List<String> ids = Message.getMessageIDs();
        assertEquals(5, ids.size());
        assertTrue(ids.contains(ID_4));
    }

    @Test
    public void testTotalSentOnlyCountsSentMessages() {
        assertEquals(2, Message.returnTotalMessages());
    }

    // ---------- Longest message ----------

   
    public void testLongestMessageOfTestData1To4() {
        List<Message> messages1To4 = new ArrayList<>(Arrays.asList(m1, m2, m3, m4));
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                     Message.longestMessage(messages1To4));
    }

   
    public void testLongestStoredMessage() {
        assertEquals(TEXT_2, Message.longestStoredMessage());
    }

   
    public void testLongestStoredMessageWhenNoneStored() {
        Message.resetAll();
        assertEquals("", Message.longestStoredMessage());
    }

    // ---------- Search by message ID ----------

   
    public void testSearchForMessageID() {
        Message found = Message.findByMessageID("0838884567");
        assertNotNull(found);
        assertEquals("It is dinner time!", found.getMessageText());
    }

  
    public void testSearchForMessageIDDisplaysRecipientAndMessage() {
        String result = Message.searchByMessageID(ID_4);
        assertTrue(result.contains("Recipient: " + RECIPIENT_4));
        assertTrue(result.contains("Message: " + TEXT_4));
    }

   
    public void testSearchForMessageIDNotFound() {
        assertNull(Message.findByMessageID("0000000000"));
        assertEquals("No message found with the message ID: 0000000000",
                     Message.searchByMessageID("0000000000"));
    }

    // ---------- Search by recipient ----------

  
    public void testSearchMessagesSentOrStoredForRecipient() {
        assertEquals(Arrays.asList(TEXT_2, TEXT_5),
                     Message.searchByRecipient("+27838884567"));
    }

  
    public void testSearchStoredMessagesForRecipient() {
        assertEquals(Arrays.asList(TEXT_2, TEXT_5),
                     Message.searchStoredByRecipient("+27838884567"));
    }

  
    public void testSearchForRecipientWithNoMessages() {
        assertTrue(Message.searchByRecipient("+27000000000").isEmpty());
    }

    // ---------- Delete using the message hash ----------

   
    public void testDeleteMessageUsingMessageHash() throws IOException {
        String result = Message.deleteByHash(m2.getMessageHash());

        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" "
                   + "successfully deleted.", result);
        assertEquals(Arrays.asList(TEXT_5), Message.getStoredMessageTexts());
        assertFalse(Message.getMessageHashes().contains(m2.getMessageHash()));

        // The JSON file must be updated too
        String json = new String(Files.readAllBytes(tempJson), StandardCharsets.UTF_8);
        assertFalse(json.contains("Where are you?"));
        assertTrue(json.contains("Ok, I am leaving without you."));
    }

 
    public void testDeleteMessageWithUnknownHash() {
        assertEquals("No stored message found with the message hash: 99:9:NOPE",
                     Message.deleteByHash("99:9:NOPE"));
        assertEquals(2, Message.getStoredMessages().size());
    }

    // ---------- Sender and recipient display ----------

    @Test
    public void testDisplaySenderAndRecipientOfStoredMessages() {
        String result = Message.displayStoredSendersAndRecipients();
        assertTrue(result.contains("Sender: kyl_1"));
        assertTrue(result.contains("Recipient: " + RECIPIENT_2));
        assertFalse(result.contains(RECIPIENT_1)); // message 1 was sent, not stored
    }

    // ---------- Reports ----------

   
    public void testDisplayReportOfSentMessages() {
        String report = Message.displayReport();
        assertTrue(report.contains("Message Hash: " + m1.getMessageHash()));
        assertTrue(report.contains("Recipient: " + RECIPIENT_1));
        assertTrue(report.contains("Message: " + TEXT_1));
        assertTrue(report.contains("Message Hash: " + m4.getMessageHash()));
        assertTrue(report.contains("Message: " + TEXT_4));
        assertFalse(report.contains(TEXT_2)); // stored, not sent
    }

  
    public void testStoredMessagesReportHasFullDetails() {
        String report = Message.storedMessagesReport();
        assertTrue(report.contains("Message ID: 3456789012"));
        assertTrue(report.contains("Message Hash: " + m2.getMessageHash()));
        assertTrue(report.contains("Sender: kyl_1"));
        assertTrue(report.contains("Recipient: " + RECIPIENT_2));
        assertTrue(report.contains("Message: " + TEXT_2));
        assertTrue(report.contains("Message: " + TEXT_5));
    }

    // ---------- Reading the JSON file back into the array ----------

  
    public void testLoadStoredMessagesFromJson() {
        Message.resetAll();                    // simulate starting the program again
        int loaded = Message.loadStoredMessages();

        assertEquals(2, loaded);
        assertEquals(Arrays.asList(TEXT_2, TEXT_5), Message.getStoredMessageTexts());
        assertEquals("kyl_1", Message.getStoredMessages().get(0).getSender());
        assertEquals(m2.getMessageHash(), Message.getStoredMessages().get(0).getMessageHash());
    }

  
    public void testLoadStoredMessagesKeepsSpecialCharacters() {
        String tricky = "He said \"hi\"\nand left\\ \t(done)";
        Message special = new Message("9999999999", 7, "+27831112222", tricky);
        special.sentMessage(3);

        Message.resetAll();
        assertEquals(3, Message.loadStoredMessages());
        assertTrue(Message.getStoredMessageTexts().contains(tricky));
    }

   
    public void testLoadStoredMessagesWhenFileMissing() throws IOException {
        Files.deleteIfExists(tempJson);
        Message.resetAll();
        assertEquals(0, Message.loadStoredMessages());
        assertTrue(Message.getStoredMessages().isEmpty());
    }
}
