package com.example.multifunctions.api.broker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.multifunctions.config.FirestoreConfig;
import com.example.multifunctions.exception.BrokerException;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@Service
public class ChatBroker {

    private final Log logger = LogFactory.getLog(ChatBroker.class);

    @Autowired
    FirestoreConfig firestore;

    /**
     * 
     * @param documentId
     * @param sender
     * @param message
     * @return
     */
    public boolean messages(String documentId, String sender, String message) {

        String methodName = "messages(S,S,S) - ";
        logger.info(methodName + " document id is " + documentId);

        // Reference to the specified document in the "chat" collection
        DocumentReference chatMessageRef = firestore.getConnection().collection("chat").document(documentId);
        ApiFuture<DocumentSnapshot> future = chatMessageRef.get();

        try {
            DocumentSnapshot document = future.get();
            Map<String, Object> chatMessageData;
            if (document.exists()) {
                logger.info(methodName + " append");
                // If document exists, append to existing messages list
                chatMessageData = document.getData();
                if (chatMessageData != null) {
                    List<Map<String, String>> messages = (List<Map<String, String>>) chatMessageData.get("messages");
                    if (messages == null) {
                        messages = new ArrayList<>();
                    }
                    Map<String, String> newMessage = new HashMap<>();
                    newMessage.put("sender", sender);
                    newMessage.put("message", message);
                    newMessage.put("timestamp", FieldValue.serverTimestamp().toString()); // Using new Date().toString()
                                                                                          // for simplicity
                    messages.add(newMessage);
                    chatMessageData.put("messages", messages);
                }
            } else {
                logger.info(methodName + " create new doc id");
                // Create new document
                List<Map<String, String>> messages = new ArrayList<>();
                Map<String, String> newMessage = new HashMap<>();
                newMessage.put("sender", sender);
                newMessage.put("message", message);
                newMessage.put("timestamp", FieldValue.serverTimestamp().toString()); // Using new Date().toString() for
                                                                                      // simplicity
                messages.add(newMessage);

                chatMessageData = new HashMap<>();
                chatMessageData.put("messages", messages);
            }
            chatMessageRef.set(chatMessageData, SetOptions.merge());
        } catch (InterruptedException | ExecutionException e) {
            logger.error(methodName + " error", e);
            throw new RuntimeException(methodName, e);
        }

        logger.info(methodName + "end.");
        return true;
    }

    /**
     * 
     * @param documentId
     * @return
     */
    public List<Map<String, String>> messages(String documentId) {
        List<Map<String, String>> messages = new ArrayList<>();
        DocumentReference chatMessageRef = firestore.getConnection().collection("chat").document(documentId);
        ApiFuture<DocumentSnapshot> future = chatMessageRef.get();

        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                Map<String, Object> data = document.getData();
                if (data != null) {
                    messages = (List<Map<String, String>>) data.get("messages");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("messages error", e);
            throw new BrokerException("messages() - " + documentId, e);
        }

        return messages;
    }

}
