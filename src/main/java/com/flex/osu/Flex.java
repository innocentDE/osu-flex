package com.flex.osu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flex.data.FlexData;
import com.flex.database.storage.UserServersStorage;
import com.flex.database.storage.UserStorage;
import com.flex.discord.embeds.ScoreEmbed;
import com.flex.osu.api.requests.FlexRequests;
import com.flex.osu.entities.score.Score;
import com.flex.osu.entities.user.User;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Flex {

    private final Logger logger = LogManager.getLogger(Flex.class);
    private final JDA api;
    private final UserStorage userStorage;
    private final UserServersStorage userServersStorage;

    private final FlexRequests requests;

    public Flex(JDA api, Connection connection){
        this.api = api;
        userStorage = new UserStorage(connection);
        requests = new FlexRequests(connection);
        userServersStorage = new UserServersStorage(connection);
    }

    public void start() throws InterruptedException {
        try {
            List<Integer> userIds = userStorage.getUserIds();

            List<User> users = requests.getOnlineUsers(userIds);
            logger.debug("Currently {} out of {} users are online", users.size(), userIds.size());

            for(User user : users){
                Optional<Score> recentScore = requests.getScore(user.id);

                if(recentScore.isEmpty()) continue;
                if(userStorage.isBestId(user.id, recentScore.get().id)) continue;

                userStorage.insertBestId(user.id, recentScore.get().id);
                Optional<Score> score = requests.isInBest(
                        user.id,
                        recentScore.get(),
                        100);

                sendEmbedIfInBest(user, score);
            }
        } catch (JsonProcessingException | SQLException e) {
            logger.error(e);
            Thread.sleep(FlexData.ERROR_SLEEP);
        }
    }

    private void sendEmbedIfInBest(User user, Optional<Score> score) throws SQLException {
        if(score.isPresent()){
            ScoreEmbed embed = new ScoreEmbed(score.get());
            Map<String, String> servers = userServersStorage.getServersByUser(score.get().user_id);

            if(servers.isEmpty()){
                // todo: remove user from table
                logger.debug("No servers found for user " + user.username);
                return;
            }

            if (hasBotPermission(servers))
                return;

            for(Map.Entry<String, String> server : servers.entrySet()){
                api.getGuildById((server.getKey()))
                        .getTextChannelById(server.getValue())
                        .sendMessageEmbeds(embed.getEmbed())
                        .queue();
                logger.debug("Sent embed for user " + user.username + " to server " + server.getKey());
            }
        }
    }

    private boolean hasBotPermission(Map<String, String> servers) {
        for(Map.Entry<String, String> server : servers.entrySet()){
            if(!api.getGuildById(server.getKey())
                    .getSelfMember()
                    .hasPermission(api.getGuildById(server.getKey())
                            .getTextChannelById(server.getValue()),
                            Permission.MESSAGE_SEND)) {
                logger.debug("Bot has no permission to send messages to channel " + server.getValue() + " in server " + server.getKey());

                api.getGuildById(server.getKey())
                        .retrieveOwner()
                        .complete()
                        .getUser()
                        .openPrivateChannel()
                        .queue((channel) -> channel.sendMessage(
                                "Bot has no permission to send messages to channel " + server.getValue() + " in server " + server.getKey()).queue()
                        );
                return true;
            }
        }
        return false;
    }
}
