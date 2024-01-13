package com.flex.discord;

import com.flex.discord.listeners.GuildListener;
import com.flex.discord.listeners.SlashCommandListener;
import com.flex.discord.registries.CommandRegistry;
import com.flex.discord.utility.BotUtility;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class Bot {

    @Getter
    private JDA api;
    private final Connection connection;
    private CommandRegistry registry;
    private BotUtility utility;

    private final Logger logger = LogManager.getLogger(Bot.class);

    public Bot(String token, Connection connection) {
        this.connection = connection;
        initialize(token);
    }

    private void initialize(String token) {
        try {
            api = JDABuilder.createDefault(token).build().awaitReady();
            registry = new CommandRegistry(api, connection);
            utility = new BotUtility(api, connection);
            utility.updateGuilds();
            registry.registerCommands();
            addListener();
            logger.info("Bot started");
        } catch (InterruptedException | SQLException | InvalidTokenException | IllegalArgumentException e) {
            logger.error(e);
            System.exit(1);
        }

    }

    private void addListener() {
        api.addEventListener(new GuildListener(connection));
        api.addEventListener(new SlashCommandListener(api, registry));
        logger.debug("Listeners added");
    }
}
