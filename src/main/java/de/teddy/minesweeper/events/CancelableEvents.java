package de.teddy.minesweeper.events;

import de.teddy.minesweeper.Minesweeper;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class CancelableEvents implements Listener {

    public static final NamespacedKey BYPASS_EVENTS = new NamespacedKey(Minesweeper.getPlugin(), "bypass_events");
    private final Map<CancelableEvent, Boolean> cancelableEventBooleanMap = new HashMap<>();

    public CancelableEvents(ConfigurationSection section) {
        if (section != null) {
            cancelableEventBooleanMap.put(CancelableEvent.CANCEL_ENTITY_DAMAGE, section.getBoolean("cancelEntityDamage", true));
            cancelableEventBooleanMap.put(CancelableEvent.CANCEL_FOOD_CHANGE, section.getBoolean("cancelFoodChange", true));
            cancelableEventBooleanMap.put(CancelableEvent.CANCEL_BLOCK_PLACE, section.getBoolean("cancelBlockPlace", true));
            cancelableEventBooleanMap.put(CancelableEvent.CANCEL_BLOCK_BREAK, section.getBoolean("cancelBlockBreak", true));
            cancelableEventBooleanMap.put(CancelableEvent.CANCEL_INVENTORY_INTERACT, section.getBoolean("cancelInventoryInteract", true));
            cancelableEventBooleanMap.put(CancelableEvent.CANCEL_DROP_ITEM, section.getBoolean("cancelDropItem", true));
            cancelableEventBooleanMap.put(CancelableEvent.CANCEL_PICKUP_ITEM, section.getBoolean("cancelPickupItem", true));
        }

        for (CancelableEvent value : CancelableEvent.values()) {
            if (!cancelableEventBooleanMap.containsKey(value))
                cancelableEventBooleanMap.put(value, true);
        }
    }

    private static boolean shouldCancel(Player player) {
        return player.getPersistentDataContainer().getOrDefault(BYPASS_EVENTS, PersistentDataType.BYTE, (byte) 0) == 0b0;
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!cancelableEventBooleanMap.get(CancelableEvent.CANCEL_ENTITY_DAMAGE))
            return;

        if (event.getEntity() instanceof Player player) {
            if (shouldCancel(player)) {
                player.setInvulnerable(true);
                player.setHealth(20);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (!cancelableEventBooleanMap.get(CancelableEvent.CANCEL_FOOD_CHANGE))
            return;

        if (event.getEntity() instanceof Player player) {
            if (shouldCancel(player)) {
                event.setFoodLevel(20);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (!cancelableEventBooleanMap.get(CancelableEvent.CANCEL_BLOCK_PLACE))
            return;

        if (shouldCancel(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (!cancelableEventBooleanMap.get(CancelableEvent.CANCEL_BLOCK_BREAK))
            return;

        if (shouldCancel(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryInteractEvent(InventoryInteractEvent event) {
        if (!cancelableEventBooleanMap.get(CancelableEvent.CANCEL_INVENTORY_INTERACT))
            return;

        if (event.getWhoClicked() instanceof Player player)
            if (shouldCancel(player))
                event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        if (!cancelableEventBooleanMap.get(CancelableEvent.CANCEL_DROP_ITEM))
            return;

        if (shouldCancel(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (!cancelableEventBooleanMap.get(CancelableEvent.CANCEL_PICKUP_ITEM))
            return;

        if (event.getEntity() instanceof Player player)
            if (shouldCancel(player))
                event.setCancelled(true);
    }

}
