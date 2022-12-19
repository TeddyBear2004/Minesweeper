package de.teddy.minesweeper.game.temporary;

import de.teddy.minesweeper.events.CancelableEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class Area {

    private final Location loc1;
    private final Location loc2;
    private final boolean temporaryInventory;
    private final boolean temporaryFly;
    private final Map<CancelableEvent, Boolean> temporaryEvents;

    public Area(Map<?, ?> map) {
        this(readLoc1(map),
             readLoc2(map),
             (boolean) ((Map<?, ?>) map.get("actions")).get("temporary_inventory"),
             (boolean) ((Map<?, ?>) map.get("actions")).get("temporary_inventory"),
             readTemporaryEvents((Map<?, ?>) ((Map<?, ?>) map.get("actions")).get("cancelled_events"))
        );
    }


    public Area(Location loc1, Location loc2, boolean temporaryInventory, boolean temporaryFly, Map<CancelableEvent, Boolean> temporaryEvents) {
        this.loc1 = new Location(loc1.getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockY(), loc2.getBlockY()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
        this.loc2 = new Location(loc1.getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockY(), loc2.getBlockY()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        this.temporaryInventory = temporaryInventory;
        this.temporaryFly = temporaryFly;
        this.temporaryEvents = temporaryEvents;
    }

    private static Location readLoc1(Map<?, ?> map) {
        return new Location(Bukkit.getWorld(map.get("world").toString()), (int) map.get("x1"), (int) map.get("y1"), (int) map.get("z1"));
    }

    private static Location readLoc2(Map<?, ?> map) {
        return new Location(Bukkit.getWorld(map.get("world").toString()), (int) map.get("x2"), (int) map.get("y2"), (int) map.get("z2"));
    }

    private static Map<CancelableEvent, Boolean> readTemporaryEvents(Map<?, ?> map) {
        Map<CancelableEvent, Boolean> cancelableEventBooleanMap = new HashMap<>();
        map = (Map<?, ?>) map.get("cancelled_events");

        if (map != null) {
            cancelableEventBooleanMap.put(CancelableEvent.ENTITY_DAMAGE, (Boolean) map.get("cancelEntityDamage"));
            cancelableEventBooleanMap.put(CancelableEvent.FOOD_CHANGE, (Boolean) map.get("cancelFoodChange"));
            cancelableEventBooleanMap.put(CancelableEvent.BLOCK_PLACE, (Boolean) map.get("cancelBlockPlace"));
            cancelableEventBooleanMap.put(CancelableEvent.BLOCK_BREAK, (Boolean) map.get("cancelBlockBreak"));
            cancelableEventBooleanMap.put(CancelableEvent.INVENTORY_INTERACT, (Boolean) map.get("cancelInventoryInteract"));
            cancelableEventBooleanMap.put(CancelableEvent.DROP_ITEM, (Boolean) map.get("cancelDropItem"));
            cancelableEventBooleanMap.put(CancelableEvent.PICKUP_ITEM, (Boolean) map.get("cancelPickupItem"));
        }

        for (CancelableEvent value : CancelableEvent.values()) {
            if (!cancelableEventBooleanMap.containsKey(value))
                cancelableEventBooleanMap.put(value, true);
        }

        return cancelableEventBooleanMap;
    }

    public boolean isInArea(Location location) {
        return location.getX() >= loc1.getX() && location.getX() <= loc2.getX()
                && location.getY() >= loc1.getY() && location.getY() <= loc2.getY()
                && location.getZ() >= loc1.getZ() && location.getZ() <= loc2.getZ();
    }

    public boolean isTemporaryFlightEnabled() {
        return temporaryFly;
    }

    public boolean getTemporaryEvents(CancelableEvent event) {
        return temporaryEvents.getOrDefault(event, false);
    }

}
