package de.teddybear2004.minesweeper.game;

import de.teddybear2004.minesweeper.game.exceptions.BombExplodeException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class SurfaceDiscoverer {

    public static final List<int[]> SURROUNDINGS = new ArrayList<>();

    static {
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                if (!(i == 0 && j == 0))
                    SURROUNDINGS.add(new int[]{i, j});
    }

    /**
     * Uncovers the fields next to a given number field on a Minesweeper board.
     *
     * @param board  the board on which to uncover the fields
     * @param width  the width coordinate of the number field
     * @param height the height coordinate of the number field
     * @throws BombExplodeException if a bomb is uncovered
     */
    public static void uncoverFieldsNextToNumber(@NotNull Board board, int width, int height) throws BombExplodeException {
        // Check if the given coordinates are within the bounds of the board
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length) {
            throw new IllegalArgumentException();
        }

        Field field = board.getField(width, height);
        if (field == null || field.isCovered())
            return;


        AtomicInteger markedFields = new AtomicInteger(0);

        SURROUNDINGS.parallelStream().forEach(ints -> {
            int i = ints[0];
            int j = ints[1];

            if (width + i >= 0 && width + i < board.getBoard().length && height + j >= 0 && height + j < board.getBoard()[0].length) {
                if (board.getBoard()[width + i][height + j].isMarked()) {
                    markedFields.incrementAndGet();
                }
            }
        });

        if (field.getNeighborCount() != markedFields.get())
            return;

        for (int[] ints : SURROUNDINGS) {
            int i = ints[0];
            int j = ints[1];

            if (width + i >= 0 && width + i < board.getBoard().length && height + j >= 0 && height + j < board.getBoard()[0].length && !board.getBoard()[width + i][height + j].isMarked()) {
                uncoverFields(board, width + i, height + j);
            }
        }

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
    public static void uncoverFields(@NotNull Board board, int width, int height) throws BombExplodeException {
        // Check if the given coordinates are within the bounds of the board
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length) {
            throw new IllegalArgumentException();
        }

        Field field = board.getField(width, height);
        if (field == null || !field.isCovered()) return;

        field.setUncover();
        if (field.isBomb())
            throw new BombExplodeException("Bomb at " + width + " and " + height + " is exploded.");

        if (field.getNeighborCount() != 0) return;

        Stack<Field> stack = new Stack<>();
        stack.push(field);

        while (!stack.isEmpty()) {
            Field current = stack.pop();

            SURROUNDINGS.parallelStream().forEach(ints -> {
                int i = ints[0];
                int j = ints[1];

                Field relativeTo = current.getRelativeTo(i, j);
                if (relativeTo == null || !relativeTo.isCovered() || relativeTo.isMarked())
                    return;

                relativeTo.setUncover();
                if (relativeTo.getNeighborCount() != 0)
                    return;

                stack.push(relativeTo);
            });
        }
    }

    public static void flagFieldsNextToNumber(@NotNull Board board, int width, int height, boolean place) {
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length)
            throw new IllegalArgumentException();

        Field field = board.getField(width, height);
        if (field == null || field.isCovered())
            return;

        SURROUNDINGS.parallelStream().forEach(ints -> {
            int i = ints[0];
            int j = ints[1];


            Field field1 = field.getRelativeTo(i, j);
            if (field1 != null && field1.isCovered())
                field1.setMark(place ? MarkType.BOMB_MARK : MarkType.NONE);
        });

    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public static int calculate3BV(@NotNull Board board) {
        return -1; //todo implement me
    }

}
