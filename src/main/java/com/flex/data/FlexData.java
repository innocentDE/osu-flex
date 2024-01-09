package com.flex.data;

import lombok.Getter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
public abstract class FlexData {

    public static final String CLIENT_VERSION = "osu!flex - v.1.0.0";
    public static final int SLEEP = 30000;
    public static final int ERROR_SLEEP = 5000;
    public static final int DEFAULT_THRESHOLD = 20;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MAX_TOP_PLAYS = 100;
    public static final List<Long> DEVELOPER_DISCORD_IDS = Arrays.asList(283613120981762049L, 340774639821127680L);
    public static final String ERROR_MESSAGE = "Something went wrong";

    public static Color getRandomOsuPaletteColor() {
        int[][] osuColorPalette = {
                {240, 98, 161},
                {246, 104, 167},
                {255, 121, 184},
                {255, 124, 187},
                {255, 135, 198}
        };

        Random random = new Random();
        int randomIndex = random.nextInt(osuColorPalette.length);
        int[] rgb = osuColorPalette[randomIndex];

        return new Color(rgb[0], rgb[1], rgb[2]);
    }
}
