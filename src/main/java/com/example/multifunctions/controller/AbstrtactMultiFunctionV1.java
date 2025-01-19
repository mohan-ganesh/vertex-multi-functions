
package com.example.multifunctions.controller;

import com.example.multifunctions.api.IChat;
import com.example.multifunctions.api.IFunctions;
import com.example.multifunctions.functions.FunctionsDefinitions;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.FunctionResponseOrBuilder;
import com.google.cloud.vertexai.api.FunctionCall;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.common.collect.ImmutableList;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.SafetySetting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.Struct;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

/*
This class demonstrates how to use Gemini  for getting deterministic function call names
*/
abstract class AbstrtactMultiFunctionV1 {

        public static Log logger = LogFactory.getLog(AbstrtactMultiFunctionV1.class);

        public String systemInstructions = "you are a helpful assistant.\n" +
                        "Your mission is to find a open appointment for a given member.\n" +
                        "Create new member if the member does not exist with the first, last names and email address.\n"
                        +
                        "Find the openings  availbale confirm the appointment";

        private static String modelName;

        @Value("${model.name:gemini-1.5-flash-002}")
        public void setModelName(String value) {
                modelName = value;
        }

        private String location;

        @Value("${location.default:us-central1}")
        public void setLocation(String value) {
                location = value;
        }

        @Autowired
        IFunctions iFunctions;

        @Autowired
        IChat iChat;

        public String service(String promptText, String id) throws Exception {
                GenerateContentResponse response;
                String answer = null;
                Part functionResponse = null;
                List<Map<String, String>> conversationHistory = new ArrayList<>();
                // Add initial user message
                conversationHistory.add(Map.of("role", "User", "content", promptText));

                do {
                        response = callModel(constructPrompt(conversationHistory));
                        if (response == null || response.getCandidatesList().isEmpty()) {
                                logger.error("Gemini returned no response or candidates for prompt: "
                                                + constructPrompt(conversationHistory));
                                return "Error: No response from the model."; // Or another appropriate error handling
                        }

                        Content responseJSONCnt = response.getCandidates(0).getContent();

                        if (responseJSONCnt.getPartsCount() > 0) {
                                functionResponse = responseJSONCnt.getParts(0);
                                answer = ResponseHandler.getText(response);
                                if (answer != null) {
                                        // Append only the model's response to the conversation history
                                        conversationHistory.add(Map.of("role", "Model", "content", answer));
                                } else {
                                        logger.warn("Model returned no text content for prompt: "
                                                        + constructPrompt(conversationHistory));
                                        // Decide whether to continue or exit here. You could add a default response.
                                        conversationHistory.add(Map.of("role", "Model", "content",
                                                        "I'm not sure how to respond to that."));
                                }
                        }

                        String functionName = null;
                        if (functionResponse != null && functionResponse.hasFunctionCall()) {
                                FunctionCall functionCall = functionResponse.getFunctionCall();
                                functionName = functionCall.getName();
                                Struct args = functionCall.getArgs();

                                logger.info("determined function name is + " + functionName);
                                logger.info("arguments for functions - " + args.toString());
                                String apiFunctionResponse = "";
                                switch (functionName) {

                                        case "get_address":
                                                logger.info(functionCall + " start");
                                                // Implementation or call to function
                                                logger.info(functionCall + " end");
                                                break;
                                        case "search_member":
                                                logger.info(functionCall + " start");
                                                apiFunctionResponse = iFunctions.searchMember(args);
                                                logger.info(functionCall + " end");
                                                break;
                                        case "create_member":
                                                logger.info(functionCall + " start");
                                                apiFunctionResponse = iFunctions.createMember(args);
                                                logger.info(functionCall + " response " + apiFunctionResponse);
                                                break;

                                        default:
                                                break;
                                }
                                // conversationHistory.add(Map.of("role", "Function", "content",
                                // functionName + " with args: " + args)); // Add function call to history
                                conversationHistory.add(Map.of("role", "model", "content",
                                                apiFunctionResponse));

                                // function

                        }
                } while (functionResponse != null && functionResponse.hasFunctionCall()
                                && response.getCandidates(0).getContent().getPartsCount() > 0);

                return formatConversationHistory(conversationHistory);

        }

        private String constructPrompt(List<Map<String, String>> conversationHistory) {
                Gson gson = new Gson();
                JsonObject jsonPrompt = new JsonObject();
                JsonArray contents = new JsonArray();

                for (Map<String, String> turn : conversationHistory) {
                        JsonObject turnObject = new JsonObject();
                        turnObject.addProperty("role", turn.get("role"));

                        JsonArray parts = new JsonArray();
                        JsonObject partObject = new JsonObject();
                        partObject.addProperty("text", turn.get("content"));
                        parts.add(partObject);

                        turnObject.add("parts", parts); // Use .add() here
                        contents.add(turnObject);
                }

                jsonPrompt.add("contents", contents); // Use .add() here
                return gson.toJson(jsonPrompt);
        }

        private String formatConversationHistory(List<Map<String, String>> conversationHistory) {
                StringBuilder formattedHistory = new StringBuilder();
                for (Map<String, String> turn : conversationHistory) {
                        formattedHistory.append(turn.get("role")).append(": ").append(turn.get("content")).append("\n");
                }
                return formattedHistory.toString();
        }

        private GenerateContentResponse callModel(String promptText)
                        throws IOException, InterruptedException {

                String projectId = System.getenv("PROJECT_ID");

                if (projectId == null) {
                        throw new ExceptionInInitializerError("projectId is required.");
                }

                try (VertexAI vertexAI = new VertexAI(projectId, location)) {

                        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                                        .setMaxOutputTokens(2048)
                                        .setTemperature(0.5F)
                                        .setTopK(32)
                                        .setTopP(1)
                                        .build();

                        List<SafetySetting> safetySettings = Arrays.asList(
                                        SafetySetting.newBuilder()
                                                        .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                                                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                                                        .build(),
                                        SafetySetting.newBuilder()
                                                        .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                                                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                                                        .build());

                        Tool tools = Tool.newBuilder()
                                        .addAllFunctionDeclarations(
                                                        FunctionsDefinitions.getInstance().getFunctionDeclarations())
                                        .build();
                        if (modelName == null) {
                                modelName = "gemini-1.5-flash-002";

                        }
                        GenerativeModel geminiModel = new GenerativeModel(modelName, vertexAI)
                                        .withSystemInstruction(ContentMaker.fromString(systemInstructions))
                                        .withTools(Arrays.asList(tools))
                                        .withGenerationConfig(generationConfig)
                                        .withSafetySettings(safetySettings);
                        logger.info("calling model with prompt" + promptText);
                        GenerateContentResponse response = geminiModel.generateContent(promptText);

                        return response;
                }
        }
}