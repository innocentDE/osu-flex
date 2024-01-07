package com.flex.osu.api.requests.utility;


import com.flex.data.FlexData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ColorFinder {

    public Color getDominantColor(String userAvatar) throws IOException {
        BufferedImage avatar = ImageIO.read(new URL(userAvatar));
        return getAverageColor(avatar);
    }

    private Color getAverageColor(BufferedImage image) {
        long sumRed = 0, sumGreen = 0, sumBlue = 0, totalPixels = 0;

        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                if (alpha > 32) {
                    sumRed += (rgb >> 16) & 0xFF;
                    sumGreen += (rgb >> 8) & 0xFF;
                    sumBlue += rgb & 0xFF;
                    totalPixels++;
                }
            }
        }

        if (totalPixels == 0) {
            return FlexData.getRandomOsuPaletteColor();
        }

        int avgRed = (int) (sumRed / totalPixels);
        int avgGreen = (int) (sumGreen / totalPixels);
        int avgBlue = (int) (sumBlue / totalPixels);

        float[] hsb = Color.RGBtoHSB(avgRed, avgGreen, avgBlue, null);
        float saturation = hsb[1];

        float increasedSaturation = Math.min(1.0f, saturation * 1.5f);

        return Color.getHSBColor(hsb[0], increasedSaturation, hsb[2]);
    }
}