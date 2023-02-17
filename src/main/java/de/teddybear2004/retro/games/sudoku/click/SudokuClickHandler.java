package de.teddybear2004.retro.games.sudoku.click;

import com.comphenix.protocol.wrappers.BlockPosition;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.click.ClickHandler;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.sudoku.SudokuField;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SudokuClickHandler implements ClickHandler<SudokuField, Board<SudokuField>> {

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
    @Override
    public void leftClick(@Nullable Player player, @NotNull Game game, @NotNull BlockPosition blockPosition, @NotNull Board<SudokuField> board, @Nullable SudokuField field, @NotNull Location location) {
        if (board.isFinished())
            return;

        if (board.getPlayer().equals(player)) {
            board.startStarted();

            if (field == null) {
                try{
                    board.checkField(location.getBlockX(), location.getBlockZ());
                }catch(IllegalArgumentException ignore){
                }

                board.draw();
                return;
            }
            if (!field.isStartValue()) {
                int heldItemSlot = player.getInventory().getHeldItemSlot();
                field.setNumber(heldItemSlot + 1);
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
    @Override
    public void rightClick(@NotNull Player player, @NotNull Board<SudokuField> board, @Nullable SudokuField field, @Nullable Cancellable cancellable) {
        if (field == null)
            return;

        if (cancellable != null)
            cancellable.setCancelled(true);

        InventoryManager.PlayerInventory.GAME.apply(player);

        if (board.isFinished() || field.isStartValue())
            return;

        int heldItemSlot = player.getInventory().getHeldItemSlot();
        field.setNumber(heldItemSlot + 1);

        board.draw();
    }

}
