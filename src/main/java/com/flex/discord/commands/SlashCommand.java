package com.flex.discord.commands;

import com.flex.osu.api.requests.FlexRequests;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class SlashCommand extends ListenerAdapter {

    protected JDA api;
    protected String name;
    protected String description;
    protected SlashCommandData command;
    protected FlexRequests requests;

    public SlashCommand(JDA api) {
        this.api = api;
        createCommand();
    }

    public SlashCommand(JDA api, String name, String description) {
        this.api = api;
        this.name = name;
        this.description = description;
        createCommand();
    }

    public SlashCommand(JDA api, FlexRequests requests, String name, String description) {
        this.api = api;
        this.requests = requests;
        this.name = name;
        this.description = description;
        createCommand();
    }

    public void registerForGuild(Guild guild) {
        guild.upsertCommand(command).queue();
        api.addEventListener(this);
    }

    public void registerGlobally() {
        api.upsertCommand(command).queue();
        api.addEventListener(this);
    }

    protected void sendMessage(SlashCommandInteractionEvent event, String message) {
        event.getHook().editOriginal(message).queue();
    }

    protected void sendEmbed(SlashCommandInteractionEvent event, MessageEmbed embed) {
        event.getHook().editOriginalEmbeds(embed).queue();
    }

    protected abstract void createCommand();
}
