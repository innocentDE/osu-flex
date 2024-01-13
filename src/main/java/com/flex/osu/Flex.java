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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.channels.Channel;
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
            Map<String, String> servers = userServersStorage.getServersByUser(data.getUser().id);
            if(servers.isEmpty()){
                // todo: remove user from table
                logger.debug("No servers found for user " + data.getUser().username);
                return;
            }

            for(Map.Entry<String, String> server : servers.entrySet()){
                ScoreEmbed embed = new ScoreEmbed(data);
                int threshold = userServersStorage.getThreshold(data.getUser().id, Long.parseLong(server.getKey()));

                if (!meetsConditions(data, server, threshold))
                    continue;

                sendEmbed(data, server, embed);
            }
        }
    }

    private boolean meetsConditions(OsuData data, Map.Entry<String, String> server, int threshold) {

        logger.debug("Checking conditions for user " + data.getUser().username + " in server " + server.getKey());

        Guild guild = api.getGuildById(server.getKey());
        TextChannel textChannel;

        if(guild == null){
            logger.debug("Server " + server.getKey() +
                    " not found. Maybe the bot is not in the server anymore or the server was deleted");
            return false;
        }

        textChannel = guild.getTextChannelById(server.getValue());

        if(textChannel == null){
            logger.debug("Channel " + server.getValue() +
                    " not found. Maybe the channel was deleted?");
            sendNoPermissionNoticeToOwner(server.getKey(), server.getValue());
            return false;
        }

        String guildName = guild.getName();
        String channelName = textChannel.getName();

        if(data.getScoreIndex() > threshold){
            logger.debug(String.format("Score didn't reach threshold %d for user %s in server %s",
                    threshold, data.getUser().username, guildName));
            return false;
        }

        if(!hasBotPermission(server)) {
            logger.debug(String.format("Bot has no permission to send messages to channel %s in server %s",
                    channelName, guildName));
            return false;
        }

        return true;
    }

    private void sendEmbed(OsuData data, Map.Entry<String, String> server, ScoreEmbed embed) {
        api.getGuildById((server.getKey()))
                .getTextChannelById(server.getValue())
                .sendMessageEmbeds(embed.getEmbed())
                .queue();
        logger.debug("Sent embed for user " + data.getUser().username + " to server " + server.getKey());
    }


    private boolean hasBotPermission(Map.Entry<String, String> server){
        // todo: check if bot has permission to send messages to channel
        return true;
    }

    private void sendNoPermissionNoticeToOwner(String serverId, String channelId){
        Guild guild = api.getGuildById(serverId);
        TextChannel textChannel = guild.getTextChannelById(channelId);

        if(guild == null){
            logger.debug("Server " + serverId +
                    " not found. Maybe the bot is not in the server anymore or the server was deleted");
        }

        if(textChannel == null){
            logger.debug("Channel " + channelId +
                    " not found. Maybe the channel was deleted?");

            api.getGuildById(serverId)
                    .retrieveOwner().
                    complete()
                    .getUser()
                    .openPrivateChannel()
                    .queue(channel -> channel.sendMessage("The channel " + channelId + " in server " + guild.getName() +
                            " does not exist. Please set a new channel with /set").queue());
        }

        api.getGuildById(serverId)
                .retrieveOwner().
                complete()
                .getUser()
                .openPrivateChannel()
                .queue(channel -> channel.sendMessage("I don't have permission to send messages to channel " +
                        textChannel.getName() + " in server " + guild.getName()).queue());
    }
}
