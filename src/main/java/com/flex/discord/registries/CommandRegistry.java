package com.flex.discord.registries;

import com.flex.discord.commands.*;
import com.flex.osu.api.requests.FlexRequests;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.List;

@Getter
public class CommandRegistry {

    @Getter(AccessLevel.NONE)
    private final JDA api;
    private final FlexRequests requests;

    private AddUserCommand addUserCommand;
    private RemoveUserCommand removeUserCommand;
    private SetChannelCommand setChannelCommand;
    private SetThresholdCommand setThresholdCommand;
    private HelpCommand helpCommand;

    private final Logger logger = LogManager.getLogger(CommandRegistry.class);

    public CommandRegistry(JDA api, Connection dbConnection) {
        this.api = api;
        this.requests = new FlexRequests(dbConnection);
        initialize();
    }

    private void initialize() {
        addUserCommand = new AddUserCommand(api, requests);
        removeUserCommand = new RemoveUserCommand(api, requests);
        setChannelCommand = new SetChannelCommand(api, requests);
        setThresholdCommand = new SetThresholdCommand(api, requests);
        helpCommand = new HelpCommand(api, requests);
    }

    public void registerCommands() {
        addUserCommand.registerGlobally();
        removeUserCommand.registerGlobally();
        setChannelCommand.registerGlobally();
        setThresholdCommand.registerGlobally();
        helpCommand.registerGlobally();
        // messageAllCommand.registerForGuild(Objects.requireNonNull(api.getGuildById(FlexData.OSU_FLEX_DISCORD_ID)));
        logger.info("Registered all global slash commands");
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
            commands.forEach(command ->
                    api.deleteCommandById(command.getId()).queue()
            );
        });
        logger.info("Deleted all global slash commands");
    }

    private void deleteAllGuildCommands(Guild guild) {
        guild.retrieveCommands().queue(commands ->
                commands.forEach(command ->
                        guild.deleteCommandById(command.getId()).queue()
                )
        );
    }

    public void deleteAllGuildCommands() {
        api.getGuilds().forEach(this::deleteAllGuildCommands);
        logger.info("Deleted all guild slash commands");
    }

    public void deleteAllGuildCommands(List<Guild> guilds) {
        guilds.forEach(this::deleteAllGuildCommands);
        logger.info("Deleted all guild slash commands");
    }
}
