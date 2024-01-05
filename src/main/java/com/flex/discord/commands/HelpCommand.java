package com.flex.discord.commands;

import com.flex.discord.embeds.HelpEmbed;
import com.flex.osu.api.requests.FlexRequests;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class HelpCommand extends SlashCommand {
    public HelpCommand(JDA api, FlexRequests requests) {
        super(
                api,
                requests,
                "help",
                "Lists all of the commands");

    }

    @Override
    protected void createCommand() {
        command = Commands.slash(name, description);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        HelpEmbed embed = new HelpEmbed();
        if (event.getName().equals(name)) {
            event.replyEmbeds(embed.getEmbed()).queue();
        }
    }
}
