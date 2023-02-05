package de.teddybear2004.retro.games.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class IsBetween {

    public static boolean isOutside2D(@NotNull Location corner, int width, int height, @NotNull Location location) {
        int minX = Math.min(corner.getBlockX(), corner.getBlockX() + width);
        int maxX = Math.max(corner.getBlockX(), corner.getBlockX() + width);
        int minZ = Math.min(corner.getBlockZ(), corner.getBlockZ() + height);
        int maxZ = Math.max(corner.getBlockZ(), corner.getBlockZ() + height);

        return minX > location.getX() || location.getX() > maxX || minZ > location.getZ() || location.getZ() > maxZ;
    }

    public static boolean isOutside(double x1, double x2, double clicked) {
        return !(Math.min(x1, x2) <= clicked) || !(clicked <= Math.max(x1, x2));
    }

}
