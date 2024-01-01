package com.flex.discord.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(name)) {
            event.deferReply().queue();
            try {
                handleUserRegistration(event);
            } catch (SQLException | JsonProcessingException e) {
                event.getHook().sendMessage("Something went wrong").queue();
            }
        }
    }

    private void handleUserRegistration(SlashCommandInteractionEvent event) throws SQLException, JsonProcessingException {
        long guildId = event.getGuild().getIdLong();
        String username = event.getOption(optionName).getAsString();
        if (userStorage.isUserRegistered(username, guildId)) {
            event.getHook().sendMessage("User is already registered").queue();
        } else {
            Optional<User> user = requests.getUser(username);
            if (user.isPresent()) {
                if(!userStorage.getUserId(username).isPresent()){
                    userStorage.registerUser(user.get());
                    logger.debug(String.format("%s on %s registered %s", event.getUser().getAsTag(), event.getGuild().getName(), username));
                }
                logger.debug(String.format("%s on %s added %s", event.getUser().getAsTag(), event.getGuild().getName(), username));
                userServersStorage.addKeys(user.get().id, guildId);
                event.getHook().sendMessage("User registered").queue();
            } else {
                event.getHook().sendMessage("User does not exist").queue();
            }
        }

    }

}
