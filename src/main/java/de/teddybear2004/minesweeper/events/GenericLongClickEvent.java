package de.teddybear2004.minesweeper.events;

import com.comphenix.protocol.wrappers.BlockPosition;
import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.Field;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.click.ClickHandler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class GenericLongClickEvent implements Listener {

    private final GameManager manager;
    private final ClickHandler clickHandler;

    public GenericLongClickEvent(GameManager manager, ClickHandler clickHandler) {
        this.manager = manager;
        this.clickHandler = clickHandler;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Block targetBlockExact = event.getPlayer().getTargetBlockExact(35);

            Game game = manager.getGame(event.getPlayer());
            Board board = manager.getBoard(event.getPlayer());
            if (game == null || board == null || targetBlockExact == null)
                return;

            BlockPosition blockPosition = new BlockPosition(targetBlockExact.getX(), targetBlockExact.getY(), targetBlockExact.getZ());
            Location location = blockPosition.toLocation(board.getCorner().getWorld());
            Field field = board.getField(location);

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
