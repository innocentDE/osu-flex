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
    private final Logger logger = LogManager.getLogger(RequestUtility.class);
    private final CredentialStorage credentialStorage;

    @Getter
    private final ObjectMapper mapper = new ObjectMapper();

    public RequestUtility(Connection connection) {
        credentialStorage = new CredentialStorage(connection);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void updateAccessToken() throws SQLException {

        accessToken = credentialStorage.getAccessToken();
    }

    public HttpResponse<String> sendGetRequest(String endpoint) {
        int maxAttempts = 5;
        int waitTime = 1000;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                URI uri = new URI(API_BASE_URL + endpoint);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();

                HttpResponse<String> response = HttpClient
                        .newHttpClient()
                        .send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == HttpStatus.SC_OK) {
                    return response;
                } else if (response.statusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    // todo: handle UNAUTHORIZED on first polling attempt
                    logger.warn("Request failed with status code " + response.statusCode());
                    updateAccessToken();
                } else if(response.statusCode() == HttpStatus.SC_NOT_FOUND) {
                    return response;
                } else {
                    logger.warn("Request failed with status code " + response.statusCode());
                }
            } catch (URISyntaxException | InterruptedException | IOException | SQLException e) {
                logger.debug("Attempt " + (attempt + 1) + " failed: " + e.getMessage());
                if (attempt == maxAttempts - 1) {
                    break;
                }
                try {
                    Thread.sleep(waitTime);
                    waitTime *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrupted: " + ie.getMessage());
                }
            }
        }
        // todo: handle this exception
        throw new IllegalStateException("Failed to send request after " + maxAttempts + " attempts");
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
