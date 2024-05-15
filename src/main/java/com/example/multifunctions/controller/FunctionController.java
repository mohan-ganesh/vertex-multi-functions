package com.example.multifunctions.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.bp.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
public class FunctionController extends AbstrtactMultiFunction {

    public static Log logger = LogFactory.getLog(FunctionController.class);

    /**
     *
     * @return String
     * @throws Exception
     */
    @RequestMapping(path = "/v1/prompt", method = RequestMethod.POST)
    public ResponseEntity<Map> prompt(@RequestHeader(value = "conversationId", required = false) String conversationId,
            @RequestParam String prompt) throws Exception {

        String id = (conversationId == null) ? UUID.randomUUID().toString() : conversationId;
        String answer = service(prompt, id);

        Map response = new HashMap();
        response.put("conversationId", id);
        response.put("answer", answer);

        return ResponseEntity.ok(response);
    }
}
