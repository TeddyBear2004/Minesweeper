package de.teddy.minesweeper.events;

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
        if(event.getView().getTitle().equals(ChatColor.AQUA + "Minesweeper")){
            event.setCancelled(true);
            ItemStack currentItem = event.getCurrentItem();
            if(currentItem == null)
                return;

            ItemMeta itemMeta = currentItem.getItemMeta();
            if(itemMeta == null)
                return;

            String displayName = itemMeta.getDisplayName();

            if(event.getWhoClicked() instanceof Player){
                if(displayName.equals(ChatColor.GREEN + "Einfach")){
                    Game.MAP10X10.requestGame((Player)event.getWhoClicked());
                }else if(displayName.equals(ChatColor.YELLOW + "Mittel")){
                    Game.MAP18X18.requestGame((Player)event.getWhoClicked());
                }else if(displayName.equals(ChatColor.RED + "Schwer")){
                    Game.MAP24X24.requestGame((Player)event.getWhoClicked());
                }
            }
        }
    }
}
