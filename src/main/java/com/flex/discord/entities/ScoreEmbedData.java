package com.flex.discord.entities;

import com.flex.data.FlexData;
import com.flex.osu.api.requests.utility.ColorFinder;
import com.flex.osu.entities.OsuData;
import com.flex.osu.entities.score.Score;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;

@Getter
@Setter
public class ScoreEmbedData {

    private final Logger logger = LogManager.getLogger(ScoreEmbedData.class);

    private final Score score;
    private String title;
    private String mods;
    private String rating;
    private String bpm;
    private String length;
    private String rank;
    private String accuracy;
    private String statistics;
    private String pp;
    private String scoreUrl;
    private Color color;
    private String author;
    private String userUrl;
    private String avatar;
    private String version;
    private String cover;
    private int scoreIndex;

    public ScoreEmbedData(OsuData data) {
        this.score = data.getScore();
        this.scoreIndex = data.getScoreIndex();
        setDefaults();
    }

    private void setDefaults() {
        setTitle();
        setLength();
        setMods();
        setRating();
        setBpm();
        setRank();
        setAccuracy();
        setStatistics();
        setPp();
        setScoreUrl();
        setColor();
        setAuthor();
        setUserUrl();
        setAvatar();
        setVersion();
        setCover();
    }

    private void setTitle() {
        title = score.beatmapset.artist + " - " + score.beatmapset.title + " [" + score.beatmap.version + "]";
    }

    private void setLength() {
        Duration duration = Duration.ofSeconds(score.beatmap.total_length);
        length = String.format("%d:%02d", duration.toMinutes(), duration.toSecondsPart());
    }

    private void setMods() {
        mods = score.mods.isEmpty() ? "NM" : String.join("", score.mods);
    }

    private void setRating() {
        rating = String.valueOf(score.beatmap.difficulty_rating);
    }

    private void setBpm() {
        bpm = String.valueOf(score.beatmap.bpm);
    }

    private void setRank() {
        rank = score.rank;
    }

    private void setAccuracy() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        accuracy = decimalFormat.format(score.accuracy * 100);
    }

    private void setStatistics() {
        statistics = "[" + score.statistics.count_300 + "/" + score.statistics.count_100
                + "/" + score.statistics.count_50 + "/" + score.statistics.count_miss + "]";
    }

    private void setPp() {
        pp = String.valueOf(Math.round(score.pp));
    }

    private void setScoreUrl() {
        scoreUrl = "https://osu.ppy.sh/scores/osu/" + score.best_id;
    }

    private void setColor() {

        ColorFinder colorFinder = new ColorFinder();
        try {
            color = colorFinder.getDominantColor(score.user.avatar_url);
        } catch (IOException e) {
            logger.warn(e.getStackTrace());
            color = FlexData.getRandomOsuPaletteColor();
        }
    }

    private void setAuthor() {
        author = String.format("New #%s top play for %s", scoreIndex, score.user.username);
    }

    private void setUserUrl() {
        userUrl = "https://osu.ppy.sh/users/" + score.user.id;
    }

    private void setAvatar() {
        avatar = score.user.avatar_url;
    }

    private void setVersion() {
        version = FlexData.CLIENT_VERSION;
    }

    private void setCover() {
        cover = score.beatmapset.covers.cover;
    }
}
