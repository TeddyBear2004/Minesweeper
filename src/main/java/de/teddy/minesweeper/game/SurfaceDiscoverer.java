package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.exceptions.BombExplodeException;

import java.util.Stack;

public class SurfaceDiscoverer {

    /**
     * Uncovers a field on a Minesweeper board and, if the field is a number field with no neighboring bombs,
     * recursively uncovers all adjacent fields.
     *
     * @param board  the board on which to uncover the field
     * @param width  the width coordinate of the field to uncover
     * @param height the height coordinate of the field to uncover
     * @throws BombExplodeException if a bomb is uncovered
     */
    public static void uncoverFields(Board board, int width, int height) throws BombExplodeException {
        // Check if the given coordinates are within the bounds of the board
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length) {
            throw new IllegalArgumentException();
        }

        Board.Field field = board.getField(width, height);
        if (field.isCovered()) {
            field.setUncover();

            if (field.isBomb()) {
                throw new BombExplodeException("Bomb at " + width + " and " + height + " is exploded.");

            }

            if (field.getNeighborCount() == 0) {
                Stack<int[]> stack = new Stack<>();
                stack.push(new int[]{width, height});

                while (!stack.isEmpty()) {
                    int[] coordinates = stack.pop();
                    int x = coordinates[0];
                    int y = coordinates[1];

                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            if (!(i == 0 && j == 0)) {
                                // Ignore fields outside the board
                                if (x + i >= 0 && x + i < board.getBoard().length && y + j >= 0 && y + j < board.getBoard()[0].length) {
                                    Board.Field neighborField = board.getField(x + i, y + j);
                                    if (neighborField.isCovered()) {
                                        neighborField.setUncover();
                                        if (neighborField.getNeighborCount() == 0) {
                                            stack.push(new int[]{x + i, y + j});
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Uncovers the fields next to a given number field on a Minesweeper board.
     *
     * @param board  the board on which to uncover the fields
     * @param width  the width coordinate of the number field
     * @param height the height coordinate of the number field
     * @throws BombExplodeException if a bomb is uncovered
     */
    public static void uncoverFieldsNextToNumber(Board board, int width, int height) throws BombExplodeException {
        // Check if the given coordinates are within the bounds of the board
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length) {
            throw new IllegalArgumentException();
        }

        Board.Field field = board.getField(width, height);
        if (!field.isCovered()) {
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

            if (field.getNeighborCount() == markedFields) {
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

}
