package com.flex.discord.embeds;

import com.flex.data.FlexData;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Getter
public class HelpEmbed {

    private final MessageEmbed embed;

    public HelpEmbed() {
        embed = createEmbed();
    }

    private MessageEmbed createEmbed() {
        return new EmbedBuilder()
                .setTitle("Help")
                .setDescription("A list of commands for the bot")
                .addField(
                        "/add [username]",
                        "Adds a user to osu!flex",
                        false
                )
                .addField(
                        "/remove [username]",
                        "Removes a user from osu!flex",
                        false
                )
                .addField(
                        "/set [channel]",
                        "Sets the channel where osu!flex sends the top plays",
                        false
                )
                .addField(
                        "/list",
                        "Lists all tracked users on this server",
                        false
                )
                .addField(
                        "/help",
                        "Opens a list with all commands for the bot",
                        false
                )
                .addField(
                        "Links",
                        "For help join the [osu!flex](https://discord.gg/PxFdAkejV9) discord server!\n" +
                                "Bug report on [GitHub](https://github.com/innocentDE/osu-flex).",
                        false
                )
                .setColor(FlexData.getRandomOsuPaletteColor())
                .setFooter(FlexData.CLIENT_VERSION)
                .build();
    }
}
