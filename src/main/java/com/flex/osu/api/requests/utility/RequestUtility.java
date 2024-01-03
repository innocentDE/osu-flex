package com.flex.osu.api.requests.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flex.database.storage.CredentialStorage;
import com.flex.osu.entities.user.User;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RequestUtility {

    private String accessToken;
    private static final String API_BASE_URL = "https://osu.ppy.sh/api/v2";
    private static final int MAX_ATTEMPTS = 5;
    private static int SLEEP_TIME = 1000;
    private final Logger logger = LogManager.getLogger(RequestUtility.class);
    private final CredentialStorage credentialStorage;

    @Getter
    private final ObjectMapper mapper = new ObjectMapper();

    public RequestUtility(Connection connection) {
        credentialStorage = new CredentialStorage(connection);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public HttpResponse<String> sendGetRequest(String endpoint) throws IllegalStateException {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                sendRequest(endpoint);
            } catch (URISyntaxException | InterruptedException | IOException e) {
                logger.debug("Attempt " + (attempt + 1) + " failed: " + e.getMessage());
                if (hasMaxAttemptsReached(attempt))
                    break;
            }
        }
        throw new IllegalStateException("Unexpected state: failed to send request");
    }

    private void sendRequest(String endpoint) throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URI(API_BASE_URL + endpoint);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() == HttpStatus.SC_UNAUTHORIZED){
            try{
                accessToken = credentialStorage.getAccessToken();
            } catch (SQLException sqle){
                throw new IllegalStateException("Unexpected state: failed to send request" + sqle.getMessage());
            }
        }
    }

    private boolean hasMaxAttemptsReached(int attempt) {
        if (attempt == MAX_ATTEMPTS - 1) {
            return true;
        }
        try {
            Thread.sleep(SLEEP_TIME);
            SLEEP_TIME *= 2;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    public String trimBrackets(String responseBody) {
        return responseBody.substring(1, responseBody.length() - 1);
    }

    public String buildQueryString(List<Integer> userIds) {
        return userIds.stream()
                .map(id -> "ids%5B%5D=" + id)
                .collect(Collectors.joining("&"));
    }

    public List<User> extractUsersFromResponse(String responseBody) throws JsonProcessingException {
        JsonNode rootNode = mapper.readTree(responseBody);
        return mapper.convertValue(rootNode.get("users"), new TypeReference<>() {});
    }

    public List<User> filterOnlineUsers(List<User> allUsers) {
        if (allUsers == null) {
            return Collections.emptyList();
        }
        return allUsers.stream()
                .filter(User::is_online)
                .collect(Collectors.toList());
    }
}
