package de.teddybear2004.minesweeper.game;

import de.teddybear2004.minesweeper.game.exceptions.BombExplodeException;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TutorialGame extends Game {

    private final Field[] @NotNull [] board;
    private final Field startField;

    public TutorialGame(Plugin plugin, GameManager gameManager, @NotNull Language language, Location corner, Location spawn, Field[] @NotNull [] board, String name, @NotNull Material material, int inventoryPosition, Field startField) {
        super(plugin, gameManager, language, null, corner, spawn, board.length, board[0].length, determineBombCount(board), name, material, inventoryPosition);
        this.board = board;
        this.startField = startField;
    }

    private static int determineBombCount(Field[] @NotNull [] board) {
        int count = 0;

        for (Field[] fields : board) {
            for (Field field : fields) {
                if (field.isBomb()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public @Nullable Board startGame(@NotNull Player p, boolean shouldTeleport, int bombCount, int width, int height, long seed, boolean setSeed, boolean saveStats) {
        Board board = super.startGame(p, shouldTeleport, bombCount, width, height, seed, true, false);

        if (board == null)
            return null;

        board.generateBoard(this.board);
        try{
            board.checkField(startField.getX() + board.getCorner().getBlockX(), startField.getY() + board.getCorner().getBlockZ(), false);
        }catch(BombExplodeException ignored){
        }

        return board;
    }
}