package de.teddybear2004.retro.games.sudoku;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SudokuBackTrackerTest {

    @Test
    public void testHasConflicts() {
        Random random = new Random();
        assertTrue(new SudokuBackTracker(random, new int[][]{
                {1, 8, 5, 9, 2, 7, 4, 6, 3},
                {4, 2, 3, 1, 6, 8, 7, 5, 9},
                {7, 6, 9, 3, 5, 4, 2, 1, 8},
                {6, 7, 9, 4, 2, 1, 8, 3, 5},
                {8, 4, 3, 7, 5, 9, 2, 6, 1},
                {2, 5, 1, 8, 3, 6, 7, 4, 9},
                {1, 6, 3, 5, 4, 9, 7, 2, 8},
                {9, 4, 8, 2, 3, 7, 1, 6, 5},
                {5, 7, 2, 8, 1, 6, 3, 4, 9},
        }).hasConflicts());
        assertFalse(new SudokuBackTracker(random, new int[][]{
                {3, 9, 8, 6, 5, 4, 7, 2, 1},
                {1, 2, 4, 8, 3, 7, 6, 9, 5},
                {6, 5, 7, 2, 1, 9, 4, 8, 3},
                {8, 3, 2, 5, 7, 1, 9, 6, 4},
                {9, 4, 5, 3, 8, 6, 1, 7, 2},
                {7, 6, 1, 9, 4, 0, 8, 5, 0},
                {5, 8, 3, 4, 2, 0, 0, 1, 7},
                {2, 7, 6, 1, 9, 8, 5, 0, 0},
                {4, 1, 9, 7, 6, 5, 2, 3, 8},
        }).hasConflicts());

    }

    @Test
    public void testGenerate() {
        Random random = new Random();
        SudokuBackTracker sudokuBackTracker = new SudokuBackTracker(random, new int[][]{
                {1, 7, 8, 0, 0, 0, 0, 0, 5},
                {0, 0, 3, 0, 0, 6, 9, 0, 0},
                {0, 0, 0, 8, 0, 5, 0, 0, 0},
                {6, 0, 0, 0, 0, 0, 5, 8, 0},
                {0, 9, 4, 7, 0, 0, 0, 0, 6},
                {2, 0, 0, 0, 0, 1, 0, 4, 0},
                {5, 0, 1, 0, 8, 0, 3, 0, 2},
                {0, 0, 0, 0, 0, 0, 0, 9, 0},
                {8, 2, 0, 6, 1, 0, 0, 0, 0},
        });
        sudokuBackTracker.generate(0, 0);
        for (int[] ints : sudokuBackTracker.getBoard()) {
            System.out.println(Arrays.toString(ints));
        }
    }

    @Test
    void getPossibleInteger() {
        SudokuBackTracker sudokuBackTracker;
        Random random = new Random();

        sudokuBackTracker = new SudokuBackTracker(random, new int[][]{
                {0, 0, 8, 6, 5, 4, 7, 2, 1},
                {1, 2, 4, 8, 3, 7, 6, 9, 5},
                {6, 5, 7, 2, 1, 9, 4, 8, 3},
                {8, 0, 0, 5, 7, 1, 9, 6, 4},
                {9, 4, 0, 0, 8, 6, 1, 7, 0},
                {7, 6, 1, 9, 4, 0, 8, 5, 0},
                {5, 8, 0, 4, 2, 0, 0, 1, 7},
                {2, 7, 6, 1, 9, 8, 5, 0, 0},
                {4, 1, 9, 7, 6, 5, 2, 3, 8},
        });

        assertArrayEquals(new int[]{3}, sudokuBackTracker.getPossibleIntegers(0, 0));
        assertArrayEquals(new int[]{3, 9}, sudokuBackTracker.getPossibleIntegers(0, 1));
        assertArrayEquals(new int[]{2, 3, 5}, sudokuBackTracker.getPossibleIntegers(4, 2));

        sudokuBackTracker = new SudokuBackTracker(random, new int[][]{
                {4, 7, 3, 8, 6, 9, 2, 5, 1,},
                {2, 8, 9, 7, 1, 5, 6, 3, 4,},
                {1, 6, 5, 4, 3, 0, 9, 8, 7,},
                {7, 0, 6, 0, 0, 0, 5, 2, 8,},
                {3, 4, 2, 0, 0, 0, 7, 1, 9,},
                {5, 9, 8, 2, 0, 0, 3, 4, 6,},
                {6, 3, 1, 5, 9, 4, 8, 7, 2,},
                {8, 5, 7, 0, 2, 6, 4, 9, 3,},
                {9, 2, 4, 3, 8, 7, 1, 6, 5,},
        });

        assertArrayEquals(new int[]{9}, sudokuBackTracker.getPossibleIntegers(3, 3));
    }

    @Test
    void reduce() {
        int single = 0;
        int iterations = 10_000;
        Random random = new Random();

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            SudokuBackTracker sudokuBackTracker33 = new SudokuBackTracker(random, 9);
            sudokuBackTracker33.generate(0, 0);
            sudokuBackTracker33.reduce(33);
            if (sudokuBackTracker33.clone().hasSingleSolution(0, 0)) {
                single++;
            }
        }

        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;

        System.out.println("Boards with single solution: " + single);
        System.out.println("Percentage of single solution boards: " + (double) single / iterations);

        System.out.println("Time elapsed in milliseconds: " + (double) (timeElapsed / 1_000_000) + "ms");
        System.out.println("Average Time elapsed in milliseconds: " + ((double) timeElapsed / (double) 1_000_000 / (double) iterations) + "ms");
        System.out.println("Time per single solution: " + (double) (timeElapsed / 1_000_000) / single + "ms");


        /*for (int i = 0; i < iterations; i++) {
            SudokuBackTracker sudokuBackTracker29 = new SudokuBackTracker(random, 9);
            sudokuBackTracker29.generate(0, 0);
            sudokuBackTracker29.reduce(29);
            if (sudokuBackTracker29.clone().hasSingleSolution(0, 0))
                single++;
        }

        endTime = System.nanoTime();
        timeElapsed = endTime - startTime;

        System.out.println("Boards with single solution: " + single);
        System.out.println("Percentage of single solution boards: " + (double) single / iterations);

        System.out.println("Time elapsed in milliseconds: " + (double) (timeElapsed / 1_000_000) + "ms");
        System.out.println("Average Time elapsed in milliseconds: " + ((double) timeElapsed / (double) 1_000_000 / (double) iterations) + "ms");
        System.out.println("Time per single solution: " + (double) (timeElapsed / 1_000_000) / single + "ms");



        for (int i = 0; i < iterations; i++) {
            SudokuBackTracker sudokuBackTracker25 = new SudokuBackTracker(random, 9);
            sudokuBackTracker25.generate(0, 0);
            sudokuBackTracker25.reduce(25);
            if (sudokuBackTracker25.clone().hasSingleSolution(0, 0))
                single++;
        }

        endTime = System.nanoTime();
        timeElapsed = endTime - startTime;

        System.out.println("Boards with single solution: " + single);
        System.out.println("Percentage of single solution boards: " + (double) single / iterations);

        System.out.println("Time elapsed in milliseconds: " + (double) (timeElapsed / 1_000_000) + "ms");
        System.out.println("Average Time elapsed in milliseconds: " + ((double) timeElapsed / (double) 1_000_000 / (double) iterations) + "ms");
        System.out.println("Time per single solution: " + (double) (timeElapsed / 1_000_000) / single + "ms");*/
    }

    @Test
    void hasSingleSolution() {
        Random random = new Random();
        SudokuBackTracker sudokuBackTracker = new SudokuBackTracker(random, new int[][]{
                {0, 4, 7, 5, 3, 9, 6, 1, 2},
                {9, 5, 6, 1, 7, 2, 3, 4, 8},
                {2, 3, 1, 6, 4, 8, 9, 5, 7},
                {4, 6, 2, 3, 8, 5, 7, 9, 1},
                {3, 9, 8, 7, 6, 1, 5, 2, 4},
                {7, 1, 5, 2, 9, 4, 8, 3, 6},
                {1, 2, 9, 8, 5, 6, 4, 7, 3},
                {5, 8, 3, 4, 2, 7, 1, 6, 9},
                {6, 7, 4, 9, 1, 3, 2, 8, 5},
        });

        assertTrue(sudokuBackTracker.hasSingleSolution(0, 0));

        sudokuBackTracker = new SudokuBackTracker(random, new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0},
                {9, 5, 6, 1, 7, 2, 3, 4, 8},
                {2, 3, 1, 6, 4, 8, 9, 5, 7},
                {4, 6, 2, 3, 8, 5, 7, 9, 1},
                {3, 9, 8, 7, 6, 1, 5, 2, 4},
                {7, 1, 5, 2, 9, 4, 8, 3, 6},
                {1, 2, 9, 8, 5, 6, 4, 7, 3},
                {5, 8, 3, 4, 2, 7, 1, 6, 9},
                {6, 7, 4, 9, 1, 3, 2, 8, 5},
        });
        assertTrue(sudokuBackTracker.hasSingleSolution(0, 0));

        sudokuBackTracker = new SudokuBackTracker(random, new int[][]{
                {4, 7, 3, 8, 6, 9, 2, 5, 1,},
                {2, 8, 9, 7, 1, 5, 6, 3, 4,},
                {1, 6, 5, 4, 3, 0, 9, 8, 7,},
                {7, 1, 6, 9, 4, 3, 5, 2, 8,},
                {3, 4, 2, 6, 5, 8, 7, 1, 9,},
                {5, 9, 8, 2, 7, 1, 3, 4, 6,},
                {6, 3, 1, 5, 9, 4, 8, 7, 2,},
                {8, 5, 7, 1, 2, 6, 4, 9, 3,},
                {9, 2, 4, 3, 8, 7, 1, 6, 5,},
        });
        assertTrue(sudokuBackTracker.hasSingleSolution(0, 0));
    }

}