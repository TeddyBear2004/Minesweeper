package de.teddybear2004.retro.games.events;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.inventory.generator.ChooseGameGenerator;
import de.teddybear2004.retro.games.game.inventory.generator.SettingsGenerator;
import de.teddybear2004.retro.games.game.inventory.generator.ViewGamesGenerator;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class GenericRightClickEvent implements Listener {

    private final GameManager gameManager;
    private final InventoryManager manager;

    public GenericRightClickEvent(GameManager gameManager, InventoryManager manager) {
        this.gameManager = gameManager;
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (Objects.equals(event.getHand(), EquipmentSlot.OFF_HAND) || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        ItemStack itemStack = event.getItem();

        Game game = gameManager.getGame(player);

        if (itemStack != null) {
            if (game != null) {
                if (itemStack.equals(InventoryManager.PlayerInventory.Items.RELOAD.getItemStack())) {
                    Board<?> board = gameManager.getBoard(player);
                    if (board.isGenerated()) {
                        gameManager.finishGame(player, false);
                        game.getStarter()
                                .setBombCount(((MinesweeperBoard) board).getBombCount())
                                .setShouldTeleport(false)
                                .setWidth(board.getWidth())
                                .setHeight(board.getHeight())
                                .build(player, MinesweeperBoard.class);
                        event.setCancelled(true);
                    }
                    return;
                } else if (itemStack.equals(InventoryManager.PlayerInventory.Items.LEAVE.getItemStack())) {
                    gameManager.finishGame(player, false);
                    InventoryManager.PlayerInventory.VIEWER.apply(player);
                    event.setCancelled(true);
                    return;
                }
            }

            if (itemStack.equals(InventoryManager.PlayerInventory.Items.WATCH_OTHER.getItemStack())) {
                player.openInventory(manager.getInventory(ViewGamesGenerator.class, player));
                event.setCancelled(true);
            } else if (itemStack.equals(InventoryManager.PlayerInventory.Items.START.getItemStack())) {
                player.openInventory(manager.getInventory(ChooseGameGenerator.class, player));
                event.setCancelled(true);
            } else if (itemStack.equals(InventoryManager.PlayerInventory.Items.SETTINGS.getItemStack())) {
                player.openInventory(manager.getInventory(SettingsGenerator.class, player));
                event.setCancelled(true);
            }
        }

        Block block = event.getClickedBlock();
        if (block != null && !gameManager.isInside(block.getLocation()))
            event.setCancelled(true);
    }

}
