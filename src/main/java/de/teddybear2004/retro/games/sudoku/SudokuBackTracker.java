package de.teddybear2004.retro.games.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A backtracking algorithm to generate a valid solution for a sudoku puzzle.
 */
public class SudokuBackTracker implements Cloneable {

    // Random number generator to be used for generating the solution
    private final Random random;
    // Size of the puzzle (9 for standard 9x9 puzzle)
    private final int size;
    // The puzzle board represented as a 2D integer array
    private int[][] board;
    private int solutionCount = -1;

    /**
     * Constructor to create a new instance of the class.
     *
     * @param random Random number generator to be used for generating the solution
     * @param size   Size of the puzzle
     */
    public SudokuBackTracker(Random random, int size) {
        // Initialize instance variables
        this.random = random;
        this.size = size;
        this.board = new int[size][size];
    }

    /**
     * Constructor to create a new instance of the class with a partially filled puzzle.
     *
     * @param random Random number generator to be used for generating the solution
     * @param ints   The partially filled puzzle
     */
    public SudokuBackTracker(Random random, int[][] ints) {
        // Initialize instance variables
        this.random = random;
        this.size = ints.length;
        this.board = ints;
    }

    /**
     * Generates a solution for the sudoku puzzle using backtracking algorithm.
     */
    public boolean generate(int x, int y) {
        if (x == size)
            return true;

        int nextX = (y == size - 1) ? x + 1 : x;
        int nextY = (y == size - 1) ? 0 : y + 1;

        if (board[x][y] != 0)
            return generate(nextX, nextY);

        int[] integers = getPossibleIntegers(x, y);

        int index, integer, length, afterLength;
        int[] before, after;

        while (integers.length > 0) {
            length = integers.length;

            index = random.nextInt(length);
            integer = integers[index];

            board[x][y] = integer;
            if (!hasConflicts() && generate(nextX, nextY))
                return true;

            board[x][y] = 0;

            before = Arrays.copyOfRange(integers, 0, index);
            after = Arrays.copyOfRange(integers, index + 1, length);

            afterLength = length - index - 1;
            integers = new int[index + afterLength];
            System.arraycopy(before, 0, integers, 0, index);
            System.arraycopy(after, 0, integers, index, afterLength);
        }

        return false;
    }

    public boolean isFinishedSuccessful() {
        for (int[] ints : this.board)
            for (int anInt : ints)
                if (anInt == 0)
                    return false;

        return !hasConflicts();
    }

    public boolean hasConflicts() {
        int size = this.board.length;
        int set, fieldV, hShift;

        for (int i = 0; i < size; i++) {
            set = 0;

            for (int j = 0; j < size; j++) {
                fieldV = this.board[j][i];
                hShift = this.board[i][j] + size;

                if (hShift != size) {
                    if (((set >> hShift) & 0b1) == 1)
                        return true;

                    set |= 0b1 << hShift;
                }

                if (fieldV != 0) {
                    if (((set >> fieldV) & 0b1) == 1)
                        return true;

                    set |= 0b1 << fieldV;
                }
            }
        }

        int i = size / 3;
        int xSquare, ySquare, x, y, field;
        for (xSquare = 0; xSquare < i; xSquare++) {
            for (ySquare = 0; ySquare < i; ySquare++) {
                set = 0;

                for (x = 0; x < 3; x++) {
                    for (y = 0; y < 3; y++) {
                        field = this.board[xSquare * 3 + x][ySquare * 3 + y];

                        if (field == 0)
                            continue;

                        if (((set >> field) & 1) == 1)
                            return true;

                        set |= 0b1 << field;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasSingleSolution(int x, int y) {
        if (solutionCount < 0)
            solutionCount = 0;

        if (x == size) {
            solutionCount++;
            return solutionCount == 2;
        }

        int nextX = (y == size - 1) ? x + 1 : x;
        int nextY = (y == size - 1) ? 0 : y + 1;

        if (board[x][y] != 0)
            return hasSingleSolution(nextX, nextY);

        int[] integers = getPossibleIntegers(x, y);

        int index, integer;

        for (index = 0; index < integers.length; index++) {
            integer = integers[index];

            board[x][y] = integer;
            if (!hasConflicts() && hasSingleSolution(nextX, nextY))
                if (solutionCount == 2)
                    return true;

            board[x][y] = 0;
        }

        return false;
    }

    public void reduce(int leftoverFields) {
        //to improve the random speed I will just generate one random number
        //and determine by the bits which fields should be removed.

        List<int[]> fields = new ArrayList<>();

        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board[x].length; y++)
                if (board[x][y] != 0) fields.add(new int[]{x, y});


        int[] random;

        while (fields.size() > leftoverFields) {
            random = fields.remove(this.random.nextInt(fields.size()));
            board[random[0]][random[1]] = 0;
        }
    }

    public int[] getPossibleIntegers(int x, int y) {
        boolean[] possibleNumbers = new boolean[size];
        Arrays.fill(possibleNumbers, true);

        for (int i = 0; i < size; i++) {
            int fieldV = board[x][i];
            int fieldH = board[i][y];

            if (fieldV > 0)
                possibleNumbers[fieldV - 1] = false;

            if (fieldH > 0)
                possibleNumbers[fieldH - 1] = false;
        }

        int xSquare = x / 3 * 3;
        int ySquare = y / 3 * 3;
        for (int xTemp = 0; xTemp < 3; xTemp++) {
            for (int yTemp = 0; yTemp < 3; yTemp++) {
                int xCache = xSquare + xTemp;
                int yCache = ySquare + yTemp;

                if (x != xCache || y != yCache) {
                    int field = board[xCache][yCache];

                    if (field > 0)
                        possibleNumbers[field - 1] = false;
                }
            }
        }

        int count = 0;
        for (int i = 0; i < size; i++) {
            if (possibleNumbers[i])
                count++;
        }

        int[] result = new int[count];
        int index = 0;
        for (int i = 0; i < size; i++) {
            if (possibleNumbers[i]) {
                result[index] = i + 1;
                index += 1;
            }
        }

        return result;
    }

    @Override
    public SudokuBackTracker clone() {
        try{
            SudokuBackTracker clone = (SudokuBackTracker) super.clone();
            clone.board = new int[size][size];

            for (int i = 0; i < board.length; i++)
                System.arraycopy(board[i], 0, clone.board[i], 0, board[i].length);

            return clone;
        }catch(CloneNotSupportedException ignore){
            throw new RuntimeException();
        }

    }

    public int[][] getBoard() {
        return board;
    }

}
