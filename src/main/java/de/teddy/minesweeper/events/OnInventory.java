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

			Game.games
					.stream().filter(game -> displayName.equals(game.getDifficultyName()))
					.findFirst().ifPresent(game -> game.startGame(((Player)event.getWhoClicked())));
		}
	}
}
