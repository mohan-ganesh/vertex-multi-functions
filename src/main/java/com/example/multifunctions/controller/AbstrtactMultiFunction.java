package com.example.multifunctions.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.example.multifunctions.api.FunctionsImpl;
import com.example.multifunctions.api.IFunctions;
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
import com.google.gson.Gson;
import com.google.protobuf.Struct;

public abstract class AbstrtactMultiFunction {

    public static Log logger = LogFactory.getLog(AbstrtactMultiFunction.class);

    private static Map<String, String> conversationMemory = new HashMap<>();

    public String systemInstructions = "you are a medical helpful assistant.\n" +
            "Your mission is to find a open appointment for a member for their visit to doctor.\n" +
            "Create new member if the member does not exist with the first, last names and email address.\n" +
            "Find available opening slots and confirm the appointment uponn creation.";

    private static String modelName = "gemini-1.5-flash-002";

    private String location = "us-central1";

    public String service(String promptText, String id) throws Exception {

        return chatDiscussion("mohanganesh", location, modelName, promptText, id);

    }

    public static String chatDiscussion(String projectId, String location, String modelName, String prompt, String id)
            throws IOException, InterruptedException {

        String methodName = "chatDiscussion(S,S,S,S) - ";
        logger.info(methodName + "start");

        FunctionsImpl functionsImpl = new FunctionsImpl();
        StringBuilder conversationHistory = new StringBuilder();

        int iteration = 0;
        boolean hasFunctionCall;
        String answer = "";

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {

            GenerativeModel model = initializeGenerativeModel(vertexAI, modelName); // Helper function to initialize
                                                                                    // model (see below)
            ChatSession chatSession = new ChatSession(model);

            do {
                hasFunctionCall = false;

                GenerateContentResponse modelResponse = null;

                // Construct Content object with the user's question.
                Content inputContent = null;
                logger.info(id);
                if (conversationMemory.containsKey(id)) {
                    String existing = conversationMemory.get(id);
                    conversationHistory.append(answer + "\n" + existing + "\n");
                    conversationMemory.put(id, conversationHistory.toString());

                } else {
                    conversationMemory.put(id, answer.toString());
                }
                logger.info("history - " + conversationMemory.get(id).toLowerCase());

                if (iteration == 0) {

                    String newPrompt = "";
                    if (conversationMemory.containsKey(id)) {
                        newPrompt = prompt + " . Chat History is " + conversationMemory.get(id).toString();
                    }
                    inputContent = Content.newBuilder().setRole("user")
                            .addParts(Part.newBuilder().setText(newPrompt).build())
                            .build();

                } else {

                    String newPrompt = "";
                    if (conversationMemory.containsKey(id)) {
                        newPrompt = answer.toString() + " . Chat History is " + conversationMemory.get(id).toString();
                    }

                    inputContent = Content.newBuilder().setRole("model")
                            .addParts(Part.newBuilder().setText(newPrompt).build())
                            .build();

                }

                logger.info(inputContent.toString());

                Thread.sleep(20000);
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
                                logger.info(part.toString());
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

                        if (functionResult != null) {
                            answer = functionResult;
                            conversationHistory.append(functionResult).append("\n");
                        }
                        if (currentTurn.length() > 0) {
                            answer = currentTurn.toString().trim();
                        }
                    } // end of content if loop

                } // end of if

                iteration++;
                logger.info("current iteration is " + iteration);
            } // end of do loop
            while (iteration < 5 && hasFunctionCall);
        } // end of try

        logger.info(methodName + "end");
        return answer.toString();

    } // end of chatDiscussion

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
}
