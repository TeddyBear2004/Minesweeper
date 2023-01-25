package de.teddybear2004.minesweeper.game.event;

import de.teddybear2004.minesweeper.game.Board;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class BoardFinishEvent extends BoardEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final long time;
    private final int flagScore;

    public BoardFinishEvent(Board board, Player player, long time, int flagScore) {
        super(board, player);
        this.time = time;
        this.flagScore = flagScore;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
