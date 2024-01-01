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
    private static final int TOKEN_RENEWAL_LEAD_TIME = 5;
    private final Logger logger = LogManager.getLogger(AccessTokenProvider.class);
    private final CredentialStorage credentialStorage;
    private final HttpClient httpClient;
    private final String clientId;
    private final String clientSecret;

    public AccessTokenProvider(String clientId, String clientSecret, Connection connection, HttpClient httpClient) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.credentialStorage = new CredentialStorage(connection);
        this.httpClient = httpClient;
    }

    public void obtainAccessToken() throws SQLException {
        int delay = 0;
        int period = 1440 - TOKEN_RENEWAL_LEAD_TIME;
        int expiry = -TOKEN_RENEWAL_LEAD_TIME;
        if (credentialStorage.hasAccessToken()) {
            logger.info("Access token found in database.");
            if (!credentialStorage.isAccessTokenExpired()) {
                expiry += credentialStorage.getExpiry();
                delay = period + expiry;
                oneTimeAccessTokenRenewal(expiry);
            }
        } else {
            logger.info("Access token not found in database.");
        }
        scheduleAccessTokenRenewal(delay, period);
    }

    private void scheduleTokenRenewal(int delay, int period, boolean isFixedRate) {
        LocalDateTime renewalTime = LocalDateTime.now().plusMinutes(delay);
        String formattedTime = renewalTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
        String renewalType = isFixedRate ? "one-time " : "";

        logger.info("Scheduled {}access token renewal at {}", renewalType, formattedTime);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        if (isFixedRate) {
            scheduler.schedule(this::requestAndStoreAccessToken, delay, TimeUnit.MINUTES);
        } else {
            scheduler.scheduleAtFixedRate(this::requestAndStoreAccessToken, delay, period, TimeUnit.MINUTES);
        }
    }

    private void scheduleAccessTokenRenewal(int delay, int period) {
        scheduleTokenRenewal(delay, period, false);
    }

    private void oneTimeAccessTokenRenewal(int expiry) {
        scheduleTokenRenewal(expiry, 0, true);
    }

    private void requestAndStoreAccessToken() {
        try {
            HttpResponse<String> response = sendAccessTokenRequest();
            storeAccessTokenInDatabase(parseAccessToken(response));
            logger.info("Access token successfully renewed.");
        } catch (Exception e) {
            logger.error("An error occurred: " + e.getMessage(), e);
        }
    }

    private HttpResponse<String> sendAccessTokenRequest() throws URISyntaxException, IOException, InterruptedException {
        String requestBody = String.format(TOKEN_REQUEST_BODY_FORMAT, clientId, clientSecret);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(TOKEN_REQUEST_URI))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String parseAccessToken(HttpResponse<String> response) throws JSONException {
        JSONObject json = new JSONObject(response.body());
        return json.getString("access_token");
    }

    private void storeAccessTokenInDatabase(String accessToken) throws SQLException {
        credentialStorage.insertAccessToken(accessToken);
    }
}
