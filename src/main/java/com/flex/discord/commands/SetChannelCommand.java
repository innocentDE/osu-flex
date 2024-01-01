package com.flex.discord.commands;

import com.flex.database.storage.ServerStorage;
import com.flex.discord.commands.SlashCommand;
import com.flex.osu.api.requests.FlexRequests;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.sql.SQLException;

public class SetChannelCommand extends SlashCommand {

    private final String optionName = "channel";
    private final String optionDescription = "The channel to send the top scores to";
    private final ServerStorage serverStorage;

    public SetChannelCommand(JDA api, FlexRequests requests) {
        super(
                api,
                requests,
                "set",
                "sets the channel where osu!flex sends the top plays"
        );
        serverStorage = new ServerStorage(requests.getConnection());
    }

    protected void createCommand() {
        command = Commands.slash(name, description)
                .addOption(OptionType.CHANNEL, optionName, optionDescription, true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(name)) {
            event.deferReply().queue();
            try {
                handleSettingChannel(event);
                event.getHook().sendMessage("Channel set").queue();
            } catch (SQLException e) {
                event.getHook().sendMessage("Failed to set channel").queue();
            }
        }
    }

    private void handleSettingChannel(SlashCommandInteractionEvent event) throws SQLException {
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getOption(optionName).getAsChannel().getIdLong();
        serverStorage.setChannel(guildId, channelId);
    }
}
