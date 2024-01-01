package com.flex.discord;

import com.flex.discord.commands.SetChannelCommand;
import com.flex.discord.commands.AddUserCommand;
import com.flex.discord.commands.RemoveUserCommand;
import com.flex.discord.listeners.GuildListener;
import com.flex.discord.utility.BotUtility;
import com.flex.osu.api.requests.FlexRequests;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;

public class Bot {

    @Getter
    private JDA api;
    private BotUtility utility;
    private final FlexRequests requests;
    private final Connection connection;
    private final Logger logger = LogManager.getLogger(Bot.class);

    public Bot(String token, Connection connection) {
        this.connection = connection;
        requests = new FlexRequests(connection);
        initialize(token);
    }

    private void initialize(String token) {
        try {
            api = JDABuilder.createDefault(token).build().awaitReady();
            utility = new BotUtility(api);
            utility.registerCommands(
                    new SetChannelCommand(api, requests),
                    new AddUserCommand(api, requests),
                    new RemoveUserCommand(api, requests)
            );
            GuildListener guildListener = new GuildListener(connection);
            api.addEventListener(guildListener);
            logger.info("Bot started");
        } catch (InterruptedException e) {
            logger.fatal("Failed to start bot");
            System.exit(1);
        }
    }
}
