package de.teddybear2004.minesweeper.game;

import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinesweeperField implements Field {

    private final boolean isBomb;
    private final int bombCount;
    private final Board<MinesweeperField> board;
    private final int x;
    private final int y;
    private boolean isCovered;
    private MarkType markType;

    public MinesweeperField(Board<MinesweeperField> board, int x, int y, boolean isBomb, int bombCount) {
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
        if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_MARKS))
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

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public Board<MinesweeperField> getBoard() {
        return board;
    }

    @Override
    public @Nullable MinesweeperField getRelativeTo(int i, int j) {
        return board.getField(x + i, y + j);
    }

    @Override
    public @NotNull Location getLocation() {
        return board.getCorner().clone().add(x, 0, y);
    }

}
