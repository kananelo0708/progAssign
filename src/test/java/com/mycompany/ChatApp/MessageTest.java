package com.mycompany.ChatApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MessageTest {

    // Test data from the brief
    private static final String ID = "0012345678"; // fixed ID so hashes are predictable
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String MESSAGE_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String MESSAGE_2 = "Hi Keegan, did you receive the payment?";

    private Message message1;
    private Message message2;
    private Path tempJson;

    @Before
    public void setUp() throws IOException {
        Message.resetAll();
        tempJson = Files.createTempFile("quickchat-test", ".json");
        Message.setStoragePath(tempJson);

        message1 = new Message(ID, 0, RECIPIENT_1, MESSAGE_1);
        message2 = new Message(ID, 1, RECIPIENT_2, MESSAGE_2);
    }

    // ---------- Message length ----------

    @Test
    public void testMessageLengthSuccess() {
        assertEquals("Message ready to send.", Message.checkMessageLength(MESSAGE_1));
    }

    @Test
    public void testMessageLengthExactly250() {
        String text = "a".repeat(250);
        assertEquals("Message ready to send.", Message.checkMessageLength(text));
    }

    @Test
    public void testMessageLengthFailure() {
        String text = "a".repeat(260);
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                     Message.checkMessageLength(text));
    }

    // ---------- Recipient number ----------

    @Test
    public void testRecipientCellSuccess() {
        assertEquals("Cell phone number successfully captured.",
                     message1.checkRecipientCell());
    }

    @Test
    public void testRecipientCellFailure() {
        assertEquals("Cell phone number is incorrectly formatted or does not "
                   + "contain an international code. Please correct the number "
                   + "and try again.", message2.checkRecipientCell());
    }

    // ---------- Message hash ----------

    @Test
    public void testMessageHashTestCase1() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
    }

    @Test
    public void testMessageHashLoop() {
        String[] texts = { MESSAGE_1, MESSAGE_2, "Thanks", "Hi Mike, thanks!" };
        String[] expected = { "00:0:HITONIGHT", "00:1:HIPAYMENT", "00:2:THANKSTHANKS", "00:3:HITHANKS" };

        for (int i = 0; i < texts.length; i++) {
            Message m = new Message(ID, i, RECIPIENT_1, texts[i]);
            assertEquals(expected[i], m.createMessageHash());
        }
    }

    // ---------- Message ID ----------

    @Test
    public void testMessageIDIsCreated() {
        Message m = new Message(RECIPIENT_1, MESSAGE_1, 0);
        assertNotNull(m.getMessageID());
        assertEquals(10, m.getMessageID().length());
        assertTrue(m.getMessageID().matches("\\d{10}"));
        assertTrue(m.checkMessageID());
        System.out.println("Message ID generated: " + m.getMessageID());
    }

    @Test
    public void testMessageIDTooLong() {
        Message m = new Message("12345678901", 0, RECIPIENT_1, MESSAGE_1);
        assertFalse(m.checkMessageID());
    }

    // ---------- SentMessage options ----------

    @Test
    public void testSentMessageSend() {
        assertEquals("Message successfully sent.", message1.sentMessage(1));
    }

    @Test
    public void testSentMessageDisregard() {
        assertEquals("Press 0 to delete the message.", message2.sentMessage(2));
    }

    @Test
    public void testSentMessageStore() {
        assertEquals("Message successfully stored.", message1.sentMessage(3));
    }

    // ---------- Totals and printing ----------

    @Test
    public void testReturnTotalMessages() {
        message1.sentMessage(1); // sent
        message2.sentMessage(2); // disregarded - must not count
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessages() {
        message1.sentMessage(1);
        String printed = Message.printMessages();
        assertTrue(printed.contains("00:0:HITONIGHT"));
        assertTrue(printed.contains(RECIPIENT_1));
        assertTrue(printed.contains(MESSAGE_1));
    }

    // ---------- JSON storage ----------

    @Test
    public void testStoreMessageWritesJson() throws IOException {
        assertTrue(message1.storeMessage());
        String json = new String(Files.readAllBytes(tempJson), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"" + RECIPIENT_1 + "\""));
    }
}
