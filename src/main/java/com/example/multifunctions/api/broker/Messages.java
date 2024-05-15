package com.example.multifunctions.api.broker;

import java.util.List;
import java.util.Map;

public class Messages {

    List<Map<String, String>> message = null;

    public Messages(List<Map<String, String>> messages) {

        this.message = messages;
    }

    public List<Map<String, String>> getMessage() {
        return message;
    }

    public void setMessage(List<Map<String, String>> message) {
        this.message = message;
    }

}
