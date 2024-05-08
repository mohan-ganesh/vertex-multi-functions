
package com.example.multifunctions;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
This class demonstrates how to use Gemini  for getting deterministic function call names
*/
public class GeminiMultiFunctions {

        public static Log logger = LogFactory.getLog(GeminiMultiFunctions.class);

        public String service(String projectId, String location, String modelName, String promptText) throws Exception {
                String rawResult = callApi(projectId, location, modelName, promptText);
                return rawResult;
        }

        public static String callApi(String projectId, String location,
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
                                                                        .putProperties("latlng", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription(
                                                                                                        "Check for any open slot appointments for medical hospitcal")
                                                                                        .build())
                                                                        .addRequired("zipcode")
                                                                        .build())
                                        .build();

                        // Add the function to a "tool"
                        Tool tool_latlong = Tool.newBuilder()
                                        .addFunctionDeclarations(functionDeclaration_latlong)
                                        .addFunctionDeclarations(functionDeclaration_medical_appointment)
                                        .build();

                        // Invoke the Gemini model with the use of the tool to generate the API
                        // parameters from the prompt input.
                        GenerativeModel geminiModel = new GenerativeModel(modelName, vertexAI)
                                        .withTools(Arrays.asList(tool_latlong));

                        ImmutableList<Tool> setTools = geminiModel.getTools();
                        int toolsSize = setTools.size();
                        logger.debug("number of tools - " + toolsSize);
                        GenerateContentResponse response = geminiModel.generateContent(promptText);
                        Content responseJSONCnt = response.getCandidates(0).getContent();
                        Part functionResponse = responseJSONCnt.getParts(0);

                        String functionName;
                        if (functionResponse.hasFunctionCall()) {
                                FunctionCall functionCall = functionResponse.getFunctionCall();
                                functionName = functionCall.getName();

                        } else {
                                functionName = "undefined";
                        }
                        return functionName;
                }
        }
}