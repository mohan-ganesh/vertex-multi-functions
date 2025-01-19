package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.multifunctions.api.IFunctions;
import com.example.multifunctions.controller.FunctionController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import java.util.Map;

public class FunctionControllerTest {

    @InjectMocks
    private FunctionController userController;

    @Autowired
    private IFunctions functions;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Disabled
    public void testGetUserById_WhenUserExists() throws Exception {

        String prompt = "search for  member id is 1737210909999,  If user does not exist create new member with first name John and last name doe email is johndoe@email.com";
        System.out.println(functions);
        System.getProperties().list(System.out);

        ResponseEntity<Map> response = userController
                .prompt("",
                        prompt);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.class, response.getBody());

    }

}
