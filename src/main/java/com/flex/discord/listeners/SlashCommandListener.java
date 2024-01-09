package com.flex.discord.listeners;

import com.flex.data.FlexData;
import com.flex.discord.registries.CommandRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.flex.discord.commands.Commands;

import java.util.Objects;

public class SlashCommandListener extends ListenerAdapter {

    private final JDA api;
    private final CommandRegistry registry;

    public SlashCommandListener(JDA api, CommandRegistry registry){
        this.api = api;
        this.registry = registry;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){

        event.deferReply().setEphemeral(true).queue();

        if(event.isFromGuild()){
            Commands command = Commands.getCommand(event.getName());
            switch (Objects.requireNonNull(command)){
                case ADD:
                    registry.getAddUserCommand().execute(event);
                    break;
                case REMOVE:
                    registry.getRemoveUserCommand().execute(event);
                    break;
                case SET:
                    registry.getSetChannelCommand().execute(event);
                    break;
                case THRESHOLD:
                    registry.getSetThresholdCommand().execute(event);
                    break;
                case HELP:
                    registry.getHelpCommand().execute(event);
                    break;
                default:
                    event.getHook().editOriginal(FlexData.ERROR_MESSAGE).queue();
            }
        } else {
            event.getHook().editOriginal("This command can only be used on a server").queue();
        }
    }
}
