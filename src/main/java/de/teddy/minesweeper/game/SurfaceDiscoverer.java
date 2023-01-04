package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.exceptions.BombExplodeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class SurfaceDiscoverer {

    private static final List<int[]> INTS = new ArrayList<>();

    static {
        INTS.add(new int[]{-1, -1});
        INTS.add(new int[]{-1, +0});
        INTS.add(new int[]{-1, +1});
        INTS.add(new int[]{+0, -1});
        INTS.add(new int[]{+0, +0});
        INTS.add(new int[]{+0, +1});
        INTS.add(new int[]{+1, -1});
        INTS.add(new int[]{+1, +0});
        INTS.add(new int[]{+1, +1});
    }

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
                Stack<Board.Field> stack = new Stack<>();
                stack.push(field);

                while (!stack.isEmpty()) {
                    Board.Field current = stack.pop();

                    INTS.parallelStream().forEach(ints -> {
                        int i = ints[0];
                        int j = ints[1];

                        Board.Field relativeTo = current.getRelativeTo(i, j);
                        if (relativeTo == null || !relativeTo.isCovered() || relativeTo.isMarked())
                            return;

                        relativeTo.setUncover();
                        if (relativeTo.getNeighborCount() != 0)
                            return;

                        stack.push(relativeTo);
                    });
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
            AtomicInteger markedFields = new AtomicInteger(0);

            INTS.parallelStream().forEach(ints -> {
                int i = ints[0];
                int j = ints[1];

                if (width + i >= 0 && width + i < board.getBoard().length && height + j >= 0 && height + j < board.getBoard()[0].length) {
                    if (board.getBoard()[width + i][height + j].isMarked()) {
                        markedFields.incrementAndGet();
                    }
                }
            });

            if (field.getNeighborCount() == markedFields.get()) {
                for (int[] ints : INTS) {
                    int i = ints[0];
                    int j = ints[1];

                    if (width + i >= 0 && width + i < board.getBoard().length && height + j >= 0 && height + j < board.getBoard()[0].length && !board.getBoard()[width + i][height + j].isMarked()) {
                        uncoverFields(board, width + i, height + j);
                    }
                }
            }
        }
    }

    public static void flagFieldsNextToNumber(Board board, int width, int height, boolean place) {
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length)
            throw new IllegalArgumentException();

        Board.Field field = board.getField(width, height);
        if (field.isCovered())
            return;

        INTS.parallelStream().forEach(ints -> {
            int i = ints[0];
            int j = ints[1];

            if (width + i >= 0 && width + i < board.getBoard().length && height + j >= 0 && height + j < board.getBoard()[0].length) {
                Board.Field field1 = field.getRelativeTo(i, j);

                if (field1.isCovered())
                    field1.setMark(place ? MarkType.BOMB_MARK : MarkType.NONE);
            }
        });

    }

}
