package de.teddybear2004.minesweeper.game.event;

import de.teddybear2004.minesweeper.game.DuelGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DuelLeaveEvent extends DuelEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final DuelGame.Builder builder;

    public DuelLeaveEvent(Player player, DuelGame.Builder builder) {
        super(player);
        this.builder = builder;
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

    public DuelGame.Builder getBuilder() {
        return builder;
    }

}
