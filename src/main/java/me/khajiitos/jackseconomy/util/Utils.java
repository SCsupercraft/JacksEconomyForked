package me.khajiitos.jackseconomy.util;

public class Utils {

    public static float clamp(float value, float min, float max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }

        return value;
    }

    public static double clamp(double value, double min, double max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }

        return value;
    }

    public static int hexToMinecraftColor(String hexColor) {
        int color = Integer.parseInt(hexColor.substring(1), 16);

        if (hexColor.length() == 7) {
            color |= 0xFF000000;
        }

        return color;
    }

}
