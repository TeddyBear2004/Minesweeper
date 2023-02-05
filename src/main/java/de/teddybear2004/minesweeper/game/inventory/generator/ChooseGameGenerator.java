package de.teddybear2004.minesweeper.game.inventory.generator;

import com.mojang.datafixers.util.Pair;
import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.MinesweeperBoard;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ChooseGameGenerator extends InventoryGenerator {


    public ChooseGameGenerator(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public @NotNull Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> insertConsumerItems(@NotNull Inventory inventory, InventoryManager inventoryManager) {
        Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> itemStackFunctionHashMap = new HashMap<>();

        for (Game game : getGameManager().getGames()) {
            if (game.getInventoryPosition() < 0)
                continue;

            Pair<Integer, ItemStack> integerItemStackPair = inventoryManager.insertItemId(game.getItemStack());

            inventory.setItem(game.getInventoryPosition(), integerItemStackPair.getSecond());
            itemStackFunctionHashMap.put(integerItemStackPair.getFirst(), player ->
                    (inventory1, clickType) -> game.getStarter().build(player, MinesweeperBoard.class));
        }

        return itemStackFunctionHashMap;
    }

    @Override
    public int getSize() {
        return Minesweeper.getPlugin(Minesweeper.class).getChooseGameLines() * 9;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + Minesweeper.getPlugin(Minesweeper.class).getLanguage().getString("minesweeper");
    }


}
