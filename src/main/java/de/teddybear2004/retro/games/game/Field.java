package de.teddybear2004.retro.games.game;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Field<F extends Field<F>> {

    private final Board<F> board;
    private final int x;
    private final int y;

    public Field(Board<F> board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Board<F> getBoard() {
        return board;
    }

    public @Nullable F getRelativeTo(int i, int j) {
        return board.getField(x + i, y + j);
    }

    public @NotNull Location getLocation() {
        return board.getCorner().clone().add(x, 0, y);
    }

}
