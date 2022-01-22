package de.teddy.minesweeper.events;

import de.teddy.minesweeper.game.Board;
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
        event.getPlayer().getInventory().setContents(Inventories.viewerInventory);
        event.getPlayer().setAllowFlight(true);
        event.getPlayer().setResourcePack("https://cdn.discordapp.com/attachments/676083915382849576/875365210997784587/teddy.zip");

        boolean watching = false;
        for(Game map : Game.values()){
            Board runningGame = map.getRunningGame();
            if(runningGame != null){
                map.startViewing(event.getPlayer(), runningGame);
                watching = true;
                break;
            }
        }
        if(!watching){
            Game.games.get(0).startViewing(event.getPlayer(), null);
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        Game game = Game.getGame(event.getPlayer());
        if(game != null){
            Game.finishGame(event.getPlayer(), true);
            Board board = Game.getBoard(event.getPlayer());
            if(board != null)
                board.breakGame();
        }
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
