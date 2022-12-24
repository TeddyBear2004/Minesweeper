package de.teddy.minesweeper.game.modifier;

import de.teddy.minesweeper.events.CancelableEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class Modifier {

    private final boolean temporaryFly;
    private final boolean allowFly;
    private final boolean allowDefaultWatch;
    private final Map<CancelableEvent, Boolean> temporaryEvents;

    public Modifier(FileConfiguration config) {
        this(false,
             config.getBoolean("allow_fly", true),
             config.getBoolean("allow_default_watch", true),
             readTemporaryEvents(null));
    }

    public Modifier(ConfigurationSection config, ConfigurationSection section) {
        this(section.getBoolean("temporary_fly", false),
             config.getBoolean("allow_fly", true),
             config.getBoolean("allow_default_watch", true),
             readTemporaryEvents(section.getConfigurationSection("cancelled_events"))
        );
    }

    public Modifier(boolean temporaryFly, boolean allowFly, boolean allowDefaultWatch, Map<CancelableEvent, Boolean> temporaryEvents) {
        this.temporaryFly = temporaryFly;
        this.allowFly = allowFly;
        this.allowDefaultWatch = allowDefaultWatch;
        this.temporaryEvents = temporaryEvents;
    }


    private static Map<CancelableEvent, Boolean> readTemporaryEvents(ConfigurationSection map) {
        Map<CancelableEvent, Boolean> cancelableEventBooleanMap = new HashMap<>();

        if (map != null) {
            cancelableEventBooleanMap.put(CancelableEvent.ENTITY_DAMAGE, map.getBoolean("cancelEntityDamage", true));
            cancelableEventBooleanMap.put(CancelableEvent.FOOD_CHANGE, map.getBoolean("cancelFoodChange", true));
            cancelableEventBooleanMap.put(CancelableEvent.BLOCK_PLACE, map.getBoolean("cancelBlockPlace", true));
            cancelableEventBooleanMap.put(CancelableEvent.BLOCK_BREAK, map.getBoolean("cancelBlockBreak", true));
            cancelableEventBooleanMap.put(CancelableEvent.INVENTORY_INTERACT, map.getBoolean("cancelInventoryInteract", true));
            cancelableEventBooleanMap.put(CancelableEvent.DROP_ITEM, map.getBoolean("cancelDropItem", true));
            cancelableEventBooleanMap.put(CancelableEvent.PICKUP_ITEM, map.getBoolean("cancelPickupItem", true));
        }

        for (CancelableEvent value : CancelableEvent.values()) {
            if (!cancelableEventBooleanMap.containsKey(value))
                cancelableEventBooleanMap.put(value, true);
        }

        return cancelableEventBooleanMap;
    }

    public boolean isTemporaryFlightEnabled() {
        return temporaryFly;
    }

    public boolean allowFly() {
        return allowFly;
    }

    public boolean allowDefaultWatch() {
        return allowDefaultWatch;
    }

    public boolean getTemporaryEvents(CancelableEvent event) {
        return temporaryEvents.getOrDefault(event, false);
    }

}
