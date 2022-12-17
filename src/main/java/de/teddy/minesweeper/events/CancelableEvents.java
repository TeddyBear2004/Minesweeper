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

public class CancelableEvents implements Listener {

    public static final NamespacedKey BYPASS_EVENTS = new NamespacedKey(Minesweeper.getPlugin(), "bypass_events");
    private final boolean cancelEntityDamage;
    private final boolean cancelFoodChange;
    private final boolean cancelBlockPlace;
    private final boolean cancelBlockBreak;
    private final boolean cancelInventoryInteract;
    private final boolean cancelDropItem;
    private final boolean cancelPickupItem;

    public CancelableEvents(ConfigurationSection section) {
        if (section == null) {
            cancelEntityDamage = true;
            cancelFoodChange = true;
            cancelBlockPlace = true;
            cancelBlockBreak = true;
            cancelInventoryInteract = true;
            cancelDropItem = true;
            cancelPickupItem = true;
        } else {
            cancelEntityDamage = section.getBoolean("cancelEntityDamage", true);
            cancelFoodChange = section.getBoolean("cancelFoodChange", true);
            cancelBlockPlace = section.getBoolean("cancelBlockPlace", true);
            cancelBlockBreak = section.getBoolean("cancelBlockBreak", true);
            cancelInventoryInteract = section.getBoolean("cancelInventoryInteract", true);
            cancelDropItem = section.getBoolean("cancelDropItem", true);
            cancelPickupItem = section.getBoolean("cancelPickupItem", true);
        }
    }

    private static boolean shouldCancel(Player player) {
        return player.getPersistentDataContainer().getOrDefault(BYPASS_EVENTS, PersistentDataType.BYTE, (byte) 0) == 0b0;
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!cancelEntityDamage)
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
        if (!cancelFoodChange)
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
        if (!cancelBlockPlace)
            return;

        if (shouldCancel(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (!cancelBlockBreak)
            return;

        if (shouldCancel(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryInteractEvent(InventoryInteractEvent event) {
        if (!cancelInventoryInteract)
            return;

        if (event.getWhoClicked() instanceof Player player)
            if (shouldCancel(player))
                event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        if (!cancelDropItem)
            return;

        if (shouldCancel(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (!cancelPickupItem)
            return;

        if (event.getEntity() instanceof Player player)
            if (shouldCancel(player))
                event.setCancelled(true);
    }

}
