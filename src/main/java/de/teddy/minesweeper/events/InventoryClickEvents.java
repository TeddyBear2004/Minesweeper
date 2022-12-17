package de.teddy.minesweeper.events;

import de.teddy.minesweeper.game.inventory.Inventories;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

public class InventoryClickEvents implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();

        if (!Inventories.isValidInventory(event.getView()))
            return;

        event.setCancelled(true);

        Consumer<Inventory> runnable = Inventories.getConsumer(event.getCurrentItem(), ((Player) event.getWhoClicked()));
        if (runnable != null)
            runnable.accept(clickedInventory);
    }
}
