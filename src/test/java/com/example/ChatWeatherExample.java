package com.example;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import java.io.IOException;
import java.util.Arrays;

public class ChatWeatherExample {

    public static void main(String[] args) throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "mohanganesh";
        String location = "us-central1";
        String modelName = "gemini-1.5-flash-001";

        getWeather(projectId, location, modelName);
    }

    public static void getWeather(String projectId, String location, String modelName)
            throws IOException {

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            ChatSession chatSession = new ChatSession(model);

            // Construct Content object with the user's question.
            Content content = Content.newBuilder().setRole("user")
                    .addParts(Part.newBuilder().setText("What is the weather like today?").build())
                    .build();

            GenerateContentResponse response = chatSession.sendMessage(content);

            // Process the response. This is a placeholder; replace with your actual
            // processing logic.
            if (response.getCandidatesCount() > 0) {
                System.out.println(response.getCandidates(0).getContent().getParts(0).getText());
            } else {
                System.out.println("Model returned no response.");
            }
        }
    }
}
