package de.teddy.minesweeper.game.inventory.content;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MainMenuFiller implements ContentFiller {

    private final List<Game> games;

    public MainMenuFiller(List<Game> games) {
        this.games = games;
    }

    @Override
    public @NotNull Inventories getEInventory() {
        return Inventories.CHOOSE_GAME;
    }

    @Override
    public @NotNull Map<ItemStack, Function<Player, Inventory>> insertInventoryItems(Inventory inventory) {
        return new HashMap<>();
    }

    @Override
    public @NotNull Map<ItemStack, Function<Player, Inventories>> insertEInventoryItems(Inventory inventory) {
        return new HashMap<>();
    }

    @Override
    public @NotNull Map<ItemStack, Function<Player, Consumer<Inventory>>> insertConsumerItems(@NotNull Inventory inventory) {
        HashMap<ItemStack, Function<Player, Consumer<Inventory>>> itemStackFunctionHashMap = new HashMap<>();

        for (Game game : games) {
            if (game.getInventoryPosition() < 0)
                continue;
            inventory.setItem(game.getInventoryPosition(), game.getItemStack());
            itemStackFunctionHashMap.put(game.getItemStack(), player -> {
                game.getStarter()
                        .build(player);
                return null;
            });
        }

        return itemStackFunctionHashMap;
    }


}
