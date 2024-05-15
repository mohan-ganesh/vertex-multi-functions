package com.example.multifunctions.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.multifunctions.api.broker.ChatBroker;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Component
public class ChatImpl implements IChat {

    private final Log logger = LogFactory.getLog(ChatImpl.class);

    @Autowired
    ChatBroker chatBroker;

    @Override
    public boolean messages(String documentId, String user, String message) {

        return chatBroker.messages(documentId, user, message);
    }

    @Override
    public List<Map<String, String>> messages(String documentId) {
        return chatBroker.messages(documentId);
    }

}
