package com.flex.discord;

import com.flex.database.storage.ServerStorage;
import com.flex.discord.commands.HelpCommand;
import com.flex.discord.commands.SetChannelCommand;
import com.flex.discord.commands.AddUserCommand;
import com.flex.discord.commands.RemoveUserCommand;
import com.flex.discord.listeners.GuildListener;
import com.flex.discord.utility.BotUtility;
import com.flex.osu.api.requests.FlexRequests;
import com.flex.osu.api.requests.utility.RequestUtility;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class Bot {

    @Getter
    private JDA api;
    private final FlexRequests requests;
    private final ServerStorage serverStorage;
    private final Connection connection;
    private final Logger logger = LogManager.getLogger(Bot.class);

    public Bot(String token, Connection connection) {
        this.connection = connection;
        requests = new FlexRequests(connection);
        serverStorage = new ServerStorage(connection);

        initialize(token);
    }

    private void initialize(String token) {
        try {
            api = JDABuilder.createDefault(token).build().awaitReady();
            BotUtility utility = new BotUtility(api, serverStorage);
            utility.registerCommands(
                    new SetChannelCommand(api, requests),
                    new AddUserCommand(api, requests),
                    new RemoveUserCommand(api, requests),
                    new HelpCommand(api, requests)
            );
            utility.updateGuilds();
            api.addEventListener(new GuildListener(connection));

            logger.info("Bot started");
        } catch (InterruptedException | SQLException e) {
            logger.fatal("Failed to start bot");
            System.exit(1);
        }
    }
}
