package com.flex.discord.commands;

import com.flex.database.storage.ServerStorage;
import com.flex.database.storage.UserServersStorage;
import com.flex.database.storage.UserStorage;
import com.flex.discord.commands.SlashCommand;
import com.flex.osu.api.requests.FlexRequests;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.sql.SQLException;
import java.util.Optional;

public class RemoveUserCommand extends SlashCommand {

    UserServersStorage userServersStorage;
    UserStorage userStorage;
    ServerStorage serverStorage;

    public RemoveUserCommand(JDA api, FlexRequests requests) {
        super(
                api,
                requests,
                "remove",
                "Remove a user from osu!flex"
        );
        userServersStorage = new UserServersStorage(requests.getConnection());
        userStorage = new UserStorage(requests.getConnection());
        serverStorage = new ServerStorage(requests.getConnection());
    }

    @Override
    public void createCommand() {
        command = Commands.slash(name, description)
                .addOption(OptionType.STRING, "user", "The user to remove", true);
    }

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(name)) {
            event.deferReply().queue();
            String username = event.getOption("user").getAsString();
            long serverId = event.getGuild().getIdLong();
            try {
                handleUserRemoval(event, username, serverId);
            } catch (SQLException e) {
                event.getHook().sendMessage("Something went wrong").queue();
            }
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
                event.getHook().sendMessage("User removed").queue();
            } else {
                event.getHook().sendMessage("User already removed or not registered").queue();
            }
        } else {
            event.getHook().sendMessage("User already removed or not registered").queue();
        }
    }
}
