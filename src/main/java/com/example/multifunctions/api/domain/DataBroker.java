package com.example.multifunctions.api.domain;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.example.multifunctions.domain.Instructions;
import com.example.multifunctions.functions.FunctionsDefinitions;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private List<String> listSystemInstructions;

    public DataBroker() {
        loadSystemInstructions();
    }

    private void loadSystemInstructions() {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("system-instructions.json")) {
            if (inputStream == null) {
                throw new IOException("Resource not found: instructions.json");
            }
            Instructions instructions = objectMapper.readValue(inputStream, Instructions.class);
            this.listSystemInstructions = instructions.getInstructions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getter for systemInstructions
    public List<String> getSystemInstructions() {
        return listSystemInstructions;
    }

    // Method to concatenate all instructions into a single string
    public String getAllInstructions() {
        return listSystemInstructions.stream().collect(Collectors.joining());
    }

    // Inner class to map JSON structure
    private static class Instructions {
        private List<String> instructions;

        public List<String> getInstructions() {
            return instructions;
        }

        public void setInstructions(List<String> instructions) {
            this.instructions = instructions;
        }
    }

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
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy"); // Date format for folder
        String dateString = dateFormat.format(new Date());
        String folderName = dateString; // Folder name

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, folderName + "/" + transactionId + "_" + dateString + ".txt");
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()); // Generate timestamp

        try {
            byte[] existingContent = storage.readAllBytes(blobId); // Try to read existing content
            String existingHistory = new String(existingContent, StandardCharsets.UTF_8);
            chatHistory = chatHistory + " ,timeline : " + timestamp + "\n" + existingHistory; // Append new history to
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

    protected GenerativeModel initializeGenerativeModel(VertexAI vertexAI, String modelName) {
        // ... (set generation config, safety settings, tools, etc., as needed)
        // Construct and return the GenerativeModel

        logger.info(getSystemInstructions());

        String systemInstructions = "You are a helpful and friendly assistant specializing in managing doctor appointments. Your primary goal is to assist users in booking appointments by following these guidelines.\n"
                + "You will be provided with the conversation history to understand the user's needs. You can only proceed with booking an appointment after you have collected the member ID, first name, last name, email address, and zip code from the user, the user has explicitly confirmed the appointment time and details and you have successfully received a response from the `schedule_appointment` function.\n"
                + "The process begins with finding the member's details and available appointment slots.\n"
                +
                "1. **Member ID Lookup**: Begin by asking the user if they know their member ID. The member ID format is ###-###-###. If provided, validate the format. If the format is incorrect, ask the user to provide the member ID in the correct format. If a valid member ID is provided, use the `search_member` function to retrieve member details. Acknowledge the user's input before proceeding. Once member details are retrieved, ask the user for their zip code. Acknowledge the provided zip code.\n"
                +
                "2. **New Member Creation**: If the member does not exist (based on the response from `search_member`), ask for the member's first name, last name, and email address. Explain why this information is needed. Once provided, use the `create_member` function to create the member profile. After the member is created, confirm the system-generated member ID to the user. Acknowledge the provided information. Then, ask the user for their zip code. Acknowledge the provided zip code.\n"
                +
                "3. **Find Available Slots:** Once the member details and zip code are confirmed or created, use the `get_available_slots` function to find available appointment slots based on the member's information. Present the available slots to the user. If no slots are available, inform the user and then:\n"
                + "    *   Ask if they have any flexibility in their preferred date or time.\n"
                + "    *   If possible, suggest alternative dates or times based on the information provided by `get_available_slots`. If the function provides alternative options, present them to the user.\n"
                + "    *   If no alternative slots are available, inform the user that there are no slots matching their criteria and that they can try again later or contact support for further assistance.\n"
                +
                "4. **Confirm Appointment Details:** After the user selects an available slot, clearly state the appointment details (date, time, member name) and ask the user to confirm if they would like to proceed with the booking. Only proceed to the next step if the user explicitly confirms the appointment.\n"
                +
                "5. **Check Booking Status:** Before scheduling the appointment, verify that you have all the necessary information: member ID, first name, last name, email, zip code, and explicit user confirmation. If any information is missing, ask the user to provide it. \n"
                +
                "6. **Schedule Appointment**: Once the user confirms and you have all the required information, use the `schedule_appointment` function to schedule the appointment. After receiving a successful response from `schedule_appointment`, provide a personalized confirmation number. Use the user's name to personalize the response.\n"
                +
                "7. **Confirmation Message:** Display a confirmation message to the user, such as: \"Your appointment is confirmed, [Member Name]. Your confirmation code is [Confirmation Code].\"\n"

                + "8. **Chat History Review:** Always thoroughly review the chat history to maintain context and provide accurate assistance. If the user is missing any required information, guide them through the necessary steps.\n"
                + "9. **Function Call Usage:** Invoke functions when the user has provided all the required arguments. Do not make assumptions about the values to plug into function arguments. If you're waiting for a function's response, do not give the user the impression that the booking process is complete.\n"
                + "10. **Clarification:** If a user request is ambiguous or unclear, ask for clarification. If the user provides invalid information (e.g. incorrect email format), ask them to provide the information again in the correct format.\n"
                + "11. **Error Handling**: If an error occurs during any function call, inform the user that there was an issue and ask them to try again or provide any further information needed to resolve the issue. If you are waiting for a response from a function, do not give the user the impression that the booking process is complete. \n";

        systemInstructions = getAllInstructions();

        logger.info(systemInstructions);
        GenerationConfig generationConfig = GenerationConfig.newBuilder()
                .setMaxOutputTokens(8192)
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
