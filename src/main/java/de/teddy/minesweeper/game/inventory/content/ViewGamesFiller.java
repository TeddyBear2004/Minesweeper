package de.teddy.minesweeper.game.inventory.content;

import de.teddy.minesweeper.game.GameManager;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.util.HeadGenerator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
    public Inventories getEInventory() {
        return Inventories.VIEW_GAMES;
    }

    @Override
    public Map<ItemStack, Function<Player, Inventories>> insertEInventoryItems(Inventory inventory) {
        return new HashMap<>();
    }

    @Override
    public Map<ItemStack, Function<Player, Consumer<Inventory>>> insertConsumerItems(Inventory inventory) {
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

    public ItemStack generateBackItem(Map<ItemStack, Function<Player, Consumer<Inventory>>> map) {
        ItemStack itemStack = HeadGenerator.getHeadFromUrl("https://textures.minecraft.net/texture/bd69e06e5dadfd84e5f3d1c21063f2553b2fa945ee1d4d7152fdc5425bc12a9");
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName("Back");
        }

        itemStack.setItemMeta(itemMeta);

        map.put(itemStack, player -> inventory -> {
            //todo implement me
        });

        return itemStack;
    }

    public ItemStack generateNextItem(Map<ItemStack, Function<Player, Consumer<Inventory>>> map) {
        ItemStack itemStack = HeadGenerator.getHeadFromUrl("https://textures.minecraft.net/texture/19bf3292e126a105b54eba713aa1b152d541a1d8938829c56364d178ed22bf");
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName("Next");
        }

        itemStack.setItemMeta(itemMeta);

        map.put(itemStack, player -> inventory -> {
            //todo implement me
        });

        return itemStack;
    }

    public ItemStack generatePlayerView(Player player, Board board, Map<ItemStack, Function<Player, Consumer<Inventory>>> map) {
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
