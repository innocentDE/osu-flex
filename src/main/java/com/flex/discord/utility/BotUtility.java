package com.flex.discord.utility;

import com.flex.database.storage.ServerStorage;
import com.flex.discord.commands.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class BotUtility {

    private final JDA api;
    private final ServerStorage serverStorage;
    private final Logger logger = LogManager.getLogger(BotUtility.class);


    public BotUtility(JDA api, ServerStorage serverStorage) {
        this.api = api;
        this.serverStorage = serverStorage;
    }

    public void registerCommands(SlashCommand... commands) {
        for (SlashCommand command : commands) {
            command.registerGlobally();
        }
    }

    public void registerCommands(List<Guild> guilds, SlashCommand... commands) {
        for (Guild guild : guilds) {
            for (SlashCommand command : commands) {
                command.registerForGuild(guild);
            }
        }
    }

    public void deleteAllGlobalCommands() {
        api.retrieveCommands().queue(commands -> {
            for (Command command : commands) {
                api.deleteCommandById(command.getId()).queue();
            }
        });
    }

    public void deleteAllGuildCommands(Guild guild) {
        guild.retrieveCommands().queue(commands -> {
            for (Command command : commands) {
                guild.deleteCommandById(command.getId()).queue();
            }
        });
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
        serverStorage.unregisterServers(leftGuildIds);

        logger.debug("Registered {} new guilds while {} guilds left", newGuildIds.size(), leftGuildIds.size());
    }
}
