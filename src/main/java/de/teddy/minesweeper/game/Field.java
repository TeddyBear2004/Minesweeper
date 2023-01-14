package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.Location;
import org.bukkit.Material;

public class Field {

    private final boolean isBomb;
    private final int bombCount;
    private final Board board;
    private final int x;
    private final int y;
    private boolean isCovered;
    private MarkType markType;

    public Field(Board board, int x, int y, boolean isBomb, int bombCount) {
        this.board = board;
        this.x = x;
        this.y = y;
        this.isCovered = true;
        this.markType = MarkType.NONE;
        this.isBomb = isBomb;
        this.bombCount = bombCount;
    }

    public int getNeighborCount() {
        return bombCount;
    }

    public void setUncover() {
        this.markType = MarkType.NONE;
        this.isCovered = false;
    }

    public boolean isBomb() {
        return isBomb;
    }

    public boolean isMarked() {
        return !markType.isNone();
    }

    public void reverseMark() {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(board.getPlayer());
        if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_MARKS).orElse(true))
            this.markType = this.markType.next(board.getPlayer());
        else
            this.markType = MarkType.NONE;

    }

    public Material getMark() {
        return markType.getMaterial();
    }

    public void setMark(MarkType markType) {
        this.markType = markType;
    }

    public boolean isCovered() {
        return isCovered;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Board getBoard() {
        return board;
    }

    public Field getRelativeTo(int i, int j) {
        return board.getField(x + i, y + j);
    }

    public Location getLocation() {
        return board.getCorner().clone().add(x, 0, y);
    }

}
