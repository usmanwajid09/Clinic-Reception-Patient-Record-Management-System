package com.clinic.ui.animation;

import java.awt.Color;

/**
 * Utility for smooth RGB and Alpha linear interpolation between two colors.
 */
public final class ColorInterpolator {
    private ColorInterpolator() {}

    /**
     * Blends color c1 into color c2 based on fraction (0.0f = c1, 1.0f = c2).
     */
    public static Color blend(Color c1, Color c2, float fraction) {
        float f = Math.max(0.0f, Math.min(1.0f, fraction));

        int r = (int) (c1.getRed() + f * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + f * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + f * (c2.getBlue() - c1.getBlue()));
        int a = (int) (c1.getAlpha() + f * (c2.getAlpha() - c1.getAlpha()));

        return new Color(r, g, b, a);
    }

    /**
     * Creates a color with custom alpha channel (0 to 255).
     */
    public static Color withAlpha(Color color, int alpha) {
        int clampedAlpha = Math.max(0, Math.min(255, alpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampedAlpha);
    }
}

