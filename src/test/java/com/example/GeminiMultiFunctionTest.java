package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import com.example.multifunctions.*;

public class GeminiMultiFunctionTest {

    public static Log logger = LogFactory.getLog(GeminiMultiFunctionTest.class);

    /*
     * @Test
     * public void testSubtraction() {
     * try {
     * String projectId = System.getenv("PROJECT_ID");
     * String location = "us-central1";
     * String modelName = "gemini-1.5-pro-preview-0409";
     * // String promptText = "are there any hospital appointment slots available at
     * // zipcode 15090?";
     * 
     * String promptText = "are there any appointments available??";
     * AbstrtactMultiFunction function = new AbstrtactMultiFunction();
     * long startTime = System.nanoTime();
     * String functionName = function.service(projectId, location, modelName,
     * promptText);
     * logTimeTaken(startTime, System.nanoTime());
     * System.out.println(functionName);
     * logger.debug("detrmined function name" + functionName);
     * } catch (Exception genException) {
     * logger.error("testSubtraction()", genException);
     * }
     * }
     * 
     */

    private void logTimeTaken(long startTime, long endTime) {
        long durationInNano = endTime - startTime;
        double durationInSeconds = (double) durationInNano / 1_000_000_000.0;
        logger.info("Time taken to execute the function: " + durationInSeconds + " seconds");

    }
}
