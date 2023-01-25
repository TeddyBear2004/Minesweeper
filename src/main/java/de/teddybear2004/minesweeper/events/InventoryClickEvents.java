package de.teddybear2004.minesweeper.events;

import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.exceptions.BombExplodeException;
import de.teddybear2004.minesweeper.game.inventory.Inventories;
import de.teddybear2004.minesweeper.game.statistic.GameStatistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class InventoryClickEvents implements Listener {

    private final GameManager gameManager;

    /**
     * @param gameManager The game manger to start games.
     */
    @Contract(pure = true)
    public InventoryClickEvents(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (handleStats(event))
            return;

        Inventory clickedInventory = event.getClickedInventory();

        if (!Inventories.isValidInventory(event.getView())) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        Consumer<Inventory> runnable = Inventories.getConsumer(event.getCurrentItem(), ((Player) event.getWhoClicked()));
        if (runnable != null)
            runnable.accept(clickedInventory);
    }

    public boolean handleStats(@NotNull InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null)
            return false;

        ItemMeta itemMeta = currentItem.getItemMeta();
        if (itemMeta == null)
            return false;

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

        String map = persistentDataContainer.get(GameStatistic.MAP_KEY, PersistentDataType.STRING);
        Long seed = persistentDataContainer.get(GameStatistic.SEED_KEY, PersistentDataType.LONG);
        Integer x = persistentDataContainer.get(GameStatistic.X_KEY, PersistentDataType.INTEGER);
        Integer y = persistentDataContainer.get(GameStatistic.Y_KEY, PersistentDataType.INTEGER);


        if (map == null || seed == null || x == null || y == null)
            return false;

        for (Game game : gameManager.getGames()) {
            if (game.getMap().equalsIgnoreCase(map) && event.getWhoClicked() instanceof Player player) {
                game.getStarter()
                        .setSeed(seed)
                        .setSetSeed(true)
                        .setSaveStats(false)
                        .build(player);
                Board board = gameManager.getBoard(player);
                try{
                    board.checkField(x + board.getCorner().getBlockX(), y + board.getCorner().getBlockZ(), false);
                    board.draw();
                }catch(BombExplodeException ignored){
                }
            }
        }

        return true;
    }

}