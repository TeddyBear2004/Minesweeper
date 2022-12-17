package de.teddy.minesweeper.game.inventory.content;

import de.teddy.minesweeper.game.inventory.Inventories;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ContentFiller {
    Inventories getEInventory();

    default Map<ItemStack, Function<Player, Inventory>> insertInventoryItems(Inventory inventory) {
        return new HashMap<>();
    }

    default Map<ItemStack, Function<Player, Inventories>> insertEInventoryItems(Inventory inventory) {
        return new HashMap<>();
    }

    default Map<ItemStack, Function<Player, Consumer<Inventory>>> insertConsumerItems(Inventory inventory) {
        return new HashMap<>();
    }
}
