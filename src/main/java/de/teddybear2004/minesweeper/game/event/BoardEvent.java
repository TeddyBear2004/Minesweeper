package de.teddybear2004.minesweeper.game.event;

import de.teddybear2004.minesweeper.game.Board;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class BoardEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Board board;
    private final Player player;

    public BoardEvent(Board board, Player player) {
        this.board = board;
        this.player = player;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Board getBoard() {
        return board;
    }

    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
