package de.teddy.minesweeper.game.click;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.SurfaceDiscoverer;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.HashMap;
import java.util.Map;

public class ClickHandler {

    private final Map<Player, Long> lastLeftClick = new HashMap<>();
    private final Map<Player, Long> lastRightClick = new HashMap<>();
    private final Map<Player, Long> lastLastRightClick = new HashMap<>();

    public void leftClick(Player player, Game game, BlockPosition blockPosition, Board board, Board.Field field, Location location) {
        if (board.isFinished())
            return;

        if (board.getPlayer().equals(player)) {
            board.startStarted();

            try{
                if (field == null) {
                    try{
                        board.checkField(location.getBlockX(), location.getBlockZ());
                    }catch(IllegalArgumentException ignore){
                    }

                    board.draw();
                    return;
                }

                if (field.isMarked()) {
                    if (location.getBlockY() - game.getFieldHeight() == 1)
                        PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(field.getMark()));

                    return;
                }

                PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

                long l = System.currentTimeMillis();

                if (field.isCovered()) {
                    board.checkField(location.getBlockX(), location.getBlockZ());
                } else if (l - lastLeftClick.getOrDefault(player, (long) -1000) <= personalModifier.getDoubleClickDuration().orElse(350)) {
                    if (personalModifier.isRevealOnDoubleClick().orElse(true)) {
                        board.checkNumber(location.getBlockX(), location.getBlockZ());
                    } else {
                        board.highlightBlocksAround(field);
                    }
                }

                if (personalModifier.isEnableDoubleClick().orElse(false))
                    lastLeftClick.put(player, l);
            }catch(BombExplodeException e){
                board.lose();
            }

            board.checkIfWon();
        }

        board.draw();
    }

    public void rightClick(Player player, Board board, Board.Field field, Cancellable cancellable) {
        if (field == null)
            return;

        cancellable.setCancelled(true);
        player.getInventory().setContents(Inventories.GAME_INVENTORY);

        if (board.isFinished())
            return;

        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);
        long l = System.currentTimeMillis();
        board.startStarted();

        if (field.isCovered())
            field.reverseMark();
        else if (personalModifier.isUseMultiFlag().orElse(false)) {
            if (l - lastLastRightClick.getOrDefault(player, (long) -1000) <= 700)
                SurfaceDiscoverer.flagFieldsNextToNumber(board, field.getX(), field.getY(), false);
            else if (l - lastRightClick.getOrDefault(player, (long) -1000) <= personalModifier.getDoubleClickDuration().orElse(350))
                SurfaceDiscoverer.flagFieldsNextToNumber(board, field.getX(), field.getY(), true);

        }

        if (personalModifier.isEnableDoubleClick().orElse(false)) {
            lastLastRightClick.put(player, lastRightClick.getOrDefault(player, (long) -1000));
            lastRightClick.put(player, l);
        }


        board.draw();
    }

}
