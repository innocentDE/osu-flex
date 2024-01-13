package com.flex.osu.api.requests;

import com.flex.database.storage.CredentialStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AccessTokenProvider {

    private static final String TOKEN_REQUEST_URI = "https://osu.ppy.sh/oauth/token";
    private static final String TOKEN_REQUEST_BODY_FORMAT = """
            {
                "grant_type": "client_credentials",
                "client_id": "%s",
                "client_secret": "%s",
                "scope": "public"
            }
            """;

    private static final Logger logger = LogManager.getLogger(AccessTokenProvider.class);
    private static CredentialStorage credentialStorage;
    private static HttpClient httpClient;
    private static String clientId;
    private static String clientSecret;

    public AccessTokenProvider(String clientId, String clientSecret, Connection connection, HttpClient httpClient) {
        AccessTokenProvider.clientId = clientId;
        AccessTokenProvider.clientSecret = clientSecret;
        AccessTokenProvider.credentialStorage = new CredentialStorage(connection);
        AccessTokenProvider.httpClient = httpClient;
    }

    public void obtainAccessToken() throws SQLException {
        if (credentialStorage.hasAccessToken()) {
            logger.info("Access token found in database.");
            if (!credentialStorage.isAccessTokenExpired())
                logger.info("Access token is still valid.");
            else {
                logger.info("Access token is expired.");
                requestAndStoreAccessToken();
            }
        } else {
            logger.info("Access token not found in database.");
            requestAndStoreAccessToken();
        }
    }

    public static void requestAndStoreAccessToken() {
        try {
            HttpResponse<String> response = sendAccessTokenRequest();
            storeAccessTokenInDatabase(parseAccessToken(response));
            logger.info("Access token successfully renewed.");
        } catch (Exception e) {
            logger.error("An error occurred: " + e.getMessage(), e);
        }
    }

    private static HttpResponse<String> sendAccessTokenRequest() throws URISyntaxException, IOException, InterruptedException {
        String requestBody = String.format(TOKEN_REQUEST_BODY_FORMAT, clientId, clientSecret);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(TOKEN_REQUEST_URI))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String parseAccessToken(HttpResponse<String> response) throws JSONException {
        JSONObject json = new JSONObject(response.body());
        return json.getString("access_token");
    }

    private static void storeAccessTokenInDatabase(String accessToken) throws SQLException {
        credentialStorage.insertAccessToken(accessToken);
    }
}
