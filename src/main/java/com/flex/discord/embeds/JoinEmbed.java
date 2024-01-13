package com.flex.discord.embeds;

import com.flex.data.FlexData;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public class JoinEmbed {

    private final MessageEmbed embed;

    public JoinEmbed() {
        embed = createEmbed();
    }

    private MessageEmbed createEmbed() {
        return new EmbedBuilder()
                .setTitle("osu!flex")
                .setDescription("osu!flex is a discord bot that allows you to track players' top plays and send them to your discord server")
                .addField(
                        "/help",
                        "Opens a list with all commands for the bot",
                        true
                )
                .addField("Links",
                        "For help or (feature) requests join the [osu!flex](https://discord.gg/PxFdAkejV9) discord server!\n" +
                                "Bug report and contribution on [GitHub](https://github.com/innocentDE/osu-flex).", false)
                .setColor(FlexData.getRandomOsuPaletteColor())
                .setFooter(FlexData.CLIENT_VERSION)
                .build();
    }
}
