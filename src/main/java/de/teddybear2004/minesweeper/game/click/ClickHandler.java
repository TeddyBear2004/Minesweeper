package de.teddybear2004.minesweeper.game.click;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddybear2004.minesweeper.game.*;
import de.teddybear2004.minesweeper.game.exceptions.BombExplodeException;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import de.teddybear2004.minesweeper.util.PacketUtil;
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

                PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

                if (field.isMarked()) {
                    if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.BREAK_FLAG)) {
                        field.setMark(MarkType.NONE);
                        board.draw();
                    } else if (location.getBlockY() - game.getFieldHeight() == 1) {
                        PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(field.getMark()));
                    }

                    return;
                }


                long l = System.currentTimeMillis();

                if (field.isCovered()) {
                    board.checkField(location.getBlockX(), location.getBlockZ());
                } else if (l - lastLeftClick.getOrDefault(player, (long) -1000) <= personalModifier.<Integer>get(PersonalModifier.ModifierType.DOUBLE_CLICK_DURATION)) {
                    if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.REVEAL_ON_DOUBLE_CLICK)) {
                        board.checkNumber(location.getBlockX(), location.getBlockZ());
                    } else {
                        board.highlightBlocksAround(field);
                    }
                }

                if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK))
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
    public void rightClick(@NotNull Player player, @NotNull Board board, @Nullable Field field, @Nullable Cancellable cancellable) {
        if (field == null)
            return;

        if (cancellable != null)
            cancellable.setCancelled(true);

        InventoryManager.PlayerInventory.GAME.apply(player);

        if (board.isFinished())
            return;

        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);
        long l = System.currentTimeMillis();
        board.startStarted();

        if (field.isCovered())
            field.reverseMark();
        else if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.USE_MULTI_FLAG)) {
            if (l - lastLastRightClick.getOrDefault(player, (long) -1000) <= 700)
                SurfaceDiscoverer.flagFieldsNextToNumber(board, field.getX(), field.getY(), false);
            else if (l - lastRightClick.getOrDefault(player, (long) -1000) <= personalModifier.<Integer>get(PersonalModifier.ModifierType.DOUBLE_CLICK_DURATION))
                SurfaceDiscoverer.flagFieldsNextToNumber(board, field.getX(), field.getY(), true);

        }

        if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK)) {
            lastLastRightClick.put(player, lastRightClick.getOrDefault(player, (long) -1000));
            lastRightClick.put(player, l);
        }


        board.draw();
    }

}
