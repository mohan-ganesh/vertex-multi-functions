package com.example.multifunctions.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.bp.LocalDateTime;
import org.springframework.http.HttpStatus;

import java.util.Base64;
import java.util.Map;

@RestController
public class FunctionController {

    public static Log logger = LogFactory.getLog(FunctionController.class);

    /**
     *
     * @return String
     */
    @RequestMapping(path = "/healthcheck", method = RequestMethod.GET)
    public ResponseEntity<String> healthcheck() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return ResponseEntity.ok("alive" + currentDateTime.toString());

    }
}
