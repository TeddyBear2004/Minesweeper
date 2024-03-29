package de.teddybear2004.retro.games.events;

import de.teddybear2004.retro.games.RetroGames;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.modifier.Modifier;
import de.teddybear2004.retro.games.game.modifier.ModifierArea;
import de.teddybear2004.retro.games.game.modifier.PersonalModifier;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CancelableEvents implements Listener {

    public static final NamespacedKey BYPASS_EVENTS = new NamespacedKey(RetroGames.getPlugin(RetroGames.class), "bypass_events");
    private final Map<CancelableEvent, Boolean> cancelableEventBooleanMap = new HashMap<>();
    private final List<? extends ModifierArea> areas;
    private final GameManager gameManager;

    /**
     * @param section The {@link ConfigurationSection} to load the event cancellation from
     * @param areas   A list of all areas to determine where/if an event should be cancelled
     */
    public CancelableEvents(@Nullable ConfigurationSection section, List<? extends ModifierArea> areas, GameManager gameManager) {
        this.areas = areas;
        this.gameManager = gameManager;

        for (CancelableEvent cancelableEvent : CancelableEvent.values()) {
            cancelableEventBooleanMap.put(cancelableEvent, section == null
                    ? cancelableEvent.getDefaultValue()
                    : section.getBoolean(cancelableEvent.getKey(), cancelableEvent.getDefaultValue()));
        }
    }

    @EventHandler
    public void onEntityDamageEvent(@NotNull EntityDamageEvent event) {
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

    private boolean isInsideAreaAndShouldBeCanceled(@NotNull Location location, CancelableEvent event) {
        for (ModifierArea modifierArea : areas) {
            if (modifierArea.isInArea(location)) {
                if (Modifier.getInstance().getTemporaryEvents(event))
                    return true;
            }
        }
        return false;
    }

    private boolean shouldCancel(@NotNull Player player) {
        return player.getPersistentDataContainer().getOrDefault(BYPASS_EVENTS, PersistentDataType.BYTE, (byte) 0) == 0b0;
    }

    @EventHandler
    public void onFoodLevelChangeEvent(@NotNull FoodLevelChangeEvent event) {
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
    public void onBlockPlaceEvent(@NotNull BlockPlaceEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.BLOCK_PLACE)
                || isInsideAreaAndShouldBeCanceled(event.getBlockPlaced().getLocation(), CancelableEvent.BLOCK_PLACE)) {
            if (shouldCancel(event.getPlayer()))
                event.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockBreakEvent(@NotNull BlockBreakEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.BLOCK_BREAK)
                || isInsideAreaAndShouldBeCanceled(event.getBlock().getLocation(), CancelableEvent.BLOCK_BREAK)) {
            if (shouldCancel(event.getPlayer()))
                event.setCancelled(true);
        }

    }

    @EventHandler
    public void onInventoryInteractEvent(@NotNull InventoryInteractEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.INVENTORY_INTERACT)
                || isInsideAreaAndShouldBeCanceled(event.getWhoClicked().getLocation(), CancelableEvent.INVENTORY_INTERACT)) {
            if (event.getWhoClicked() instanceof Player player)
                if (shouldCancel(player))
                    event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);
        if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.RESTART_ON_ITEM_SWAP)) {
            Game game = gameManager.getGame(player);
            if (game != null) {
                Board<?> board = gameManager.getBoard(player);
                if (board.isGenerated()) {
                    gameManager.finishGame(player, false);
                    game.getStarter()
                            .setBombCount(((MinesweeperBoard) board).getBombCount())
                            .setShouldTeleport(false)
                            .setWidth(board.getWidth())
                            .setHeight(board.getHeight())
                            .build(player, MinesweeperBoard.class);
                    event.setCancelled(true);
                }
            }
            event.setCancelled(true);
            return;
        }
        if (cancelableEventBooleanMap.get(CancelableEvent.SWAP_ITEMS)
                || isInsideAreaAndShouldBeCanceled(player.getLocation(), CancelableEvent.SWAP_ITEMS)) {
            if (shouldCancel(player))
                event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerDropItemEvent(@NotNull PlayerDropItemEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.DROP_ITEM)
                || isInsideAreaAndShouldBeCanceled(event.getPlayer().getLocation(), CancelableEvent.DROP_ITEM)) {
            if (shouldCancel(event.getPlayer()))
                event.setCancelled(true);
        }

    }

    @EventHandler
    public void onEntityPickupItemEvent(@NotNull EntityPickupItemEvent event) {
        if (cancelableEventBooleanMap.get(CancelableEvent.PICKUP_ITEM)
                || isInsideAreaAndShouldBeCanceled(event.getEntity().getLocation(), CancelableEvent.PICKUP_ITEM)) {
            if (event.getEntity() instanceof Player player)
                if (shouldCancel(player))
                    event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryMoveItem(@NotNull InventoryMoveItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);
    }

}
