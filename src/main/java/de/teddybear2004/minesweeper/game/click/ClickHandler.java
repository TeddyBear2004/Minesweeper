package de.teddybear2004.minesweeper.game.click;

import com.comphenix.protocol.wrappers.BlockPosition;
import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.Field;
import de.teddybear2004.minesweeper.game.Game;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ClickHandler {

    void leftClick(@Nullable Player player, @NotNull Game game, @NotNull BlockPosition blockPosition, @NotNull Board board, @Nullable Field field, @NotNull Location location);

    void rightClick(@NotNull Player player, @NotNull Board board, @Nullable Field field, @Nullable Cancellable cancellable);

}
