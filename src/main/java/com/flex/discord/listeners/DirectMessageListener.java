package com.flex.discord.listeners;

import com.flex.data.FlexData;
import com.flex.discord.embeds.HelpEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DirectMessageListener extends ListenerAdapter {

    private static final String GREETING = "Hello fellow developer, with what can I help you with today?";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            if (FlexData.DEVELOPER_DISCORD_IDS.contains(Long.parseLong(event.getAuthor().getId()))) {

            } else {
                event.getChannel().sendMessageEmbeds(new HelpEmbed().getEmbed()).queue();
            }
        }
    }
}
