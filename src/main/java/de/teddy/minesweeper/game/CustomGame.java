package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.util.ConnectionBuilder;
import de.teddy.minesweeper.util.Language;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CustomGame extends Game {

    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;

    public CustomGame(Minesweeper plugin, GameManager gameManager, List<Game> games, @NotNull Language language, ConnectionBuilder connectionBuilder, Location corner, Location spawn, int minWidth, int minHeight, int maxWidth, int maxHeight, String difficulty) {
        super(plugin, gameManager, games, language, connectionBuilder, corner, spawn, -1, -1, -1, difficulty, Material.AIR, -1);

        if (minWidth > maxWidth || minHeight > maxHeight)
            throw new IllegalArgumentException("Min size cannot be bigger max size in custom game.");

        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }


    @Override
    public @Nullable Board startGame(@NotNull Player p, boolean shouldTeleport, int bombCount, int width, int height, long seed, boolean setSeed, boolean saveStats) {
        if (minHeight > height || height > maxHeight || minWidth > width || width > maxWidth)
            return null;

        return super.startGame(p, shouldTeleport, bombCount, width, height, seed, setSeed, saveStats);
    }

}
