package com.flex.osu.api.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.flex.data.FlexData;
import com.flex.osu.api.requests.utility.RequestUtility;
import com.flex.osu.entities.OsuData;
import com.flex.osu.entities.score.Score;
import com.flex.osu.entities.user.User;
import lombok.Getter;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"UnnecessaryLocalVariable", "unchecked", "RedundantSuppression"})
public class FlexRequests {

    private final RequestUtility utility;

    @Getter
    private final Connection connection;

    public FlexRequests(Connection connection) {
        this.connection = connection;
        utility = new RequestUtility(connection);
    }

    /* requests */

    public Optional<User> getUser(String username) throws JsonProcessingException {
        String rawUri = String.format("/users/%s", username);
        rawUri = URLEncoder.encode(rawUri, StandardCharsets.UTF_8);
        String uri = rawUri.replace("+", "%20");
        HttpResponse<String> response = utility.sendGetRequest(uri);
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        User user = utility.getMapper().readValue(response.body(), User.class);
        return Optional.of(user);
    }

    public Optional<Score> getScore(int userId) throws JsonProcessingException {
        String uri = String.format("/users/%d/scores/recent?include_fails=0&mode=osu&limit=1", userId);
        HttpResponse<String> response = utility.sendGetRequest(uri);
        String responseBody = utility.trimBrackets(response.body());
        if (responseBody.isEmpty()) {
            return Optional.empty();
        } // todo: handle error
        Score score = utility.getMapper().readValue(responseBody, Score.class);
        return Optional.of(score);
    }

    public List<Score> getBestScores(int userId) throws JsonProcessingException {
        int amount = FlexData.MAX_TOP_PLAYS;
        String uri = String.format("/users/%d/scores/best?include_fails=0&mode=osu&limit=%d", userId, amount);
        HttpResponse<String> response = utility.sendGetRequest(uri);
        List<Score> scores = utility.getMapper().readValue(response.body(), new TypeReference<List<Score>>() {
        });
        return scores;
    }

    public OsuData isInBest(User user, Score score) throws JsonProcessingException {
        List<Score> scores = getBestScores(user.id);
        // get lowest pp score first
        Collections.reverse(scores);
        OsuData data = new OsuData();

        if (score.pp > scores.get(0).pp) {
            data.setUser(user);
            data.setScore(score);
            data.setScoreIndex(getScoreIndex(scores, score));
            data.setBest(true);
        }
        return data;
    }

    private int getScoreIndex(List<Score> scores, Score recent) {
        int index = scores.size() + 1;
        for (Score score : scores) {
            if (recent.pp > score.pp) {
                index--;
            }
        }
        return index;
    }

    public List<User> getOnlineUsers(List<Integer> userIds) throws JsonProcessingException {
        int maxUsersPerRequest = 20;
        List<User> onlineUsers = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i += maxUsersPerRequest) {
            List<Integer> batchUserIds = userIds.subList(i, Math.min(i + maxUsersPerRequest, userIds.size()));
            String queryString = utility.buildQueryString(batchUserIds);
            String uri = "/users?" + queryString;
            HttpResponse<String> response = utility.sendGetRequest(uri);
            List<User> allUsers = utility.extractUsersFromResponse(response.body());
            List<User> batchOnlineUsers = utility.filterOnlineUsers(allUsers);
            onlineUsers.addAll(batchOnlineUsers);
        }
        return onlineUsers;
    }

}
