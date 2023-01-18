package de.teddybear2004.minesweeper.game.inventory.content;

import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.inventory.Inventories;
import de.teddybear2004.minesweeper.util.HeadGenerator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ViewGamesFiller implements ContentFiller {

    private static final int INVENTORY_SIZE = 54;
    private final static int START_SLOT = 0;
    private final static int END_SLOT = INVENTORY_SIZE - 9 - 1;
    private final GameManager gameManager;

    public ViewGamesFiller(GameManager gameManager){
        this.gameManager = gameManager;
    }

    @Override
    public @NotNull Inventories getEInventory() {
        return Inventories.VIEW_GAMES;
    }

    @Override
    public @NotNull Map<ItemStack, Function<Player, Inventories>> insertEInventoryItems(Inventory inventory) {
        return new HashMap<>();
    }

    @Override
    public @NotNull Map<ItemStack, Function<Player, Consumer<Inventory>>> insertConsumerItems(@NotNull Inventory inventory) {
        Map<ItemStack, Function<Player, Consumer<Inventory>>> map = new HashMap<>();

        Map<Player, Board> runningGames = gameManager.getRunningGames();
        List<Player> players = new ArrayList<>(runningGames.keySet());

        ItemStack itemStack = new ItemStack(Material.AIR);
        for (int i = START_SLOT; i < END_SLOT; i++) inventory.setItem(i, itemStack);

        for (int i = START_SLOT, s = 0; i < END_SLOT && s < runningGames.size(); i++, s++) {
            Player player = players.get(s);
            inventory.setItem(i, generatePlayerView(player, runningGames.get(player), map));
        }

        return map;
    }

    public @NotNull ItemStack generatePlayerView(@NotNull Player player, @NotNull Board board, @NotNull Map<ItemStack, Function<Player, Consumer<Inventory>>> map) {
        ItemStack itemStack = HeadGenerator.getHeadFromPlayerProfile(player.getPlayerProfile());

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(player.getDisplayName() + "'s Game");

            ItemStack stack = board.getGame().getItemStack();

            ItemMeta itemMeta1 = stack.getItemMeta();
            if (itemMeta1 != null)
                itemMeta.setLore(itemMeta1.getLore());
        }
        itemStack.setItemMeta(itemMeta);

        map.put(itemStack, player1 -> inventory -> {
            board.getGame().startViewing(player1, board);
            board.draw(Collections.singletonList(player1));
            player1.closeInventory();
        });

        return itemStack;
    }

}
