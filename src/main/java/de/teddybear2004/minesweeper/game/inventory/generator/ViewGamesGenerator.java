package de.teddybear2004.minesweeper.game.inventory.generator;

import com.mojang.datafixers.util.Pair;
import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.util.HeadGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ViewGamesGenerator extends InventoryGenerator {

    private static final int INVENTORY_SIZE = 54;
    private final static int START_SLOT = 0;
    private final static int END_SLOT = INVENTORY_SIZE - 9 - 1;

    public ViewGamesGenerator(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public @NotNull Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> insertConsumerItems(@NotNull Inventory inventory, InventoryManager manager) {
        Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map = new HashMap<>();

        Map<Player, Board> runningGames = getGameManager().getRunningGames();
        List<Player> players = new ArrayList<>(runningGames.keySet());

        ItemStack itemStack = new ItemStack(Material.AIR);
        for (int i = START_SLOT; i < END_SLOT; i++) inventory.setItem(i, itemStack);

        for (int i = START_SLOT, s = 0; i < END_SLOT && s < runningGames.size(); i++, s++) {
            Player player = players.get(s);
            inventory.setItem(i, generatePlayerView(player, runningGames.get(player), map, manager));
        }

        return map;
    }

    @Override
    public int getSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "Watch other games!";
    }

    public @NotNull ItemStack generatePlayerView(@NotNull Player player, @NotNull Board board, @NotNull Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map, InventoryManager manager) {
        Pair<Integer, ItemStack> integerItemStackPair = manager.insertItemId(HeadGenerator.getHeadFromPlayerProfile(player.getPlayerProfile()));

        ItemMeta itemMeta = integerItemStackPair.getSecond().getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(player.getDisplayName() + "'s Game");

            ItemStack stack = board.getGame().getItemStack();

            ItemMeta itemMeta1 = stack.getItemMeta();
            if (itemMeta1 != null)
                itemMeta.setLore(itemMeta1.getLore());
        }
        integerItemStackPair.getSecond().setItemMeta(itemMeta);

        map.put(integerItemStackPair.getFirst(), player1 -> (inventory, clickType) -> {
            board.getGame().startViewing(player1, board);
            board.draw(Collections.singletonList(player1));
            player1.closeInventory();
        });

        return integerItemStackPair.getSecond();
    }

}
