package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.example.multifunctions.api.FunctionsImpl;
import com.example.multifunctions.functions.FunctionsDefinitions;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FunctionCall;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.Struct;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.ChatSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChatInteraction {

    public static Log logger = LogFactory.getLog(ChatInteraction.class);

    public static void main(String[] args) throws IOException {

        String projectId = "mohanganesh";
        String location = "us-central1";
        String modelName = "gemini-1.5-flash-001";

        String prompt = "my zip code is 15090, are there any open slots?";
        chatDiscussion(projectId, location, modelName, prompt);
    }

    public static String chatDiscussion(String projectId, String location, String modelName, String prompt)
            throws IOException {

        String methodName = "chatDiscussion(S,S,S,S) - ";
        logger.info(methodName + "start");

        FunctionsImpl functionsImpl = new FunctionsImpl();
        List<Map<String, String>> conversationHistory = new ArrayList<>();
        conversationHistory.add(Map.of("role", "user", "content", prompt));

        int iteration = 0;
        boolean hasFunctionCall;
        Gson gson = new Gson();

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {

            GenerativeModel model = initializeGenerativeModel(vertexAI, modelName); // Helper function to initialize
                                                                                    // model (see below)
            ChatSession chatSession = new ChatSession(model);

            StringBuilder answer = new StringBuilder();
            do {
                hasFunctionCall = false;
                Content content = null;
                GenerateContentResponse modelResponse = null;

                // Construct Content object with the user's question.
                Content inputContent = null;

                if (iteration == 0) {
                    inputContent = Content.newBuilder().setRole("user")
                            .addParts(Part.newBuilder().setText(prompt).build())
                            .build();
                } else {
                    inputContent = Content.newBuilder().setRole("model")
                            .addParts(Part.newBuilder().setText(answer.toString()).build())
                            .build();
                }

                modelResponse = chatSession.sendMessage(inputContent);

                if (modelResponse == null || modelResponse.getCandidatesList().isEmpty()) {
                    logger.error(methodName + "gemini returned no response or candidates for prompt.");
                    return "Error: No response from the model.";
                }

                StringBuilder currentTurn = new StringBuilder();
                if (!modelResponse.getCandidatesList().isEmpty() && modelResponse.getCandidates(0).getContent() != null
                        && modelResponse.getCandidates(0).getContent().getPartsCount() > 0) {

                    Content resContent = modelResponse.getCandidates(0).getContent();

                    if (resContent != null && resContent.getPartsCount() > 0) {

                        String functionResult = null;

                        for (Part part : resContent.getPartsList()) {
                            if (part.hasText()) {
                                String modelText = part.getText();
                                logger.info(methodName + "text: " + modelText);
                                currentTurn.append(modelText).append(" ");
                            } else if (part.hasFunctionCall()) {
                                logger.info(methodName + "function Call: " + part.getFunctionCall());
                                hasFunctionCall = true;

                                FunctionCall functionCall = part.getFunctionCall();
                                String functionName = functionCall.getName();
                                Struct args = functionCall.getArgs();

                                switch (functionName) {
                                    case "get_appointment":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.findOpenAppointments(args);
                                        logger.info(methodName + "response " + functionResult);
                                        break;
                                    case "confirm_appointment":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.createAppointment(args);
                                        logger.info(methodName + "response " + functionResult);

                                        break;
                                    case "search_member":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.searchMember(args);
                                        logger.info(methodName + "response " + functionResult);

                                        break;
                                    case "create_member":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.createMember(args);
                                        logger.info(methodName + " response " + functionResult);
                                        break;

                                    default:
                                        break;
                                }

                            } else {
                                logger.info(methodName + "structured Data: " + part.toString());
                            }
                        } // end of part for loop
                        conversationHistory.add(Map.of("role", "model", "content", currentTurn.toString().trim()));
                        if (functionResult != null) {
                            // conversationHistory.add(Map.of("role", "model", "content", functionResult));
                            answer.append(functionResult);
                        }
                        answer.append(currentTurn.toString().trim());
                    } // end of content if loop

                } // end of if

                iteration++;
                logger.info("current iteration is " + iteration);
            } // end of do loop
            while (iteration < 3 && hasFunctionCall);
        } // end of try

        logger.info(methodName + "end");
        return "Success";

    } // end of chatDiscussion

    private static Content createContentFromPrompt(String prompt) {
        Content.Builder contentBuilder = Content.newBuilder();
        Gson gson = new Gson();

        try {
            JsonObject jsonObject = gson.fromJson(prompt, JsonObject.class);

            if (jsonObject != null && jsonObject.has("contents")) {
                JsonArray contentsArray = jsonObject.getAsJsonArray("contents");

                for (JsonElement turnElement : contentsArray) {
                    JsonObject turnObject = turnElement.getAsJsonObject();

                    if (turnObject.has("role") && turnObject.has("parts")) {

                        JsonArray partsArray = turnObject.getAsJsonArray("parts");
                        for (JsonElement partElement : partsArray) {
                            JsonObject partObject = partElement.getAsJsonObject();
                            if (partObject.has("text")) {
                                String text = partObject.get("text").getAsString();

                                // Create Part object with only the text
                                Part part = Part.newBuilder()
                                        .setText(text)
                                        .build();

                                contentBuilder.addParts(part);
                            }
                        }
                    }
                }
            } else {
                logger.warn("Invalid prompt format: 'contents' array missing."); // Log the error

            }

        } catch (Exception e) {
            logger.error("Error parsing prompt JSON:", e); // Log and re-throw the exception
        }

        return contentBuilder.build();
    }

    private static String constructPrompt(List<Map<String, String>> conversationHistory) {
        Gson gson = new Gson();
        JsonObject jsonPrompt = new JsonObject();
        JsonArray contents = new JsonArray();

        // Reverse the conversation history to put the latest interaction first
        Collections.reverse(conversationHistory);

        for (Map<String, String> turn : conversationHistory) {
            contents.add(createTurnObject(turn.get("role"), turn.get("content")));
        }

        jsonPrompt.add("contents", contents);
        return gson.toJson(jsonPrompt);
    }

    private static JsonObject createTurnObject(String role, String content) {
        JsonObject turnObject = new JsonObject();
        turnObject.addProperty("role", role);

        JsonArray parts = new JsonArray();
        JsonObject partObject = new JsonObject();
        partObject.addProperty("text", content);
        parts.add(partObject); // Add the text part

        turnObject.add("parts", parts); // Add the parts array to the turn
        return turnObject;
    }

    private static GenerativeModel initializeGenerativeModel(VertexAI vertexAI, String modelName) {
        // ... (set generation config, safety settings, tools, etc., as needed)
        // Construct and return the GenerativeModel
        String systemInstructions = "you are a helpful assistant.\n" +
                "Your mission is to find a open appointment for a given member.\n" +
                "Create new member if the member does not exist with the first, last names and email address.\n"
                +
                "Find the openings  availbale confirm the appointment";

        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                .setMaxOutputTokens(2048)
                .setTemperature(0.5F)
                .setTopK(32)
                .setTopP(1)
                .build();

        Tool tools = Tool.newBuilder()
                .addAllFunctionDeclarations(
                        FunctionsDefinitions.getInstance().getFunctionDeclarations())
                .build();

        return new GenerativeModel(modelName, vertexAI).withGenerationConfig(generationConfig)
                .withSystemInstruction(ContentMaker.fromString(systemInstructions)).withTools(Arrays.asList(tools));

    }

    /**
     * Calls the Gemini model with the given prompt and returns the response.
     *
     * @param projectId The Google Cloud project ID.
     * @param location  The Google Cloud region.
     * @param modelName The name of the Gemini model.
     * @param prompt    The prompt to send to the model.
     * @return The response from the Gemini model.
     * @throws IOException If there is an error communicating with the Gemini model.
     */

    public static GenerateContentResponse callGeminiModel(String projectId, String location, String modelName,
            String prompt)
            throws IOException {
        String methodName = "GenerateContentResponse(S,S,S,S) - ";
        String systemInstructions = "you are a helpful assistant.\n" +
                "Your mission is to find a open appointment for a given member.\n" +
                "Create new member if the member does not exist with the first, last names and email address.\n"
                +
                "Find the openings  availbale confirm the appointment";
        GenerateContentResponse response = null;
        logger.info("");
        logger.info(methodName + " start - calling model with prompt" + prompt);
        logger.info("");
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {

            GenerationConfig generationConfig = GenerationConfig.newBuilder()
                    .setMaxOutputTokens(2048)
                    .setTemperature(0.5F)
                    .setTopK(32)
                    .setTopP(1)
                    .build();

            Tool tools = Tool.newBuilder()
                    .addAllFunctionDeclarations(
                            FunctionsDefinitions.getInstance().getFunctionDeclarations())
                    .build();

            GenerativeModel model = new GenerativeModel(modelName, vertexAI).withGenerationConfig(generationConfig)
                    .withSystemInstruction(ContentMaker.fromString(systemInstructions)).withTools(Arrays.asList(tools));

            ChatSession chatSession = new ChatSession(model);

            response = chatSession.sendMessage(prompt);

        }
        logger.info(methodName + "end - response is generated.");
        return response;
    }

}
