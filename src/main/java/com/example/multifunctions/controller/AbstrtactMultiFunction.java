
package com.example.multifunctions.controller;

import com.example.multifunctions.api.IFunctions;
import com.example.multifunctions.functions.FunctionsDefinitions;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.FunctionResponseOrBuilder;
import com.google.cloud.vertexai.api.FunctionCall;
import com.google.cloud.vertexai.api.GenerateContentResponse;
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

/*
This class demonstrates how to use Gemini  for getting deterministic function call names
*/
abstract class AbstrtactMultiFunction {

        public static Log logger = LogFactory.getLog(AbstrtactMultiFunction.class);

        @Value("${model.name}")
        private String modelName;

        @Value("${location.default}")
        private String location;

        @Autowired
        IFunctions iFunctions;

        public String service(String promptText) throws Exception {
                GenerateContentResponse response = callModel(promptText);

                Content responseJSONCnt = response.getCandidates(0).getContent();

                Part functionResponse = null;
                if (responseJSONCnt.getPartsCount() > 0) {
                        functionResponse = responseJSONCnt.getParts(0);
                }

                String functionName;
                if (functionResponse != null && functionResponse.hasFunctionCall()
                                && responseJSONCnt.getPartsCount() > 0) {
                        FunctionCall functionCall = functionResponse.getFunctionCall();
                        functionName = functionCall.getName();
                        Struct args = functionCall.getArgs();

                        logger.info("arguments - " + args.toString());
                        String apiFunctionResponse = "";
                        switch (functionName) {

                                case "get_address":
                                        logger.info(functionCall + " start");

                                        logger.info(functionCall + " end");
                                        break;
                                case "search_member":
                                        logger.info(functionCall + " start");
                                        logger.info(functionCall + " end");
                                        break;
                                case "create_member":
                                        logger.info(functionCall + " start");
                                        apiFunctionResponse = iFunctions.createMember(args);
                                        logger.info(functionCall + " end");
                                        break;

                                default:
                                        break;
                        }

                } else {
                        functionName = "undefined";
                        logger.info(responseJSONCnt.toString());
                        // logger.info(functionResponse.toString());
                }
                return functionName;

        }

        private GenerateContentResponse callModel(String promptText)
                        throws IOException, InterruptedException {

                String projectId = System.getenv("PROJECT_ID");

                if (projectId == null) {
                        throw new ExceptionInInitializerError("projectId is required.");
                }

                try (VertexAI vertexAI = new VertexAI(projectId, location)) {

                        Tool tools = Tool.newBuilder()
                                        .addAllFunctionDeclarations(
                                                        FunctionsDefinitions.getInstance().getFunctionDeclarations())
                                        .build();
                        GenerativeModel geminiModel = new GenerativeModel(modelName, vertexAI)
                                        .withTools(Arrays.asList(tools));

                        GenerateContentResponse response = geminiModel.generateContent(promptText);

                        return response;
                }
        }
}