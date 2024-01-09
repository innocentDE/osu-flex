package com.flex.discord.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import static com.flex.data.FlexData.DEVELOPER_DISCORD_IDS;

public class MessageAllCommand extends SlashCommand {

    public MessageAllCommand(JDA api) {
        super(
                api,
                "msgall",
                "Send a message to all servers"
        );
    }

    private final String optionName = "message";
    private final String optionDescription = "The message to send";


    @Override
    protected void createCommand() {
        command = Commands.slash(super.name, super.description)
                .addOption(OptionType.STRING, optionName, optionDescription, true);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(name)) {
            if(DEVELOPER_DISCORD_IDS.contains(event.getUser().getId())){
                event.deferReply().setEphemeral(true).queue();

                String message = event.getOption(optionName).getAsString();
                sendToAllServers(message);

                event.getHook()
                        .setEphemeral(true)
                        .sendMessage("Message sent to all servers")
                        .queue();
            } else {
                event.getHook()
                        .setEphemeral(true)
                        .sendMessage("You do not have permission to use this command")
                        .queue();
            }
        }
    }

    public void sendToAllServers(String message) {
        // todo: send to 'set' channel instead and check if bot has permission to send messages there
        super.api.getGuilds().forEach(guild -> {
            guild.getDefaultChannel().asTextChannel().sendMessage(message).queue();
        });
    }
}
