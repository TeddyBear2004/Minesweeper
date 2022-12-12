package de.teddy.minesweeper.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class IsBetween {

    public static boolean isBetween2D(Location corner, int width, int height, Block clickedBlock) {
        int minX = Math.min(corner.getBlockX(), corner.getBlockX() + width);
        int maxX = Math.max(corner.getBlockX(), corner.getBlockX() + width);
        int minZ = Math.min(corner.getBlockZ(), corner.getBlockZ() + height);
        int maxZ = Math.max(corner.getBlockZ(), corner.getBlockZ() + height);

        return minX <= clickedBlock.getX() && clickedBlock.getX() <= maxX && minZ <= clickedBlock.getZ() && clickedBlock.getZ() <= maxZ;
    }

    public static boolean isBetween(double x1, double x2, double clicked) {
        return Math.min(x1, x2) <= clicked && clicked <= Math.max(x1, x2);
    }

}
