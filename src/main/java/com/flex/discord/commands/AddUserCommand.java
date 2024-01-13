package com.flex.discord.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flex.data.FlexData;
import com.flex.database.storage.UserServersStorage;
import com.flex.database.storage.UserStorage;
import com.flex.osu.api.requests.FlexRequests;
import com.flex.osu.entities.user.User;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.Optional;

public class AddUserCommand extends SlashCommand {

    private final  UserStorage userStorage;
    private final UserServersStorage userServersStorage;
    private final Logger logger = LogManager.getLogger(AddUserCommand.class);
    private final String optionName = "user";
    private final String optionDescription = "The user to add";

    public AddUserCommand(JDA api, FlexRequests requests) {
        super(
                api,
                requests,
                "add",
                "Add a user to the server"
        );
        userStorage = new UserStorage(requests.getConnection());
        userServersStorage = new UserServersStorage(requests.getConnection());
    }

    @Override
    protected void createCommand() {
        command = Commands.slash(name, description)
                .addOption(OptionType.STRING, optionName, optionDescription, true);
    }

    public void execute(SlashCommandInteractionEvent event) {
        try {
            handleUserRegistration(event);
        } catch (SQLException | JsonProcessingException e) {
            super.sendMessage(event, FlexData.ERROR_MESSAGE);
        }
    }

    private void handleUserRegistration(SlashCommandInteractionEvent event) throws SQLException, JsonProcessingException {
        long guildId = event.getGuild().getIdLong();
        String username = event.getOption(optionName).getAsString();
        if (userStorage.isUserRegistered(username, guildId)) {
            super.sendMessage(event, "User " + username + " is already registered on osu!flex");
        } else {
            Optional<User> user = requests.getUser(username);
            String discordUsername = event.getUser().getName();
            String guildName = event.getGuild().getName();
            if (user.isPresent()) {
                if(!userStorage.getUserId(username).isPresent()){
                    userStorage.registerUser(user.get());
                    logger.debug(String.format("%s on %s registered %s", discordUsername, guildName, username));
                }
                logger.debug(String.format("%s on %s added %s", discordUsername, guildName, username));
                userServersStorage.addKeys(user.get().id, guildId);
                super.sendMessage(event, username + " successfully added to osu!flex");
            } else {
                super.sendMessage(event, username + " does not exist");
            }
        }

    }

}
