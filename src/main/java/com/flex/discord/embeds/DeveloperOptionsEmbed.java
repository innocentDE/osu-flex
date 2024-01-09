package com.flex.discord.embeds;

import com.flex.data.FlexData;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public class DeveloperOptionsEmbed {

    private final MessageEmbed embed;

    public DeveloperOptionsEmbed() {
        embed = createEmbed();
    }

    private MessageEmbed createEmbed() {
        return new EmbedBuilder()
                .setTitle("Developer Tools")
                .setDescription("A set of useful tools that might come in handy for developers")
                .addField(
                        "msgall",
                        "Send a message to **all** servers with a given message - be careful with this one!",
                        true
                )
                .setColor(FlexData.getRandomOsuPaletteColor())
                .setFooter(FlexData.CLIENT_VERSION)
                .build();
    }
}
