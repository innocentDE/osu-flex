package com.flex.discord.listeners;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@AllArgsConstructor
public class ButtonListner extends ListenerAdapter {
    private final JDA api;
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("rendering")) {
            event.reply("It works!").setEphemeral(true).queue();
        }
    }
}