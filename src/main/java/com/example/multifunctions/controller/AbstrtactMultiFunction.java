package com.example.multifunctions.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.example.multifunctions.api.FunctionsImpl;
import com.example.multifunctions.api.IFunctions;
import com.example.multifunctions.api.domain.DataBroker;
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
import org.json.JSONObject;

public abstract class AbstrtactMultiFunction extends DataBroker {

    public static Log logger = LogFactory.getLog(AbstrtactMultiFunction.class);

    private static String modelName = "gemini-1.5-flash-002";

    private String location = "us-central1";

    private static String threadSleepTime = System.getenv("THREAD_SLEEP_TIME");

    // added map based cache initilazation that takes trasnaction id, list of
    // ordered Content object
    private static Map<String, List<Content>> chatHistoryCache = new HashMap<>();

    @Autowired
    IFunctions iFunctions;

    // Add an initializer block to set the default value if threadSleepTime is null
    {
        if (threadSleepTime == null) {
            threadSleepTime = "25000";
        }
    }

    public String service(String promptText, String id) throws Exception {
        String project_id = System.getenv("PROJECT_ID");
        return chatDiscussion(project_id, location, modelName, promptText, id);

    }

    public static String chatDiscussion(String projectId, String location, String modelName, String prompt,
            String transactionId)
            throws IOException, InterruptedException {

        String methodName = "chatDiscussion(S,S,S,S) - ";
        logger.info(methodName + "start");

        FunctionsImpl functionsImpl = new FunctionsImpl();

        int iteration = 0;
        boolean hasFunctionCall;
        String answer = "";

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {

            GenerativeModel model = initializeGenerativeModel(vertexAI, modelName);
            model.startChat();
            ChatSession chatSession = new ChatSession(model);

            String modelResponseOutput = "";

            do {
                hasFunctionCall = false;

                GenerateContentResponse modelResponse = null;

                // Construct Content object with the user's question.
                Content inputContent = null;

                if (chatHistoryCache.get(transactionId) != null) {
                    List<Content> content = chatHistoryCache.get(transactionId);
                    // Collections.reverse(content);
                    logger.info(methodName + "inside do loop that is set to histry -" + content);
                    chatSession.setHistory(content);
                }

                if (iteration == 0) {

                    inputContent = Content.newBuilder()
                            .setRole(iteration == 0 ? "user" : "model")
                            .addParts(Part.newBuilder()
                                    .setText(iteration == 0 ? prompt : answer.toString())
                                    .build())
                            .build();

                    writeChatHistoryToStorage(projectId, storageBucketName, inputContent.toString(), transactionId);
                    logger.info(inputContent);

                    Thread.sleep(Long.parseLong(threadSleepTime));
                    modelResponse = chatSession.sendMessage(inputContent);

                    if (chatHistoryCache.get(transactionId) != null) {
                        List<Content> content = chatHistoryCache.get(transactionId);
                        content.add(inputContent);
                        chatHistoryCache.put(transactionId, content);
                    } else {
                        List<Content> content = new ArrayList<>();
                        content.add(inputContent);
                        chatHistoryCache.put(transactionId, content);
                        logger.info(methodName + "initialize chat histoy");
                    }

                } else {

                    Thread.sleep(Long.parseLong(threadSleepTime));

                    Content modelContent = Content.newBuilder()
                            .setRole("user")
                            .addParts(Part.newBuilder().setText(generateFunctionCallResponseJson(modelResponseOutput)).build())
                            .build();

                            
                    modelResponse = chatSession.sendMessage(modelContent);

                    
                    addToChatHistory("user", transactionId, generateFunctionCallResponseJson(modelResponseOutput));

                    // add the function result for the next input cycle
                    /*
                    

                            
                    if (chatHistoryCache.get(transactionId) != null) {
                        List<Content> content = chatHistoryCache.get(transactionId);
                        content.add(modelContent);
                        chatHistoryCache.put(transactionId, content);
                    }
                         */
                }

                if (modelResponse == null || modelResponse.getCandidatesList().isEmpty()) {
                    logger.error(methodName + "gemini returned no response or candidates for prompt.");
                    return "Error: No response from the model.";
                }

                if (!modelResponse.getCandidatesList().isEmpty() && modelResponse.getCandidates(0).getContent() != null
                        && modelResponse.getCandidates(0).getContent().getPartsCount() > 0) {

                    Content resContent = modelResponse.getCandidates(0).getContent();

                    if (resContent != null && resContent.getPartsCount() > 0) {

                        String functionResult = null;

                        for (Part part : resContent.getPartsList()) {
                            if (part.hasText()) {
                                String modelText = part.getText();
                                logger.info(methodName + "text: " + modelText);
                                writeChatHistoryToStorage(projectId, storageBucketName, "role:model : " + modelText,
                                        transactionId);
                                modelResponseOutput = modelText;
                                answer = modelText;

                                addToChatHistory("model", transactionId, modelResponseOutput);
                                // add the function result for the next input cycle
                                /*
                                 * Content modelContent = Content.newBuilder()
                                 * .setRole("model")
                                 * .addParts(Part.newBuilder().setText(modelResponseOutput).build())
                                 * .build();
                                 * 
                                 * if (chatHistoryCache.get(transactionId) != null) {
                                 * List<Content> content = chatHistoryCache.get(transactionId);
                                 * content.add(modelContent);
                                 * chatHistoryCache.put(transactionId, content);
                                 * }
                                 */

                            } else if (part.hasFunctionCall()) {
                                logger.info(methodName + "function Call: " + part.getFunctionCall());
                                hasFunctionCall = true;

                                FunctionCall functionCall = part.getFunctionCall();
                                String functionName = functionCall.getName();
                                logger.info("*** " + functionName + "***");
                                Struct args = functionCall.getArgs();
                                // generate the request finction call and add that to chat history
                                String functionRequestInput = generateFunctionCallRequestJson(functionName,
                                        functionCall.getArgs());
                                addToChatHistory("model", transactionId, functionRequestInput);

                                switch (functionName) {
                                    case "schedule_appointment":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.createAppointment(args);
                                        writeChatHistoryToStorage(projectId, storageBucketName,
                                                "role:model : " + functionResult,
                                                transactionId);
                                        answer = functionResult;
                                        logger.info(methodName + "response " + functionResult);
                                        break;
                                    case "get_appointment":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.findOpenAppointments(args);
                                        writeChatHistoryToStorage(projectId, storageBucketName,
                                                "role:model : " + functionResult,
                                                transactionId);
                                        answer = functionResult;
                                        logger.info(methodName + "response " + functionResult);
                                        break;
                                    case "confirm_appointment":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.createAppointment(args);
                                        writeChatHistoryToStorage(projectId, storageBucketName,
                                                "role:model : " + functionResult,
                                                transactionId);
                                        logger.info(methodName + "response " + functionResult);

                                        break;
                                    case "search_member":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.searchMember(args);
                                        writeChatHistoryToStorage(projectId, storageBucketName,
                                                "role:model : " + functionResult,
                                                transactionId);
                                        answer = functionResult;
                                        logger.info(methodName + "response " + functionResult);

                                        break;
                                    case "create_member":
                                        logger.info(functionCall + " start");
                                        functionResult = functionsImpl.createMember(args);
                                        writeChatHistoryToStorage(projectId, storageBucketName,
                                                "role:model : " + functionResult,
                                                transactionId);
                                        answer = functionResult;
                                        logger.info(methodName + " response " + functionResult);
                                        break;

                                    default:
                                        logger.error(methodName + "reached default");
                                        break;
                                }

                                modelResponseOutput = functionResult;
                                
                            } else {
                                if (part != null) {
                                    logger.info(methodName + "structured Data: " + part.toString());
                                    answer = part.toString();
                                }

                            }
                        } // end of part for loop

                    } // end of content if loop

                } // end of if

                iteration++;
                logger.info(methodName + "current iteration is " + iteration);
            } // end of do loop
            while (iteration < 3 && hasFunctionCall);
        } // end of try
        logger.info(methodName + "end");
        return answer.toString();

    } // end of chatDiscussion

    /**
     * 
     * @param role
     * @param transactionId
     * @param functionRequestInput
     */
    private static void addToChatHistory(String role, String transactionId, String functionRequestInput) {

        Content modelContent = Content.newBuilder()
                .setRole(role)
                .addParts(Part.newBuilder().setText(functionRequestInput).build())
                .build();

        if (chatHistoryCache.get(transactionId) != null) {
            List<Content> content = chatHistoryCache.get(transactionId);
            content.add(modelContent);
            chatHistoryCache.put(transactionId, content);
        }
    }

    /**
     * 
     * @param functionName
     * @param args
     * @return
     */
    private static String generateFunctionCallRequestJson(String functionName, Struct args) {
        try {
            JSONObject functionCall = new JSONObject();
            functionCall.put("name", functionName);
            functionCall.put("args", args);

            JSONObject outerJson = new JSONObject();
            outerJson.put("functionCall", functionCall);

            return outerJson.toString(2); // Pretty printing

        } catch (Exception e) {
            logger.error("Error generating JSON: " + e.getMessage());
            return "{}";
        }
    }

     /**
     * 
     * @param functionName
     * @param args
     * @return
     */
    private static String generateFunctionCallResponseJson( String args) {
        try {
            JSONObject functionCall = new JSONObject();
            //functionCall.put("name", functionName);
            functionCall.put("args", args);

            JSONObject outerJson = new JSONObject();
            outerJson.put("functionResponse", functionCall);

            return outerJson.toString(2); // Pretty printing

        } catch (Exception e) {
            logger.error("Error generating JSON: " + e.getMessage());
            return "{}";
        }
    }

}
