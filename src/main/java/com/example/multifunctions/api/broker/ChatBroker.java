package com.example.multifunctions.api.broker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.multifunctions.config.FirestoreConfig;
import com.example.multifunctions.exception.BrokerException;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.UUID;
import com.google.cloud.Timestamp;

@Service
public class ChatBroker {

    private final Log logger = LogFactory.getLog(ChatBroker.class);

    @Autowired
    FirestoreConfig firestore;

    public boolean messages(String documentId, String sender, String message) {

        String methodName = "messages(S,S,S) - ";

        try {

            logger.info(methodName + " documnet id is " + documentId);
            // Reference to the specified document in the "chat" collection
            ApiFuture<DocumentSnapshot> chatMessageRef = firestore.getConnection().collection("chat")
                    .document(documentId).get();

            if (chatMessageRef.get().exists()) {
                logger.info(methodName + " append");
                // append the info
                // If document exists, append to existing messages
                // If document exists, append to existing messages list
                DocumentReference chatDocMessageRef = firestore.getConnection().collection("chat").document(documentId);
                DocumentSnapshot document = chatMessageRef.get();
                Map<String, Object> existingData = document.getData();
                if (existingData != null) {
                    logger.info(methodName + "existing data");
                    List<Map<String, String>> messages = (List<Map<String, String>>) existingData.get("messages");
                    if (messages == null) {
                        messages = new ArrayList<>();
                    }
                    Map<String, String> newMessage = new HashMap<>();
                    newMessage.put("sender", sender);
                    newMessage.put("message", message);
                    newMessage.put("timestamp", FieldValue.serverTimestamp().toString());
                    messages.add(newMessage);
                    existingData.put("messages", messages);
                } else {
                    logger.info(methodName + "appending the data");
                    existingData = new HashMap<>();
                    List<Map<String, String>> messages = new ArrayList<>();
                    Map<String, String> newMessage = new HashMap<>();
                    newMessage.put("sender", sender);
                    newMessage.put("message", message);
                    newMessage.put("timestamp", FieldValue.serverTimestamp().toString());
                    messages.add(newMessage);
                    existingData.put("messages", messages);
                }

                chatDocMessageRef.set(existingData, SetOptions.merge());
            } else {
                // create new document
                logger.info(methodName + " create new doc id");
                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("sender", sender);
                chatMessage.put("message", message);
                chatMessage.put("timestamp", FieldValue.serverTimestamp());

                List<Map<String, String>> messages = new ArrayList<>();
                Map<String, String> newMessage = new HashMap<>();
                newMessage.put("sender", sender);
                newMessage.put("message", message);
                newMessage.put("timestamp", FieldValue.serverTimestamp().toString());
                messages.add(newMessage);

                ApiFuture<WriteResult> documentReference = firestore.getConnection().collection("chat")
                        .document(documentId).create(chatMessage);
                logger.info(documentReference.toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new BrokerException(methodName, e);
        }

        logger.info(methodName + "end.");
        return true;
    }

    public List<Map<String, Object>> getAllMessages() {
        List<Map<String, Object>> messages = new ArrayList<>();
        // Query the "chat" collection and order by the timestamp
        ApiFuture<QuerySnapshot> future = firestore.getConnection().collection("chat")
                .orderBy("timestamp", Query.Direction.ASCENDING).get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                messages.add(document.getData());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return messages;
    }

}
