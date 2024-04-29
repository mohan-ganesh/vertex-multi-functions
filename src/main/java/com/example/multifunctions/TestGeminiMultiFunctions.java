package com.example.multifunctions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestGeminiMultiFunctions {

    public static Log logger = LogFactory.getLog(TestGeminiMultiFunctions.class);

    public static void main(String args[]) {

        try {

            String projectId = System.getenv("PROJECT_ID");
            String location = "us-central1";
            String modelName = "gemini-1.5-pro-preview-0409";
            // String promptText = "are there any hospital appointmnet slots available at
            // zipcode 15090?";

            String promptText = "What's the address for the 40.714224,-73.961452 value ?";
            GeminiMultiFunctions function = new GeminiMultiFunctions();
            String functionName = function.service(projectId, location, modelName, promptText);
            logger.debug("detrmined function name" + functionName);
        } catch (Exception e) {
            logger.error("service()", e);
        }

    }
}
