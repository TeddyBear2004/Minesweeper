package de.teddybear2004.retro.games.minesweeper;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Field;
import de.teddybear2004.retro.games.game.modifier.PersonalModifier;
import org.bukkit.Material;

public class MinesweeperField extends Field<MinesweeperField> {

    private final boolean isBomb;
    private final int bombCount;
    private boolean isCovered;
    private MarkType markType;

    public MinesweeperField(Board<MinesweeperField> board, int x, int y, boolean isBomb, int bombCount) {
        super(board, x, y);
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
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(getBoard().getPlayer());
        if (personalModifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_MARKS))
            this.markType = this.markType.next(getBoard().getPlayer());
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
}
