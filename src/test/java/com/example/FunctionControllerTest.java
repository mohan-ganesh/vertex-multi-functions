package com.example;

import org.junit.jupiter.api.BeforeEach;
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
    public void testGetUserById_WhenUserExists() throws Exception {

        System.out.println(functions);
        ResponseEntity<Map> response = userController
                .prompt("", "Could register the user with id w1234 first name John and last name Doe");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(String.class, response.getBody());
    }

}
