package de.teddy.minesweeper.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class IsBetween {
    public static boolean isBetween2D(Location corner, int width, int height, Block clickedBlock){
        return isBetween(corner.getBlockX(),
                corner.getBlockX() + width,
                clickedBlock.getX())
                && isBetween(corner.getBlockZ(),
                corner.getBlockZ() + height,
                clickedBlock.getZ());
    }

    public static boolean isBetween(double x1, double x2, double clicked){
        return x1 < x2 ? x1 <= clicked && clicked <= x2 : x2 <= clicked && clicked <= x1;
    }
}
