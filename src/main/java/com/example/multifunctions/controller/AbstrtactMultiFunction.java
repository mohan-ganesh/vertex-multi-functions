
package com.example.multifunctions.controller;

import com.example.multifunctions.api.IFunctions;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.FunctionDeclaration;
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

/*
This class demonstrates how to use Gemini  for getting deterministic function call names
*/
abstract class AbstrtactMultiFunction {

        public static Log logger = LogFactory.getLog(AbstrtactMultiFunction.class);

        @Autowired
        IFunctions ifunction;

        public String service(String projectId, String location, String modelName, String promptText) throws Exception {
                String rawResult = callApi(projectId, location, modelName, promptText);
                return rawResult;
        }

        private String callApi(String projectId, String location,
                        String modelName, String promptText)
                        throws IOException, InterruptedException {

                try (VertexAI vertexAI = new VertexAI(projectId, location)) {
                        /* Declare the function for the API that we want to invoke (Geo coding API) */
                        FunctionDeclaration functionDeclaration_latlong = FunctionDeclaration.newBuilder()
                                        .setName("get_address")
                                        .setDescription("Get the address for the given latitude and longitude value.")
                                        .setParameters(
                                                        Schema.newBuilder()
                                                                        .setType(Type.OBJECT)
                                                                        .putProperties("latlng", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription(
                                                                                                        "This must be a string of latitude and longitude coordinates separated by comma")
                                                                                        .build())
                                                                        .addRequired("latlng")
                                                                        .build())
                                        .build();

                        /* Declare the function for the API that we want to invoke for latlang */
                        FunctionDeclaration functionDeclaration_medical_appointment = FunctionDeclaration.newBuilder()
                                        .setName("get_appointment")
                                        .setDescription("Check for any open slot appointments for medical hospitcal.")
                                        .setParameters(
                                                        Schema.newBuilder()
                                                                        .setType(Type.OBJECT)
                                                                        .putProperties("zipcode", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription(
                                                                                                        "Check for any open slot appointments for medical hospitcal")
                                                                                        .build())
                                                                        .addRequired("zipcode")
                                                                        .build())
                                        .build();

                        FunctionDeclaration functionDeclaration_lookup_member = FunctionDeclaration.newBuilder()
                                        .setName("search_member")
                                        .setDescription("Check for user by looking up member id or user id.")
                                        .setParameters(
                                                        Schema.newBuilder()
                                                                        .setType(Type.OBJECT)
                                                                        .putProperties("member_id", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription(
                                                                                                        "Unique member or user id of the type alphanumeric character")
                                                                                        .build())
                                                                        .addRequired("zipcode")
                                                                        .build())
                                        .build();

                        FunctionDeclaration functionDeclaration_create_member = FunctionDeclaration.newBuilder()
                                        .setName("create_member")
                                        .setDescription("Check for user by looking up member id or user id.")
                                        .setParameters(
                                                        Schema.newBuilder()
                                                                        .setType(Type.OBJECT)
                                                                        .putProperties("member_id", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription(
                                                                                                        "Unique member or user id of the type alphanumeric character")
                                                                                        .build())
                                                                        .addRequired("zipcode")
                                                                        .putProperties("firstName", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription(
                                                                                                        "Member First Name")
                                                                                        .build())
                                                                        .addRequired("firstName")
                                                                        .putProperties("lastName", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription(
                                                                                                        "Member last name")
                                                                                        .build())
                                                                        .addRequired("firstName")
                                                                        .putProperties("email", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription(
                                                                                                        "Member email address")
                                                                                        .build())
                                                                        .addRequired("email")
                                                                        .build())
                                        .build();

                        // Add the function to a "tool"
                        Tool tool_latlong = Tool.newBuilder()
                                        .addFunctionDeclarations(functionDeclaration_latlong)
                                        .addFunctionDeclarations(functionDeclaration_medical_appointment)
                                        .addFunctionDeclarations(functionDeclaration_lookup_member)
                                        .addFunctionDeclarations(functionDeclaration_create_member)
                                        .build();

                        // Invoke the Gemini model with the use of the tool to generate the API
                        // parameters from the prompt input.
                        GenerativeModel geminiModel = new GenerativeModel(modelName, vertexAI)
                                        .withTools(Arrays.asList(tool_latlong));

                        logger.info("number of functions - " + tool_latlong.getFunctionDeclarationsCount());
                        GenerateContentResponse response = geminiModel.generateContent(promptText);

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
                                                apiFunctionResponse = ifunction.createMember(args);
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
        }
}