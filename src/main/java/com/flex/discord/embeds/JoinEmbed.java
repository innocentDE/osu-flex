package com.flex.discord.embeds;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

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
                        "/add [username]",
                        "adds a user to osu!flex",
                        true
                )
                .addField(
                        "/remove [username]",
                        "removes a user from osu!flex",
                        true
                )
                .addField(
                        "/set [channel]",
                        "sets the channel where osu!flex sends the top plays",
                        true
                )
                .addField(
                        "/list",
                        "lists all tracked users on this server",
                        true
                )
                .addField("Links",
                        "For help or (feature) requests join the [osu!flex](https://discord.gg/PxFdAkejV9) discord server!\n" +
                        "Bug report and contribution on [GitHub](https://github.com/innocentDE/osu-flex).", false)
                .setColor(new Color(255, 255, 255))
                .setFooter("osu!flex - v.1.0.0")
                .build();
    }
}
