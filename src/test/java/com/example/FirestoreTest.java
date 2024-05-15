package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.multifunctions.api.IChat;
import com.example.multifunctions.api.broker.ChatBroker;

public class FirestoreTest {

    @Autowired
    ChatBroker broker;

    @BeforeEach
    public void setUp() {
        if (broker == null)
            broker = new ChatBroker();
    }

    @Test
    public void testFirestoreConnection() {

        /*
         * broker.addOrUpdateChatMessage("message1", "Alice",
         * "Hello, this is a new message.");
         * broker.appendChatMessage("message1", "Bob",
         * "And this is an appended message.");
         * List<Map<String, Object>> messages = broker.getAllMessages();
         * for (Map<String, Object> message : messages) {
         * System.out.println(message);
         * }
         */
    }
}
