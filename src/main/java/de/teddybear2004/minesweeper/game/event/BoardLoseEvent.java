package de.teddybear2004.minesweeper.game.event;

import de.teddybear2004.minesweeper.game.Board;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BoardLoseEvent extends BoardFinishEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public BoardLoseEvent(Board<?> board, Player player) {
        super(board, player);
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
