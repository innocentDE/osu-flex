package com.flex.discord.utility;

import com.flex.discord.commands.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;

public class BotUtility {

    JDA api;

    public BotUtility(JDA api) {
        this.api = api;
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
}
