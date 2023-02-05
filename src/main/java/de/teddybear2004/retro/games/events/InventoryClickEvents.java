package de.teddybear2004.retro.games.events;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.statistic.GameStatistic;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
import de.teddybear2004.retro.games.minesweeper.exceptions.BombExplodeException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class InventoryClickEvents implements Listener {

    private final GameManager gameManager;
    private final InventoryManager inventoryManager;

    /**
     * @param gameManager The game manger to start games.
     */
    @Contract(pure = true)
    public InventoryClickEvents(GameManager gameManager, InventoryManager inventoryManager) {
        this.gameManager = gameManager;
        this.inventoryManager = inventoryManager;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (handleStats(event))
            return;

        Inventory clickedInventory = event.getClickedInventory();
        event.setCancelled(true);

        if (!inventoryManager.isValidInventory(event.getView())) {
            return;
        }

        BiConsumer<Inventory, ClickType> runnable = inventoryManager.getConsumer(event.getCurrentItem());
        if (runnable != null)
            runnable.accept(clickedInventory, event.getClick());
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
                        .build(player, MinesweeperBoard.class);
                Board<?> board = gameManager.getBoard(player);
                try{
                    board.checkField(x + board.getCorner().getBlockX(), y + board.getCorner().getBlockZ(), false);
                    board.draw();
                }catch(BombExplodeException ignored){
                }
            }
        }

        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        inventoryManager.onClose(event.getInventory());
    }

}
