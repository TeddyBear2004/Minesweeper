package de.teddybear2004.minesweeper.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class DuelEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;

    public DuelEvent(Player player) {
        this.player = player;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
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
