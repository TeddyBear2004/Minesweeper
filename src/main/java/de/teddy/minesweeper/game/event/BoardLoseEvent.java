package de.teddy.minesweeper.game.event;

import de.teddy.minesweeper.game.Board;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BoardLoseEvent extends BoardFinishEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public BoardLoseEvent(Board board, Player player, long time, int flagScore) {
        super(board, player, time, flagScore);
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
