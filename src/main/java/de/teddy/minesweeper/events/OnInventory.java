package de.teddy.minesweeper.events;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Game;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class OnInventory implements Listener {
    @EventHandler
    public void onInventoryRightClick(InventoryClickEvent event){
        if(event.getView().getTitle().equals(ChatColor.AQUA + Minesweeper.language.getString("minesweeper"))){
            event.setCancelled(true);
            ItemStack currentItem = event.getCurrentItem();
            if(currentItem == null)
                return;

            ItemMeta itemMeta = currentItem.getItemMeta();
            if(itemMeta == null)
                return;

            String displayName = itemMeta.getDisplayName();

            if(event.getWhoClicked() instanceof Player){
                if(displayName.equals(ChatColor.GREEN + Minesweeper.language.getString("difficulty_easy"))){
                    Game.MAP10X10.startGame((Player)event.getWhoClicked());
                }else if(displayName.equals(ChatColor.YELLOW + Minesweeper.language.getString("difficulty_normal"))){
                    Game.MAP18X18.startGame((Player)event.getWhoClicked());
                }else if(displayName.equals(ChatColor.RED + Minesweeper.language.getString("difficulty_hard"))){
                    Game.MAP24X24.startGame((Player)event.getWhoClicked());
                }
            }
        }
    }
}
