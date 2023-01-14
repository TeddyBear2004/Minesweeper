package de.teddy.minesweeper.game.click;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Field;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.SurfaceDiscoverer;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ClickHandler {

    private final Map<Player, Long> lastLeftClick = new HashMap<>();
    private final Map<Player, Long> lastRightClick = new HashMap<>();
    private final Map<Player, Long> lastLastRightClick = new HashMap<>();

    /**
     * This method performs a left click action on the given board.
     *
     * @param player        The player who clicked.
     * @param game          The game this happened on.
     * @param blockPosition The position of the clicked block
     * @param board         The board this click happened on.
     * @param field         The field on the board where the click happened.
     * @param location      The location where this click happened.
     */
    public void leftClick(@Nullable Player player, @NotNull Game game, @NotNull BlockPosition blockPosition, @NotNull Board board, @Nullable Field field, @NotNull Location location) {
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
                } else if (l - lastLeftClick.getOrDefault(player, (long) -1000) <= personalModifier.<Integer>get(PersonalModifier.ModifierType.DOUBLE_CLICK_DURATION).orElse(350)) {
                    if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.REVEAL_ON_DOUBLE_CLICK).orElse(true)) {
                        board.checkNumber(location.getBlockX(), location.getBlockZ());
                    } else {
                        board.highlightBlocksAround(field);
                    }
                }

                if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK).orElse(false))
                    lastLeftClick.put(player, l);
            }catch(BombExplodeException e){
                board.lose();
            }

            board.checkIfWon();
        }

        board.draw();
    }

    /**
     * This method performs a right click action on the given board.
     *
     * @param player      The player who clicked
     * @param board       The board this click happened on
     * @param field       The field this happened on.
     * @param cancellable A cancellable which is most likely an event.
     */
    public void rightClick(@NotNull Player player, @NotNull Board board, @Nullable Field field, @NotNull Cancellable cancellable) {
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
        else if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.USE_MULTI_FLAG).orElse(false)) {
            if (l - lastLastRightClick.getOrDefault(player, (long) -1000) <= 700)
                SurfaceDiscoverer.flagFieldsNextToNumber(board, field.getX(), field.getY(), false);
            else if (l - lastRightClick.getOrDefault(player, (long) -1000) <= personalModifier.<Integer>get(PersonalModifier.ModifierType.DOUBLE_CLICK_DURATION).orElse(350))
                SurfaceDiscoverer.flagFieldsNextToNumber(board, field.getX(), field.getY(), true);

        }

        if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK).orElse(false)) {
            lastLastRightClick.put(player, lastRightClick.getOrDefault(player, (long) -1000));
            lastRightClick.put(player, l);
        }


        board.draw();
    }

}
