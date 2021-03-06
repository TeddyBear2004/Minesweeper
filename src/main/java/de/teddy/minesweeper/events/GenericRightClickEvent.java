package de.teddy.minesweeper.events;

import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.Inventories;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class GenericRightClickEvent implements Listener {
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        if(Objects.equals(event.getHand(), EquipmentSlot.OFF_HAND) || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getItem() == null)
            return;
        ItemStack itemStack = event.getItem();

        Game game = Game.getGame(event.getPlayer());

        if(game != null){
            if(itemStack.equals(Inventories.reload)){
                Board board = Game.getBoard(event.getPlayer());
                if(board.isGenerated()){
                    Game.finishGame(event.getPlayer());
                    game.startGame(event.getPlayer(), false);
                    event.setCancelled(true);
                }
                return;
            }else if(itemStack.equals(Inventories.barrier)){
                Game.finishGame(event.getPlayer());
                event.getPlayer().getInventory().setContents(Inventories.viewerInventory);
                event.setCancelled(true);
                return;
            }
        }

        if(itemStack.equals(Inventories.compass)){
            event.getPlayer().openInventory(Bukkit.createInventory(null, 54));
            //todo inv mit allen laufenden spielen
            event.setCancelled(true);
        }else if(itemStack.equals(Inventories.hourGlass)){
            event.getPlayer().openInventory(Inventories.startCommandInventory);
            event.setCancelled(true);
        }
    }
}
