package de.teddy.minesweeper.events;

import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.statistic.GameStatistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.function.Consumer;

public class InventoryClickEvents implements Listener {

    private final List<Game> games;

    public InventoryClickEvents(List<Game> games) {
        this.games = games;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
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

    public boolean handleStats(InventoryClickEvent event) {
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

        for (Game game : games) {
            if (game.getMap().equalsIgnoreCase(map) && event.getWhoClicked() instanceof Player player) {
                game.getStarter()
                        .setSeed(seed)
                        .setSetSeed(true)
                        .setSaveStats(false)
                        .startGame(player);
                Board board = Game.getBoard(player);
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
