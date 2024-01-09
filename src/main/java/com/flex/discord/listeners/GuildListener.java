package com.flex.discord.listeners;

import com.flex.database.storage.ServerStorage;
import com.flex.database.storage.UserServersStorage;
import com.flex.database.storage.UserStorage;
import com.flex.discord.embeds.JoinEmbed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class GuildListener extends ListenerAdapter {

    private final ServerStorage serverStorage;
    // todo: remove users from users table which don't relate to any server
    private final UserStorage userStorage;
    private final UserServersStorage userServersStorage;
    private final Logger logger = LogManager.getLogger(GuildListener.class);

    public GuildListener(Connection connection) {
        serverStorage = new ServerStorage(connection);
        userStorage = new UserStorage(connection);
        userServersStorage = new UserServersStorage(connection);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        logger.info("Left server: " + event.getGuild().getIdLong());
        try {
            unregister(event.getGuild());
            logger.info("Unregistered server: " + event.getGuild().getIdLong());
        } catch (SQLException e) {
            logger.error("Failed to unregister server: " + event.getGuild().getIdLong());
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        logger.info("Joined server: " + event.getGuild().getIdLong());
        try {
            register(event.getGuild());
            sendSetupEmbed(event);
            logger.info("Registered server: " + event.getGuild().getIdLong());
        } catch (SQLException e) {
            logger.error("Failed to register server: " + event.getGuild().getIdLong());
            sendFailedMessage(event);
        }
    }

    public void sendSetupEmbed(GuildJoinEvent event) {
        event.getGuild()
                .getDefaultChannel()
                .asTextChannel()
                .sendMessageEmbeds(new JoinEmbed().getEmbed())
                .queue();
    }

    public void sendFailedMessage(GuildJoinEvent event) {
        event.getGuild()
                .getDefaultChannel()
                .asTextChannel()
                .sendMessage("Something went wrong")
                .queue();
    }

    public void register(Guild guild) throws SQLException {
        serverStorage.registerServer(
                guild.getIdLong(),
                guild.getDefaultChannel().getIdLong()
        );
    }

    public void unregister(Guild guild) throws SQLException {
        userServersStorage.removeAllKeys(guild.getIdLong());
        serverStorage.unregisterServer(guild.getIdLong());
    }
}