package de.teddy.minesweeper.events;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.temporary.Area;
import org.bukkit.Location;
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
            cancelableEventBooleanMap.put(CancelableEvent.ENTITY_DAMAGE, section.getBoolean("cancelEntityDamage", true));
            cancelableEventBooleanMap.put(CancelableEvent.FOOD_CHANGE, section.getBoolean("cancelFoodChange", true));
            cancelableEventBooleanMap.put(CancelableEvent.BLOCK_PLACE, section.getBoolean("cancelBlockPlace", true));
            cancelableEventBooleanMap.put(CancelableEvent.BLOCK_BREAK, section.getBoolean("cancelBlockBreak", true));
            cancelableEventBooleanMap.put(CancelableEvent.INVENTORY_INTERACT, section.getBoolean("cancelInventoryInteract", true));
            cancelableEventBooleanMap.put(CancelableEvent.DROP_ITEM, section.getBoolean("cancelDropItem", true));
            cancelableEventBooleanMap.put(CancelableEvent.PICKUP_ITEM, section.getBoolean("cancelPickupItem", true));
        }

        for (CancelableEvent value : CancelableEvent.values()) {
            if (!cancelableEventBooleanMap.containsKey(value))
                cancelableEventBooleanMap.put(value, true);
        }
    }

    private static boolean shouldCancel(Player player) {
        return player.getPersistentDataContainer().getOrDefault(BYPASS_EVENTS, PersistentDataType.BYTE, (byte) 0) == 0b0;
    }

    private static boolean isInsideAreaAndShouldBeCanceled(Location location, CancelableEvent event) {
        for (Area area : Minesweeper.getAreas()) {
            if (area.isInArea(location)) {
                if (area.getTemporaryEvents(event))
                    return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.ENTITY_DAMAGE)
                || isInsideAreaAndShouldBeCanceled(event.getEntity().getLocation(), CancelableEvent.ENTITY_DAMAGE)) {
            if (event.getEntity() instanceof Player player) {
                if (shouldCancel(player)) {
                    player.setInvulnerable(true);
                    player.setHealth(20);
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.FOOD_CHANGE)
                || isInsideAreaAndShouldBeCanceled(event.getEntity().getLocation(), CancelableEvent.FOOD_CHANGE)) {
            if (event.getEntity() instanceof Player player) {
                if (shouldCancel(player)) {
                    event.setFoodLevel(20);
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.BLOCK_PLACE)
                || isInsideAreaAndShouldBeCanceled(event.getBlockPlaced().getLocation(), CancelableEvent.BLOCK_PLACE)) {
            if (shouldCancel(event.getPlayer()))
                event.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.BLOCK_BREAK)
                || isInsideAreaAndShouldBeCanceled(event.getBlock().getLocation(), CancelableEvent.BLOCK_BREAK)) {
            if (shouldCancel(event.getPlayer()))
                event.setCancelled(true);
        }

    }

    @EventHandler
    public void onInventoryInteractEvent(InventoryInteractEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.INVENTORY_INTERACT)
                || isInsideAreaAndShouldBeCanceled(event.getWhoClicked().getLocation(), CancelableEvent.INVENTORY_INTERACT)) {
            if (event.getWhoClicked() instanceof Player player)
                if (shouldCancel(player))
                    event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.DROP_ITEM)
                || isInsideAreaAndShouldBeCanceled(event.getPlayer().getLocation(), CancelableEvent.DROP_ITEM)) {
            if (shouldCancel(event.getPlayer()))
                event.setCancelled(true);
        }

    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.PICKUP_ITEM)
                || isInsideAreaAndShouldBeCanceled(event.getEntity().getLocation(), CancelableEvent.PICKUP_ITEM)) {
            if (event.getEntity() instanceof Player player)
                if (shouldCancel(player))
                    event.setCancelled(true);
        }

    }

}
