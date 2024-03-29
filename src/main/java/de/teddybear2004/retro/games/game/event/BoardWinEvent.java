package de.teddybear2004.retro.games.game.event;

import de.teddybear2004.retro.games.game.Board;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BoardWinEvent extends BoardFinishEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public BoardWinEvent(Board<?> board, Player player) {
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
