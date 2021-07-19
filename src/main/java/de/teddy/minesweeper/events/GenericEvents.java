package de.teddy.minesweeper.events;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.Inventories;
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
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GenericEvents implements Listener {
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event){
        event.getEntity().setInvulnerable(true);
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event){
        event.setFoodLevel(20);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        event.setJoinMessage("");
        event.getPlayer().getInventory().setContents(Inventories.viewerInventory);
        event.getPlayer().setAllowFlight(true);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        Game game = Game.getGame(event.getPlayer());
        if(game != null)
            game.finishGame(event.getPlayer());
        event.setQuitMessage("");
    }


    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event){
        if(!event.getPlayer().isOp() || Game.getGame(event.getPlayer()) != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event){
        if(!event.getPlayer().isOp() || Game.getGame(event.getPlayer()) != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryInteractEvent(InventoryInteractEvent event){
        if(event.getWhoClicked() instanceof Player)
        if(!event.getWhoClicked().isOp() || Game.getGame((Player)event.getWhoClicked()) != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event){
        event.getPlayer().setAllowFlight(true);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event){
        if(!event.getPlayer().isOp() || Game.getGame(event.getPlayer()) != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event){
        if(event.getEntity() instanceof Player)
            if(!event.getEntity().isOp() || Game.getGame((Player)event.getEntity()) != null)
                event.setCancelled(true);
    }
}
