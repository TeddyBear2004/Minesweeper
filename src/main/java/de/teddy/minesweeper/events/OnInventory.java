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
		if(event.getView().getTitle().equals(ChatColor.AQUA + Minesweeper.getLanguage().getString("minesweeper"))){
			event.setCancelled(true);
			ItemStack currentItem = event.getCurrentItem();
			if(currentItem == null)
				return;

			ItemMeta itemMeta = currentItem.getItemMeta();
			if(itemMeta == null)
				return;

			String displayName = itemMeta.getDisplayName();

			// Find the Minesweeper game with the matching difficulty name, if any
			Game game = findMinesweeperGameByDifficultyName(displayName);
			if (game != null) {
				game.startGame(((Player)event.getWhoClicked()));
			}
		}
	}

	private Game findMinesweeperGameByDifficultyName(String difficultyName) {
		return Minesweeper.getGames()
				.stream()
				.filter(game -> difficultyName.equals(game.getDifficultyName()))
				.findFirst()
				.orElse(null);
	}
}
