package de.teddybear2004.retro.games.sudoku;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.util.ConnectionBuilder;
import de.teddybear2004.retro.games.util.Language;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SudokuBoard extends Board<SudokuField> {

    private SudokuBackTracker sudokuBackTracker;

    public SudokuBoard(@NotNull Plugin plugin, Language language, ConnectionBuilder connectionBuilder, Game game, Location corner, Player player, long seed, boolean setSeed, boolean saveStats, Atelier atelier) {
        super(plugin, language, connectionBuilder, game, 9, 9, corner, player, seed, setSeed, saveStats, atelier);

        initBoard();
    }

    @Override
    public SudokuField[][] generateBoard(int width, int height) {
        int size = 9;
        int difficulty = 37;

        do{
            this.sudokuBackTracker = new SudokuBackTracker(new Random(getSeed()), 9);
            sudokuBackTracker.generate(0, 0);
            sudokuBackTracker.reduce(difficulty);
        }while (!sudokuBackTracker.clone().hasSingleSolution(0, 0));

        SudokuField[][] sudokuFields = new SudokuField[size][size];

        int[][] board = sudokuBackTracker.getBoard();
        for (int x = 0; x < board.length; x++) {
            int[] ints = board[x];
            for (int y = 0; y < ints.length; y++) {
                int i = ints[y];
                sudokuFields[x][y] = new SudokuField(this, x, y, i, i != 0);
            }
        }

        return sudokuFields;
    }

    @Override
    public void initScoreboard() {

    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends Board> getBoardClass() {
        return SudokuBoard.class;
    }

    @Override
    public InventoryManager.PlayerInventory getPlayerInventory() {
        return InventoryManager.PlayerInventory.SUDOKU;
    }

    @Override
    public void checkIfWon() {
        if (!sudokuBackTracker.isFinishedSuccessful())
            return;

        win();
    }

    @Override
    public void sendWinMessages(String actualTimeNeededString) {
        getAllPlayers().forEach(player -> player.sendMessage(actualTimeNeededString));
    }

    @Override
    public void saveStats(boolean won, boolean saveStats, long time) {

    }

    @Override
    public void updateScoreBoard() {

    }

    @Override
    public int compareTo(@NotNull Board<?> o) {
        return 0;
    }

}
