package com.flex.osu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flex.data.FlexData;
import com.flex.database.storage.UserServersStorage;
import com.flex.database.storage.UserStorage;
import com.flex.discord.embeds.ScoreEmbed;
import com.flex.osu.api.requests.FlexRequests;
import com.flex.osu.entities.OsuData;
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

                if(recentScore.isEmpty()) {
                    logger.debug(String.format("User %s has no recent score", user.username));
                    continue;
                }
                if(userStorage.isBestId(user.id, recentScore.get().id)){
                    continue;
                }

                userStorage.insertBestId(user.id, recentScore.get().id);
                OsuData data = requests.isInBest(user, recentScore.get());
                sendEmbedIfInBest(data);
            }
        } catch (JsonProcessingException | SQLException e) {
            logger.error(e);
            Thread.sleep(FlexData.ERROR_SLEEP);
        }
    }

    private void sendEmbedIfInBest(OsuData data) throws SQLException {
        if(data.isBest()){
            ScoreEmbed embed = new ScoreEmbed(data);
            Map<String, String> servers = userServersStorage.getServersByUser(data.getUser().id);


            if(servers.isEmpty()){
                // todo: remove user from table
                logger.debug("No servers found for user " + data.getUser().username);
                return;
            }

            for(Map.Entry<String, String> server : servers.entrySet()){

                int threshold = userServersStorage.getThreshold(data.getUser().id, Long.parseLong(server.getKey()));

                if(data.getScoreIndex() > threshold){
                    logger.debug(String.format("Score didn't reach threshold %d for user %s", threshold, data.getUser().username));
                    continue;
                }
                if(hasBotPermission(server.getKey(), server.getValue())) {
                    logger.debug(String.format("Bot has no permission to send messages to channel %s in server %s",
                            server.getValue(), server.getKey()));
                    continue;
                }

                api.getGuildById((server.getKey()))
                        .getTextChannelById(server.getValue())
                        .sendMessageEmbeds(embed.getEmbed())
                        .queue();
                logger.debug("Sent embed for user " + data.getUser().username + " to server " + server.getKey());
            }
        }
    }

    // todo: parameter Guild instead of 2, split logic from method (check permission in another method)
    private boolean hasBotPermission(String serverId, String channelId){

            if(!api.getGuildById(serverId)
                    .getSelfMember()
                    .hasPermission(api.getGuildById(serverId)
                            .getTextChannelById(channelId),
                            Permission.MESSAGE_SEND)) {
                logger.debug("Bot has no permission to send messages to channel " + channelId + " in server " + serverId);

                api.getGuildById(serverId)
                        .retrieveOwner()
                        .complete()
                        .getUser()
                        .openPrivateChannel()
                        .queue((channel) -> channel.sendMessage(
                                "Bot has no permission to send messages to channel " + serverId + " in server " + serverId).queue()
                        );
                return true;
        }
        return false;
    }
}
