package de.teddybear2004.retro.games.game;

import de.teddybear2004.retro.games.minesweeper.MinesweeperField;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Field {

    int getX();

    int getY();

    Board<?> getBoard();

    @Nullable MinesweeperField getRelativeTo(int i, int j);

    @NotNull Location getLocation();

}
