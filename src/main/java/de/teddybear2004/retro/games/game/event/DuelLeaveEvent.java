package de.teddybear2004.retro.games.game.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DuelLeaveEvent extends DuelEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public DuelLeaveEvent(Player player) {
        super(player);
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
