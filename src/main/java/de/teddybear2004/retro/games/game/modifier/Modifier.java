package de.teddybear2004.retro.games.game.modifier;

import de.teddybear2004.retro.games.events.CancelableEvent;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a game modifier that can change certain aspects of the game.
 *
 * @author Teddy
 * @version 1.0
 * @see ModifierArea
 * @see CancelableEvent
 * @since 1.0
 */
public class Modifier {

    private static Modifier modifier;
    private final boolean temporaryFly;
    private final boolean allowFly;
    private final boolean allowDefaultWatch;
    private final Map<CancelableEvent, Boolean> temporaryEvents;
    private final List<ModifierArea> areas;

    /**
     * Creates a new Modifier instance using the specified configuration and areas.
     *
     * @param config The configuration containing the settings for the modifier.
     * @param areas  The list of areas where the modifier is active.
     */
    private Modifier(@NotNull FileConfiguration config, List<ModifierArea> areas) {
        this(false,
             config.getBoolean("allow_fly", true),
             config.getBoolean("allow_default_watch", true),
             readTemporaryEvents(null),
             areas
        );
    }

    private static @NotNull Map<CancelableEvent, Boolean> readTemporaryEvents(@Nullable ConfigurationSection map) {
        Map<CancelableEvent, Boolean> cancelableEventBooleanMap = new HashMap<>();

        for (CancelableEvent cancelableEvent : CancelableEvent.values()) {
            cancelableEventBooleanMap.put(cancelableEvent, map == null
                    ? cancelableEvent.getDefaultValue()
                    : map.getBoolean(cancelableEvent.getKey(), cancelableEvent.getDefaultValue()));
        }

        return cancelableEventBooleanMap;
    }

    /**
     * Creates a new Modifier instance using the specified parameters.
     *
     * @param temporaryFly      Flag indicating whether temporary flight is enabled.
     * @param allowFly          Flag indicating whether flying is allowed.
     * @param allowDefaultWatch Flag indicating whether default watch is allowed.
     * @param temporaryEvents   Map of temporary events and their cancellation status.
     * @param areas             List of areas where the modifier is active.
     */
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

    /**
     * Creates a new Modifier instance using the specified configuration and areas.
     *
     * @param config  The configuration containing the settings for the modifier.
     * @param section The configuration section for the specific modifier.
     * @param areas   The list of areas where the modifier is active.
     */
    private Modifier(@NotNull ConfigurationSection config, @NotNull ConfigurationSection section, List<ModifierArea> areas) {
        this(section.getBoolean("temporary_fly", false),
             config.getBoolean("allow_fly", true),
             config.getBoolean("allow_default_watch", true),
             readTemporaryEvents(section.getConfigurationSection("cancelled_events")),
             areas
        );
    }

    public static void initialise(@NotNull ConfigurationSection config, @NotNull ConfigurationSection section, List<ModifierArea> areas) {
        if (modifier == null)
            modifier = new Modifier(config, section, areas);
    }

    public static void initialise(@NotNull FileConfiguration config, List<ModifierArea> areas) {
        if (modifier == null) modifier = new Modifier(config, areas);
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

    public boolean fromOutsideToInside(@NotNull PlayerMoveEvent event) {
        for (ModifierArea modifierArea : areas)
            if (!modifierArea.isInArea(event.getFrom()) && event.getTo() != null && modifierArea.isInArea(event.getTo()))
                return true;

        return false;
    }

    public boolean fromInsideToOutside(@NotNull PlayerMoveEvent event) {
        for (ModifierArea modifierArea : areas)
            if (event.getTo() != null && !modifierArea.isInArea(event.getTo()) && modifierArea.isInArea(event.getFrom()))
                return true;

        return false;
    }

    public boolean isInside(@NotNull Location location) {
        for (ModifierArea modifierArea : areas)
            if (modifierArea.isInArea(location))
                return true;

        return false;

    }

}
