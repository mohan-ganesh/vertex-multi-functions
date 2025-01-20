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

import com.example.multifunctions.api.IChat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Base64;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class FunctionController extends AbstrtactMultiFunction {

    public static Log logger = LogFactory.getLog(FunctionController.class);

    @Autowired
    IChat iChat;

    /**
     *
     * @return String
     * @throws Exception
     */
    @RequestMapping(path = "/v1/prompt", method = RequestMethod.POST)
    public ResponseEntity<Map> prompt(@RequestHeader(value = "queryId", required = false) String queryId,
            @RequestBody String prompt) throws Exception {

        String id = (queryId == null) ? UUID.randomUUID().toString() : queryId;
        String answer = service(prompt, id);

        Map response = new HashMap();
        response.put("queryId", id);
        response.put("answer", answer);
        return ResponseEntity.ok(response);
    }

    /**
     *
     * @return String
     * @throws Exception
     */
    @RequestMapping(path = "/v1/messages", method = RequestMethod.POST)
    public ResponseEntity<List<Map<String, String>>> prompt(
            @RequestHeader(value = "conversationId", required = true) String conversationId)
            throws Exception {

        return ResponseEntity.ok(iChat.messages(conversationId));
    }

}
