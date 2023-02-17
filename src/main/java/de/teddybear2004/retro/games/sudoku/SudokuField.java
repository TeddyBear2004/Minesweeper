package de.teddybear2004.retro.games.sudoku;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Field;

public class SudokuField extends Field<SudokuField> {

    private final boolean startValue;
    private int number;

    public SudokuField(Board<SudokuField> board, int x, int y, int number, boolean startValue) {
        super(board, x, y);
        this.number = number;
        this.startValue = startValue;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isStartValue() {
        return startValue;
    }

}
