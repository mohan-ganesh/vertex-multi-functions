package com.example.multifunctions.api;

import org.apache.commons.logging.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.multifunctions.api.domain.Constants;
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
import com.google.protobuf.Value;

@Service
public class FunctionsImpl implements IFunctions, Constants {

    public static Log logger = LogFactory.getLog(FunctionsImpl.class);

    String API_URL = "https://test.conversation.goengen.com/appointment";

    @Override
    public String createMember(Struct args) {
        logger.info("createMember() - start");
        logger.info(args.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Member member = new Member();
        member.setEmail(args.getFieldsMap().get(Constants.EMAIL_KEY).toString());
        member.setFirstName(args.getFieldsMap().get(Constants.FIRSTNAME_KEY).toString());
        member.setLastName(args.getFieldsMap().get(Constants.LASTNAME_KEY).toString());
        member.setMemberId("" + System.currentTimeMillis());
        // Create request body
        HttpEntity<Member> request = new HttpEntity<>(member, headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(API_URL + "/user", HttpMethod.POST, request,
                String.class);

        // Get response body
        String responseBody = response.getBody();

        logger.info("createMember() - end" + responseBody);
        return responseBody;
    }

    @Override
    public String searchMember(Struct args) {
        logger.info("searchMember() - start");
        logger.info(args.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Create request body
        HttpEntity<Member> request = new HttpEntity<>(headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Send GET request
        Value memberIdValue = args.getFieldsMap().get(Constants.MEMBER_ID_KEY);
        String member_id = null;
        if (memberIdValue != null) {
            if (memberIdValue.getKindCase() == Value.KindCase.STRING_VALUE) {
                member_id = memberIdValue.getStringValue();
            } else {

                logger.warn("member_id is not a string: " + memberIdValue.getKindCase());

            }
        } else {

            logger.warn("member_id key not found in the arguments.");
            // Or throw an exception: throw new IllegalArgumentException("Missing member_id
            // argument");

        }

        String formattedUrl = API_URL + "/user/" + member_id;
        logger.info(formattedUrl);
        ResponseEntity<String> response = restTemplate.exchange(formattedUrl, HttpMethod.GET, request,
                String.class);

        // Get response body
        String responseBody = response.getBody();
        logger.info("searchMember() - end" + responseBody);
        return responseBody;
    }

    public String createAppointment(Struct args) {
        logger.info("createAppointment() - start");
        logger.info(args.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Member member = new Member();
        member.setEmail(args.getFieldsMap().get(Constants.EMAIL_KEY).toString());
        member.setFirstName(args.getFieldsMap().get(Constants.FIRSTNAME_KEY).toString());
        member.setLastName(args.getFieldsMap().get(Constants.LASTNAME_KEY).toString());
        member.setMemberId(args.getFieldsMap().get(Constants.MEMBER_ID_KEY).toString());
        // Create request body
        HttpEntity<Member> request = new HttpEntity<>(member, headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Send POST request
        ResponseEntity<String> response = restTemplate.exchange(API_URL + "/confirm", HttpMethod.POST, request,
                String.class);

        // Get response body
        String responseBody = response.getBody();
        logger.info("createAppointment() - end" + responseBody);
        return responseBody;
    }

    @Override
    public String findOpenAppointments(Struct args) {

        logger.info("findOpenAppointments() - start");
        logger.info(args.toString());
        HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // Create request body
        HttpEntity<Member> request = new HttpEntity<>(headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        String formattedUrl = API_URL + "/dayandtime";
        logger.info(formattedUrl);
        ResponseEntity<String> response = restTemplate.exchange(formattedUrl, HttpMethod.GET, request,
                String.class);

        // Get response body
        String responseBody = response.getBody();

        logger.info("findOpenAppointments() - end" + responseBody);
        return responseBody;

    }

}
