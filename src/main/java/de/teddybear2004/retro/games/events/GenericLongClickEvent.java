package de.teddybear2004.retro.games.events;

import com.comphenix.protocol.wrappers.BlockPosition;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.click.ClickHandler;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
import de.teddybear2004.retro.games.minesweeper.MinesweeperField;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class GenericLongClickEvent implements Listener {

    private final GameManager manager;
    private final ClickHandler<? super MinesweeperField, ? super Board<MinesweeperField>> clickHandler;

    public GenericLongClickEvent(GameManager manager, ClickHandler<? super MinesweeperField, ? super Board<MinesweeperField>> clickHandler) {
        this.manager = manager;
        this.clickHandler = clickHandler;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Block targetBlockExact = event.getPlayer().getTargetBlockExact(35);

            Game game = manager.getGame(event.getPlayer());
            MinesweeperBoard board = manager.getBoard(event.getPlayer(), MinesweeperBoard.class);
            if (game == null || board == null || targetBlockExact == null)
                return;

            BlockPosition blockPosition = new BlockPosition(targetBlockExact.getX(), targetBlockExact.getY(), targetBlockExact.getZ());
            Location location = blockPosition.toLocation(board.getCorner().getWorld());
            MinesweeperField field = board.getField(location);

            if (event.getAction() == Action.LEFT_CLICK_AIR) {
                clickHandler.leftClick(event.getPlayer(), game,
                                       blockPosition,
                                       board,
                                       field,
                                       location);
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                clickHandler.rightClick(event.getPlayer(),
                                        board,
                                        field,
                                        event);
            }
        }
    }

}
