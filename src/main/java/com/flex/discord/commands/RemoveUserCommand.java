package com.flex.discord.commands;

import com.flex.data.FlexData;
import com.flex.database.storage.UserServersStorage;
import com.flex.database.storage.UserStorage;
import com.flex.osu.api.requests.FlexRequests;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Optional;

public class RemoveUserCommand extends SlashCommand {

    private final UserServersStorage userServersStorage;
    private final UserStorage userStorage;
    private final Logger logger = LogManager.getLogger(RemoveUserCommand.class);

    public RemoveUserCommand(JDA api, FlexRequests requests) {
        super(
                api,
                requests,
                "remove",
                "Remove a user from osu!flex"
        );
        userServersStorage = new UserServersStorage(requests.getConnection());
        userStorage = new UserStorage(requests.getConnection());
    }

    @Override
    public void createCommand() {
        command = Commands.slash(name, description)
                .addOption(OptionType.STRING, "user", "The user to remove", true);
    }

    public void execute(SlashCommandInteractionEvent event) {
        String username = event.getOption("user").getAsString();
        long serverId = event.getGuild().getIdLong();
        try {
            handleUserRemoval(event, username, serverId);
        } catch (SQLException e) {
            super.sendMessage(event, FlexData.ERROR_MESSAGE);
        }
    }

    private void handleUserRemoval(SlashCommandInteractionEvent event, String username, long serverId) throws SQLException {
        if (userStorage.isUserRegistered(username, serverId)) {
            Optional<Integer> userId = userStorage.getUserId(username);
            if (userId.isPresent()) {
                userServersStorage.deleteKeys(userId.get(), serverId);
                // todo: remove user if not on any other servers (broken)
                // if (!userServersStorage.isUserOnMultipleServers(userId.get())) {
                //     userStorage.removeUser(userId.get());
                // }
                logger.debug(String.format("%s removed from server %s by %s", username, serverId, event.getUser().getAsTag()));
                super.sendMessage(event, username + " successfully removed from osu!flex");
            } else {
                super.sendMessage(event, username + " already removed or not registered");
            }
        } else {
            super.sendMessage(event, username + " already removed or not registered");
        }
    }
}
