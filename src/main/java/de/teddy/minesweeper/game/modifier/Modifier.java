package de.teddy.minesweeper.game.modifier;

import de.teddy.minesweeper.events.CancelableEvent;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Modifier {

    private static Modifier modifier;
    private final boolean temporaryFly;
    private final boolean allowFly;
    private final boolean allowDefaultWatch;
    private final Map<CancelableEvent, Boolean> temporaryEvents;
    private final List<ModifierArea> areas;

    private Modifier(FileConfiguration config, List<ModifierArea> areas) {
        this(false,
             config.getBoolean("allow_fly", true),
             config.getBoolean("allow_default_watch", true),
             readTemporaryEvents(null),
             areas
        );
    }

    private Modifier(ConfigurationSection config, ConfigurationSection section, List<ModifierArea> areas) {
        this(section.getBoolean("temporary_fly", false),
             config.getBoolean("allow_fly", true),
             config.getBoolean("allow_default_watch", true),
             readTemporaryEvents(section.getConfigurationSection("cancelled_events")),
             areas
        );
    }

    private Modifier(boolean temporaryFly, boolean allowFly, boolean allowDefaultWatch, Map<CancelableEvent, Boolean> temporaryEvents, List<ModifierArea> areas) {
        this.temporaryFly = temporaryFly;
        this.allowFly = allowFly;
        this.allowDefaultWatch = allowDefaultWatch;
        this.temporaryEvents = temporaryEvents;
        this.areas = areas;
    }

    public static Modifier getInstance() {
        return modifier;
    }

    public static void initialise(ConfigurationSection config, ConfigurationSection section, List<ModifierArea> areas) {
        if (modifier != null)
            modifier = new Modifier(config, section, areas);
    }

    public static void initialise(FileConfiguration config, List<ModifierArea> areas) {
        if (modifier != null) modifier = new Modifier(config, areas);
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

    public boolean fromOutsideToInside(PlayerMoveEvent event) {
        for (ModifierArea modifierArea : areas)
            if (!modifierArea.isInArea(event.getFrom()) && event.getTo() != null && modifierArea.isInArea(event.getTo()))
                return true;

        return false;
    }

    public boolean fromInsideToOutside(PlayerMoveEvent event) {
        for (ModifierArea modifierArea : areas)
            if (event.getTo() != null && !modifierArea.isInArea(event.getTo()) && modifierArea.isInArea(event.getFrom()))
                return true;

        return false;
    }

    public boolean isInside(Location location) {
        for (ModifierArea modifierArea : areas)
            if (modifierArea.isInArea(location))
                return true;

        return false;

    }

}
