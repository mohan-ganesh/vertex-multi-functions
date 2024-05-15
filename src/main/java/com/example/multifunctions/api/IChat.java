package com.example.multifunctions.api;

import java.util.List;
import java.util.Map;

public interface IChat {

    public boolean messages(String documentId, String user, String message);

    public List<Map<String, String>> messages(String documentId);
}
