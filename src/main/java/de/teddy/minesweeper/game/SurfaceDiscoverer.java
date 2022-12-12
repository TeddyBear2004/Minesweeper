package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;

import java.util.ArrayList;
import java.util.List;

public class SurfaceDiscoverer {

    public static void uncoverFields(Board board, int width, int height) throws BombExplodeException {
        uncoverFields(board, width, height, new ArrayList<>());
    }

    public static void uncoverFieldsNextToNumber(Board board, int width, int height) throws BombExplodeException {
        // Check if the given coordinates are within the bounds of the board
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length) {
            throw new IllegalArgumentException();
        }

        if (!board.getBoard()[width][height].isCovered()) {
            int markedFields = 0;

            // Check the surrounding fields for marked fields
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (!(i == 0 && j == 0)) {
                        // Ignore fields outside the board
                        if (width + i >= 0 && width + i < board.getBoard().length && height + j >= 0 && height + j < board.getBoard()[0].length) {
                            if (board.getBoard()[width + i][height + j].isMarked()) {
                                markedFields++;
                            }
                        }
                    }
                }
            }

            if (board.getBoard()[width][height].getNeighborCount() == markedFields) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (!(i == 0 && j == 0)) {
                            // Ignore fields outside the board and already marked fields
                            if (width + i >= 0 && width + i < board.getBoard().length && height + j >= 0 && height + j < board.getBoard()[0].length && !board.getBoard()[width + i][height + j].isMarked()) {
                                uncoverFields(board, width + i, height + j);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void uncoverFields(Board board, int width, int height, List<String> strings) throws BombExplodeException {
        if (board.getBoard().length <= width || board.getBoard()[0].length <= height) {
            throw new IllegalArgumentException();
        }

        Board.Field field = board.getField(width, height);
        field.setUncover();

        if (field.isBomb()) {
            throw new BombExplodeException(Minesweeper.getLanguage().getString("error_bomb_exploded", String.valueOf(width), String.valueOf(height)));
        }

        if (field.getNeighborCount() == 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (!(i == 0 && j == 0)) {
                        // Ignore fields outside the board
                        if (width + i >= 0 && width + i < board.getBoard().length && height + j >= 0 && height + j < board.getBoard()[0].length) {
                            String s = String.format("%d/%d", width + i, height + j);
                            if (board.getBoard()[width + i][height + j].isCovered() && !strings.contains(s)) {
                                strings.add(s);
                                uncoverFields(board, width + i, height + j, strings);
                            }
                        }
                    }
                }
            }
        }
    }
}
