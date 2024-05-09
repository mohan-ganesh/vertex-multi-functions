package com.example.multifunctions.api;

import org.apache.commons.logging.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.multifunctions.api.domain.Member;
import com.google.protobuf.Struct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FunctionsImpl implements IFunctions {

    public static Log logger = LogFactory.getLog(FunctionsImpl.class);

    String API_URL = "https://test.conversation.goengen.com/appointment/user";

    @Override
    public String createMember(Struct args) {
        logger.info("createMember() - start");
        logger.info(args.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Member member = new Member();
        member.setEmail(args.getFieldsMap().get("email").toString());
        member.setFirstName(args.getFieldsMap().get("firstName").toString());
        member.setLastName(args.getFieldsMap().get("firstName").toString());
        member.setMemberId("" + System.currentTimeMillis());
        // Create request body
        HttpEntity<Member> request = new HttpEntity<>(member, headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, String.class);

        // Get response body
        String responseBody = response.getBody();

        logger.info("createMember() - end" + responseBody);
        return responseBody;
    }

}
