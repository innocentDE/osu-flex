package com.flex.discord.utility;

import com.flex.database.storage.ServerStorage;
import com.flex.database.storage.UserServersStorage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BotUtility {

    private final JDA api;
    private final ServerStorage serverStorage;
    private final UserServersStorage userServersStorage;
    private final Logger logger = LogManager.getLogger(BotUtility.class);

    public BotUtility(JDA api, Connection connection) {
        this.api = api;
        this.serverStorage = new ServerStorage(connection);
        this.userServersStorage = new UserServersStorage(connection);
    }

    public void updateGuilds() throws SQLException {
        List<Long> storageGuilds = serverStorage.getServerIds();
        List<Long> onlineGuilds = api.getGuilds().stream().map(Guild::getIdLong).toList();

        List<Long> newGuildIds = onlineGuilds.stream()
                .filter(guildId -> !storageGuilds.contains(guildId))
                .toList();
        List<Long> leftGuildIds = storageGuilds.stream()
                .filter(guildId -> !onlineGuilds.contains(guildId))
                .toList();

        // todo: may cause trouble if discord server is deleted
        List<Guild> newGuilds = newGuildIds.stream()
                .map(api::getGuildById)
                .toList();

        serverStorage.registerServers(newGuilds);

        for(Long leftGuildId : leftGuildIds){
            userServersStorage.removeAllKeys(leftGuildId);
        }

        serverStorage.unregisterServers(leftGuildIds);

        logger.debug("Registered {} new guilds while {} guilds left", newGuildIds.size(), leftGuildIds.size());
    }
}
