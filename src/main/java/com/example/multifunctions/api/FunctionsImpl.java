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
        member.setEmail(getAsString(args, Constants.EMAIL_KEY));
        member.setFirstName(getAsString(args, Constants.FIRSTNAME_KEY));
        member.setLastName(getAsString(args, Constants.LASTNAME_KEY));
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

        String member_id = getAsString(args, Constants.MEMBER_ID_KEY);

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
        member.setEmail(getAsString(args, Constants.EMAIL_KEY));
        member.setFirstName(getAsString(args, Constants.FIRSTNAME_KEY));
        member.setLastName(getAsString(args, Constants.LASTNAME_KEY));
        member.setMemberId(getAsString(args, Constants.MEMBER_ID_KEY));
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

    /**
     * 
     * @param args
     * @param key
     * @return
     */
    private String getAsString(Struct args, String key) {
        Value value = args.getFieldsMap().get(key);
        if (value != null) {
            if (value.getKindCase() == Value.KindCase.STRING_VALUE) {
                return value.getStringValue();
            } else {
                logger.warn(key + " is not a string: " + value.getKindCase());
                return "no-value";
            }
        } else {
            logger.warn(key + " not found in the arguments.");
            return "no-value";
        }
    }

}
