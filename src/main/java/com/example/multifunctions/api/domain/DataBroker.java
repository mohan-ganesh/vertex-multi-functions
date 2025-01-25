package com.example.multifunctions.api.domain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.example.multifunctions.functions.FunctionsDefinitions;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

public class DataBroker {

    public static Log logger = LogFactory.getLog(DataBroker.class);

    @Value("${storage.bucket.name}") // Inject from properties
    protected String storageBucketName;

    /**
     * 
     * @param projectId
     * @param bucketName
     * @param transactionId
     * @return
     * @throws IOException
     */
    protected static String readChatHistoryFromStorage(String projectId, String bucketName, String transactionId)
            throws IOException {
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, transactionId + ".txt");
        String chatHistory = "";

        try {
            byte[] content = storage.readAllBytes(blobId);
            chatHistory = new String(content, StandardCharsets.UTF_8);
            // logger.info("Chat history read from Google Cloud Storage: gs://" + bucketName
            // + "/" + blobId.getName());

        } catch (com.google.cloud.storage.StorageException e) {
            if (e.getCode() == 404) {
                // logger.info("Blob not found. Returning empty chat history.");
            } else {
                // logger.error("Error reading from GCS:", e);
                // Handle other errors as needed
                throw e; // Or handle differently based on your requirements
            }
        }

        return chatHistory;
    }

    /**
     * 
     * @param projectId
     * @param bucketName
     * @param chatHistory
     * @param transactionId
     * @throws IOException
     */

    protected static void writeChatHistoryToStorage(String projectId, String bucketName, String chatHistory,
            String transactionId) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd"); // Date format
        String dateString = dateFormat.format(new Date());

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, transactionId + "_" + dateString + ".txt");
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()); // Generate timestamp

        try {
            byte[] existingContent = storage.readAllBytes(blobId); // Try to read existing content
            String existingHistory = new String(existingContent, StandardCharsets.UTF_8);
            chatHistory = chatHistory + "\ntimeline : " + timestamp + "\n" + existingHistory; // Append new history to
                                                                                              // existing

        } catch (com.google.cloud.storage.StorageException e) {
            if (e.getCode() == 404) { // Check for "Not Found" error specifically
                logger.info("Blob not found, creating new one."); // Log for clarity
            } else {
                logger.error("Error reading or writing to GCS:", e);
                // Consider other handling: rethrow, alternative action, etc.
                throw e; // Re-throw the exception after logging
            }
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        storage.create(blobInfo, chatHistory.getBytes(StandardCharsets.UTF_8)); // Create or overwrite

        logger.info("Chat history written to Google Cloud Storage: gs://" + bucketName + "/" + blobId.getName());
    }

    protected static GenerativeModel initializeGenerativeModel(VertexAI vertexAI, String modelName) {
        // ... (set generation config, safety settings, tools, etc., as needed)
        // Construct and return the GenerativeModel
        String systemInstructions = "You are a helpful assistant. Your primary mission is to assist in managing doctor appointments for users by following these guidelines.\n"
                +
                "1. **Search for Members**: Ask the user if they know the member id for lookup. The format of the member id will be ###-###-### format. Acknowledge the user's input before proceeding.\n"
                +
                "2. **Create a New Member**: If the member does not exist by looking up with the member id, create a new member profile by asking for the first name, last name, and email address and invoke create_member API.. Acknowledge the provided information and explain why it is needed, also confirm new system generated member id to user.\n"
                +
                "3. **Find Available Slots**: After either member creation or lookup member by id, Search for open doctor available slots that meet the member’s preferences. Clarify the preferences if needed.\n"
                +
                "4. **Schedule Appointment**: Verify appointment details with the user. Upon confirmation, schedule and provide a personalized confirmation number. Use the user's name to personalize the response.\n"
                +
                "5. **Confirmation**: After scheduling, provide the user with a confirmation message that clearly displays the member's name and unique confirmation code.\n"
                +
                "6. **Review Chat History**: Always read the chat history thoroughly to maintain context and provide accurate assistance. Handle cases where the user might not have all the required information by guiding them through the necessary steps.";

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
