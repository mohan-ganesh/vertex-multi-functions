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

        String methodName = "addOrUpdateChatMessage(S,S,S) - ";

        try {

            logger.info(methodName + " documnet id is " + documentId);
            // Reference to the specified document in the "chat" collection
            ApiFuture<DocumentSnapshot> chatMessageRef = firestore.getConnection().collection("chat")
                    .document(documentId).get();

            if (chatMessageRef.get().exists()) {
                // append the info
                // If document exists, append to existing messages
                Map<String, Object> existingData = chatMessageRef.get().getData();
                if (existingData != null) {
                    String existingMessage = (String) existingData.get("message");
                    existingData.put("message", existingMessage + "\n" + message);
                } else {
                    existingData = new HashMap<>();
                    existingData.put("message", message);
                }
                existingData.put("sender", sender);
                existingData.put("timestamp", FieldValue.serverTimestamp());
                // chatMessageRef.(existingData, SetOptions.merge());

            } else {
                // create new document

                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("sender", sender);
                chatMessage.put("message", message);
                chatMessage.put("timestamp", FieldValue.serverTimestamp());

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
