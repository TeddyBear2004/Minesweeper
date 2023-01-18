package de.teddybear2004.minesweeper.events;

import org.jetbrains.annotations.Contract;

public enum CancelableEvent {
    ENTITY_DAMAGE("cancelEntityDamage", true),
    FOOD_CHANGE("cancelFoodChange", true),
    BLOCK_PLACE("cancelBlockPlace", true),
    BLOCK_BREAK("cancelBlockBreak", true),
    INVENTORY_INTERACT("cancelInventoryInteract", true),
    DROP_ITEM("cancelDropItem", true),
    PICKUP_ITEM("cancelPickupItem", true);

    private final String key;
    private final boolean defaultValue;

    CancelableEvent(String key, boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    /**
     * @return The associated key.
     */
    @Contract(pure = true)
    public String getKey() {
        return key;
    }

    /**
     * @return The default value.
     */
    @Contract(pure = true)
    public boolean getDefaultValue() {
        return defaultValue;
    }
}
