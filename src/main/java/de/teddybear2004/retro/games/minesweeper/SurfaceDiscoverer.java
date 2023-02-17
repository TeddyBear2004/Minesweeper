package de.teddybear2004.retro.games.minesweeper;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Field;
import de.teddybear2004.retro.games.minesweeper.exceptions.BombExplodeException;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
     * Uncovers the fields next to a given number field on a RetroGames board.
     *
     * @param board  the board on which to uncover the fields
     * @param width  the width coordinate of the number field
     * @param height the height coordinate of the number field
     * @throws BombExplodeException if a bomb is uncovered
     */
    public static void uncoverFieldsNextToNumber(@NotNull MinesweeperBoard board, int width, int height) throws BombExplodeException {
        // Check if the given coordinates are within the bounds of the board
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length) {
            throw new IllegalArgumentException();
        }

        MinesweeperField field = board.getField(width, height);
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
     * Uncovers a field on a RetroGames board and, if the field is a number field with no neighboring bombs,
     * recursively uncovers all adjacent fields.
     *
     * @param board  the board on which to uncover the field
     * @param width  the width coordinate of the field to uncover
     * @param height the height coordinate of the field to uncover
     * @throws BombExplodeException if a bomb is uncovered
     */
    public static <F extends Field<F>> void uncoverFields(@NotNull Board<F> board, int width, int height) throws BombExplodeException {
        // Check if the given coordinates are within the bounds of the board
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length) {
            throw new IllegalArgumentException();
        }


        F field = board.getField(width, height);
        if (!(field instanceof MinesweeperField minesweeperField)) {
            return;
        }

        if (!minesweeperField.isCovered()) return;

        minesweeperField.setUncover();
        if (minesweeperField.isBomb())
            throw new BombExplodeException("Bomb at " + width + " and " + height + " is exploded.");

        if (minesweeperField.getNeighborCount() != 0) return;

        Stack<MinesweeperField> stack = new Stack<>();
        stack.push(minesweeperField);

        while (!stack.isEmpty()) {
            MinesweeperField current = stack.pop();

            SURROUNDINGS.parallelStream().forEach(ints -> {
                int i = ints[0];
                int j = ints[1];

                MinesweeperField relativeTo = current.getRelativeTo(i, j);
                if (relativeTo == null || !relativeTo.isCovered() || relativeTo.isMarked())
                    return;

                relativeTo.setUncover();
                if (relativeTo.getNeighborCount() != 0)
                    return;

                stack.push(relativeTo);
            });
        }
    }

    public static <F extends Field<F>> void flagFieldsNextToNumber(@NotNull Board<F> board, int width, int height, boolean place) {
        if (width < 0 || width >= board.getBoard().length || height < 0 || height >= board.getBoard()[0].length)
            throw new IllegalArgumentException();

        F field = board.getField(width, height);
        if (field == null || (field instanceof MinesweeperField && ((MinesweeperField) field).isCovered()))
            return;

        SURROUNDINGS.parallelStream().forEach(ints -> {
            int i = ints[0];
            int j = ints[1];


            F field1 = field.getRelativeTo(i, j);
            if (field1 != null && field instanceof MinesweeperField minesweeperField)
                if (minesweeperField.isCovered())
                    minesweeperField.setMark(place ? MarkType.BOMB_MARK : MarkType.NONE);
        });

    }

    public static int calculate3BV(boolean[][] board, int[][] ints) {
        int clickCount = 0;
        boolean[][] visited = new boolean[board.length][board[0].length];

        if (board.length != ints.length || board[0].length != ints[0].length)
            return -1;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j])
                    continue;

                Pair<Integer, Integer> ints3 = Pair.of(i, j);
                if (visited[i][j])
                    continue;

                if (ints[i][j] == 0) {
                    Queue<Pair<Integer, Integer>> stack = new LinkedList<>();
                    stack.offer(ints3);

                    while (!stack.isEmpty()) {
                        Pair<Integer, Integer> ints1 = stack.poll();

                        if (ints[ints1.getLeft()][ints1.getRight()] == 0) {
                            visited[ints1.getLeft()][ints1.getRight()] = true;

                            for (int[] surrounding : SURROUNDINGS) {
                                //filter out of bound
                                if (ints1.getLeft() + surrounding[0] >= 0 && ints1.getLeft() + surrounding[0] < ints.length && ints1.getRight() + surrounding[1] >= 0 && ints1.getRight() + surrounding[1] < ints[0].length) {

                                    Pair<Integer, Integer> ints2 = Pair.of(ints1.getLeft() + surrounding[0], ints1.getRight() + surrounding[1]);
                                    //has surrounding no bombs around it
                                    if (ints[ints1.getLeft() + surrounding[0]][ints1.getRight() + surrounding[1]] == 0) {
                                        //is surrounding already checked
                                        if (!visited[ints2.getLeft()][ints2.getRight()]) {
                                            visited[ints2.getLeft()][ints2.getRight()] = true;
                                            stack.offer(ints2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    clickCount++;
                    continue;
                }

                int notNull = 0;
                int all = 0;
                for (int[] surrounding : SURROUNDINGS) {
                    if (i + surrounding[0] >= 0 && i + surrounding[0] < ints.length && j + surrounding[1] >= 0 && j + surrounding[1] < ints[0].length) {
                        if (!board[i + surrounding[0]][j + surrounding[1]]) {
                            if (ints[i + surrounding[0]][j + surrounding[1]] != 0) {
                                notNull++;
                            }
                            all++;
                        }
                    }
                }
                if (notNull == all)
                    clickCount++;
            }
        }

        return clickCount;
    }

}
