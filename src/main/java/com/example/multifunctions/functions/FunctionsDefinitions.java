package com.example.multifunctions.functions;

import java.util.ArrayList;

import com.example.multifunctions.api.domain.Constants;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Type;

public class FunctionsDefinitions implements Constants {

        private ArrayList<com.google.cloud.vertexai.api.FunctionDeclaration> functions = new ArrayList<>();

        // Private constructor to prevent instantiation from outside
        private FunctionsDefinitions() {
                init();
        }

        private void init() {

                /* Declare the function for the API that we want to invoke (Geo coding API) */

                FunctionDeclaration functionDeclaration_latlong = FunctionDeclaration.newBuilder()
                                .setName("confirm_appointment")
                                .setDescription("Confirms an appointment for a specified member, identified by member ID and email address. Returns a confirmation message upon successful confirmation.")
                                .setParameters(
                                                Schema.newBuilder()
                                                                .setType(Type.OBJECT)
                                                                .putProperties("email", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "This must be a string of valid email address.")
                                                                                .build())
                                                                .addRequired("email")
                                                                .putProperties("member_id", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Unique member or user id of the type alphanumeric character")
                                                                                .build())
                                                                .addRequired("member_id")
                                                                .putProperties("firstName", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Member first name")
                                                                                .build())
                                                                .addRequired("firstName")
                                                                .putProperties("lastName", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Member last name")
                                                                                .build())
                                                                .addRequired("lastName")
                                                                .build())
                                .build();

                // addFunctionDeclaration(functionDeclaration_latlong);

                FunctionDeclaration function_schedule_appointment = FunctionDeclaration.newBuilder()
                                .setName("schedule_appointment")
                                .setDescription("Schedules a new appointment for a member.")
                                .setParameters(
                                                Schema.newBuilder()
                                                                .setType(Type.OBJECT)
                                                                .putProperties("email", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "This must be a string of valid email address.")
                                                                                .build())
                                                                .addRequired("email")
                                                                .putProperties("member_id", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Unique member or user id of the type alphanumeric character")
                                                                                .build())
                                                                .addRequired("member_id")
                                                                .putProperties("firstName", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Member first name")
                                                                                .build())
                                                                .addRequired("firstName")
                                                                .putProperties("lastName", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Member last name")
                                                                                .build())
                                                                .addRequired("lastName")
                                                                .build())
                                .build();

                addFunctionDeclaration(function_schedule_appointment);

                /* Declare the function for the API that we want to invoke for latlang */
                FunctionDeclaration functionDeclaration_medical_appointment = FunctionDeclaration.newBuilder()
                                .setName("get_available_slots")
                                .setDescription("Retrieves available slots based on provided search parameters (e.g., zip code)")
                                .setParameters(
                                                Schema.newBuilder()
                                                                .setType(Type.OBJECT)
                                                                .putProperties(Constants.ZIPCODE_KEY, Schema
                                                                                .newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Check for any open slot appointments for doctor visit")
                                                                                .build())
                                                                .addRequired(Constants.ZIPCODE_KEY)
                                                                .build())
                                .build();
                addFunctionDeclaration(functionDeclaration_medical_appointment);

                FunctionDeclaration functionDeclaration_lookup_member = FunctionDeclaration.newBuilder()
                                .setName("search_member")
                                .setDescription("Searches for a member record using the specified member ID. Returns the member's details if found; otherwise, returns an appropriate error message indicating that the member was not found.")
                                .setParameters(
                                                Schema.newBuilder()
                                                                .setType(Type.OBJECT)
                                                                .putProperties(Constants.MEMBER_ID_KEY, Schema
                                                                                .newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription("Unique member or user id of the type alphanumeric character")
                                                                                .build())
                                                                .addRequired(Constants.MEMBER_ID_KEY)
                                                                .build())
                                .build();

                addFunctionDeclaration(functionDeclaration_lookup_member);
                FunctionDeclaration functionDeclaration_create_member = FunctionDeclaration.newBuilder()
                                .setName("create_member")
                                .setDescription("Creates a new member record with the provided first name, last name, and email address, returning a unique member ID.")
                                .setParameters(
                                                Schema.newBuilder()
                                                                .setType(Type.OBJECT)
                                                                .putProperties("firstName", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Member first name")
                                                                                .build())
                                                                .addRequired("firstName")
                                                                .putProperties("lastName", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Member last name")
                                                                                .build())
                                                                .addRequired("lastName")
                                                                .putProperties("email", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Member email address")
                                                                                .build())
                                                                .addRequired("email")
                                                                .build())
                                .build();

                addFunctionDeclaration(functionDeclaration_create_member);

        }

        // Static instance of FunctionDefinitions
        private static FunctionsDefinitions instance;

        // Static method to get the instance of FunctionDefinitions
        public static synchronized FunctionsDefinitions getInstance() {
                if (instance == null) {
                        instance = new FunctionsDefinitions();
                }
                return instance;
        }

        // Method to retrieve the Iterable<? extends
        // com.google.cloud.vertexai.api.FunctionDeclaration>
        public Iterable<? extends com.google.cloud.vertexai.api.FunctionDeclaration> getFunctionDeclarations() {
                // Sample implementation returning an empty list
                return functions;
        }

        // Example method to add function declarations
        public void addFunctionDeclaration(com.google.cloud.vertexai.api.FunctionDeclaration declaration) {
                functions.add(declaration);
        }

}
