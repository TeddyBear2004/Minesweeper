package de.teddybear2004.retro.games.game.click;

import com.comphenix.protocol.wrappers.BlockPosition;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Field;
import de.teddybear2004.retro.games.game.Game;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClickHandler<F extends Field<F>, B extends Board<F>> {

    void leftClick(@Nullable Player player, @NotNull Game game, @NotNull BlockPosition blockPosition, @NotNull B board, @Nullable F field, @NotNull Location location);

    void rightClick(@NotNull Player player, @NotNull B board, @Nullable F field, @Nullable Cancellable cancellable);

}
