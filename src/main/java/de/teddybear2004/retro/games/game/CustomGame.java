package de.teddybear2004.retro.games.game;

import de.teddybear2004.retro.games.RetroGames;
import de.teddybear2004.retro.games.util.ConnectionBuilder;
import de.teddybear2004.retro.games.util.Language;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomGame extends Game {

    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;

    public CustomGame(RetroGames plugin, GameManager gameManager, @NotNull Language language, ConnectionBuilder connectionBuilder, Location corner, Location spawn, int minWidth, int minHeight, int maxWidth, int maxHeight, String difficulty) {
        super(plugin, gameManager, language, connectionBuilder, corner, spawn, -1, -1, -1, difficulty, Material.AIR, -1);

        if (minWidth > maxWidth || minHeight > maxHeight)
            throw new IllegalArgumentException("Min size cannot be bigger max size in custom game.");

        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }


    @Override
    public @Nullable Board<?> startGame(@NotNull Player p, boolean shouldTeleport, int bombCount, int width, int height, long seed, boolean setSeed, boolean saveStats, Class<? extends Board<?>> boardClass) {
        if (minHeight > height || height > maxHeight || minWidth > width || width > maxWidth)
            return null;

        return super.startGame(p, shouldTeleport, bombCount, width, height, seed, setSeed, saveStats, boardClass);
    }

}
