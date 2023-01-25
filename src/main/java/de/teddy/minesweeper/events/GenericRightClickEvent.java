package de.teddy.minesweeper.events;

import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.GameManager;
import de.teddy.minesweeper.game.inventory.Inventories;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class GenericRightClickEvent implements Listener {

    private final GameManager gameManager;

    public GenericRightClickEvent(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (Objects.equals(event.getHand(), EquipmentSlot.OFF_HAND) || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
            return;
        ItemStack itemStack = event.getItem();

        Game game = gameManager.getGame(event.getPlayer());

        if (itemStack != null) {
            if (game != null) {
                if (itemStack.equals(Inventories.reload)) {
                    Board board = gameManager.getBoard(event.getPlayer());
                    if (board.isGenerated()) {
                        gameManager.finishGame(event.getPlayer(), false);
                        game.getStarter()
                                .setBombCount(board.getBombCount())
                                .setShouldTeleport(false)
                                .setWidth(board.getWidth())
                                .setHeight(board.getHeight())
                                .build(event.getPlayer());
                        event.setCancelled(true);
                    }
                    return;
                } else if (itemStack.equals(Inventories.barrier)) {
                    gameManager.finishGame(event.getPlayer(), false);
                    event.getPlayer().getInventory().setContents(Inventories.VIEWER_INVENTORY);
                    event.setCancelled(true);
                    return;
                }
            }

            if (itemStack.equals(Inventories.compass)) {
                event.getPlayer().openInventory(Inventories.VIEW_GAMES.getInventory());
                event.setCancelled(true);
            } else if (itemStack.equals(Inventories.hourGlass)) {
                event.getPlayer().openInventory(Inventories.CHOOSE_GAME.getInventory());
                event.setCancelled(true);
            }
        }

        event.setCancelled(true);
    }

}
