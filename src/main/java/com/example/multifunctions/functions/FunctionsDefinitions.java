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
                                .setDescription("Confirm the appointment at the give member email address.")
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
                                                                                                "Member First Name")
                                                                                .build())
                                                                .addRequired("firstName")
                                                                .putProperties("lastName", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Member last name")
                                                                                .build())
                                                                .addRequired("firstName")
                                                                .build())
                                .build();

                addFunctionDeclaration(functionDeclaration_latlong);

                /* Declare the function for the API that we want to invoke for latlang */
                FunctionDeclaration functionDeclaration_medical_appointment = FunctionDeclaration.newBuilder()
                                .setName("get_appointment")
                                .setDescription("Check for any open slot appointments for medical hospital.")
                                .setParameters(
                                                Schema.newBuilder()
                                                                .setType(Type.OBJECT)
                                                                .putProperties(Constants.ZIPCODE_KEY, Schema
                                                                                .newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Check for any open slot appointments for medical hospital")
                                                                                .build())
                                                                .addRequired(Constants.ZIPCODE_KEY)
                                                                .build())
                                .build();
                addFunctionDeclaration(functionDeclaration_medical_appointment);

                FunctionDeclaration functionDeclaration_lookup_member = FunctionDeclaration.newBuilder()
                                .setName("search_member")
                                .setDescription("Check for member/user details by looking up member id or user id.")
                                .setParameters(
                                                Schema.newBuilder()
                                                                .setType(Type.OBJECT)
                                                                .putProperties("member_id", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                // ...
                                                                                .build())
                                                                .putProperties("zipcode", Schema.newBuilder() // Add
                                                                                                              // zipcode
                                                                                                              // definition
                                                                                .setType(Type.STRING)
                                                                                .setDescription("The member's zipcode")
                                                                                .build())
                                                                .addRequired("zipcode") // Now this is correct
                                                                .build())
                                .build();

                addFunctionDeclaration(functionDeclaration_lookup_member);
                FunctionDeclaration functionDeclaration_create_member = FunctionDeclaration.newBuilder()
                                .setName("create_member")
                                .setDescription("Create a new member with First Name, Last Name and Email address.")
                                .setParameters(
                                                Schema.newBuilder()
                                                                .setType(Type.OBJECT)
                                                                .putProperties("member_id", Schema.newBuilder()
                                                                                .setType(Type.STRING)
                                                                                .setDescription(
                                                                                                "Unique member or user id of the type alphanumeric character")
                                                                                .build())
                                                                .addRequired("member_id")
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
