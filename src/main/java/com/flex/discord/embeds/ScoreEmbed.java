package com.flex.discord.embeds;

import com.flex.discord.entities.ScoreEmbedData;
import com.flex.osu.entities.score.Score;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;

public class ScoreEmbed {

    private final ScoreEmbedData data;

    @Getter
    private MessageEmbed embed;

    public ScoreEmbed(Score score) {
        data = new ScoreEmbedData(score);
        embed = createEmbed();
    }

    private MessageEmbed createEmbed() {
        return new EmbedBuilder()
                .setTitle(data.getTitle())
                .setDescription(data.getMods() + " at **" + data.getRating() + "** :star: - " + data.getBpm() + " bpm - " + data.getLength() + "\n" +
                        "**" + data.getRank() + "** Rank (" + data.getAccuracy() + "%) - " + data.getStatistics() + "\n" +
                        "**" + data.getPp() + " pp**")
                .setUrl(data.getScoreUrl())
                .setColor(data.getColor())
                .setAuthor(data.getAuthor(), data.getUserUrl(), data.getAvatar())
                .setFooter(data.getVersion())
                .setTimestamp(Instant.now())
                .setImage(data.getCover())
                .build();
    }
}
