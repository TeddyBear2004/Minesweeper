package de.teddy.minesweeper.game.temporary;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Map;

public class Area {

    private final Location loc1;
    private final Location loc2;

    public Area(Map<?, ?> map) {
        this(readLoc1(map),
             readLoc2(map));
    }


    public Area(Location loc1, Location loc2) {
        this.loc1 = new Location(loc1.getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockY(), loc2.getBlockY()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
        this.loc2 = new Location(loc1.getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockY(), loc2.getBlockY()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        }

    private static Location readLoc1(Map<?, ?> map) {
        return new Location(Bukkit.getWorld(map.get("world").toString()), (int) map.get("x1"), (int) map.get("y1"), (int) map.get("z1"));
    }

    private static Location readLoc2(Map<?, ?> map) {
        return new Location(Bukkit.getWorld(map.get("world").toString()), (int) map.get("x2"), (int) map.get("y2"), (int) map.get("z2"));
    }


    public boolean isInArea(Location location) {
        return location.getX() >= loc1.getX() && location.getX() <= loc2.getX()
                && location.getY() >= loc1.getY() && location.getY() <= loc2.getY()
                && location.getZ() >= loc1.getZ() && location.getZ() <= loc2.getZ();
    }

}
