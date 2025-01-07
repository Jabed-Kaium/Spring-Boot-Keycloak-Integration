package org.example.keycloakspringboot.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.keycloakspringboot.dto.UserDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KeycloakService {

    private static final String KEYCLOAK_BASE_URL = "http://localhost:8180";
    private static final String REALM = "Keycloak_SpringBoot";
    private static final String CLIENT_ID = "springboot-openid-client-app";

    public void registerUser(UserDto userDto) throws Exception {
        String accessToken = getAdminAccessToken();
        String url = KEYCLOAK_BASE_URL + "/admin/realms/" + REALM + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String userJson = createUserJson(userDto);

        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if(response.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Error registering user: " + response.getBody());
        }
    }

    private String getAdminAccessToken() throws Exception {
        String url = KEYCLOAK_BASE_URL + "auth/realms/" + REALM + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + CLIENT_ID +
                "&client_secret=hNzeirTrc1L4XXUU7h6su7woTmOKS31a" +
                "&grant_type=client_credentials";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if(response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error getting admin access token: " + response.getBody());
        }

        Map<String, String> tokenResponse = new ObjectMapper().readValue(response.getBody(), Map.class);
        return tokenResponse.get("access_token");
    }

    private String createUserJson(UserDto userDto) {
        return "{\n" +
                "    \"username\": \"" + userDto.getUsername() + "\",\n" +
                "    \"email\": \"" + userDto.getEmail() + "\",\n" +
                "    \"firstName\": \"" + userDto.getFirstName() + "\",\n" +
                "    \"lastName\": \"" + userDto.getLastName() + "\",\n" +
                "    \"enabled\": true,\n" +
                "    \"credentials\": [\n" +
                "        {\n" +
                "            \"type\": \"password\",\n" +
                "            \"value\": \"" + userDto.getPassword() + "\"\n" +
                "      \"temporary\": false\n" +
                "        }\n" +
                "    ]\n" +
                "  \"realmRoles\": [\n" +
                "    \"USER\"\n" +
                "  ]\n" +
                "}";
    }
}
