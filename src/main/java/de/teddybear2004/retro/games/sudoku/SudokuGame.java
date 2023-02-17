package de.teddybear2004.retro.games.sudoku;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.modifier.Modifier;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.util.ConnectionBuilder;
import de.teddybear2004.retro.games.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class SudokuGame extends Game {

    public SudokuGame(Plugin plugin, GameManager gameManager, @NotNull Language language, ConnectionBuilder connectionBuilder, Location corner, Location spawn, String difficulty, @NotNull Material material, int inventoryPosition, Atelier atelier) {
        super(plugin, gameManager, language, connectionBuilder, corner, spawn, 9, 9, -1, difficulty, material, inventoryPosition, atelier);
    }

    @Override
    public @Nullable Board<?> startGame(@NotNull Player p, boolean shouldTeleport, int bombCount, int width, int height, long seed, boolean setSeed, boolean saveStats, Class<? extends Board<?>> boardClass) {
        GameManager gameManager = getGameManager();
        gameManager.stopGames(p, saveStats);
        Board<?> b;

        b = new SudokuBoard(getPlugin(), getLanguage(), getConnectionBuilder(), this, getCorner(), p, seed, setSeed, saveStats, getAtelier());
        b.drawBlancField(Collections.singletonList(p));
        gameManager.startWatching(p, b);
        gameManager.getRunningGames().put(p, b);

        Bukkit.getOnlinePlayers().forEach(onPlayer -> {
            if (gameManager.getGameWatched().get(onPlayer) == null && b.getGame() == gameManager.getPlayerLocation().get(onPlayer)) {
                gameManager.startWatching(onPlayer, b);
                b.setScoreBoard(onPlayer);
            }
        });

        b.getPlayerInventory().apply(p);

        boolean allowFly = Modifier.getInstance().allowFly() || Modifier.getInstance().isInside(getViewingSpawn());

        if (allowFly)
            p.setAllowFlight(true);
        if (shouldTeleport) {
            if (allowFly)
                p.setFlying(true);
            p.teleport(this.getViewingSpawn());
        }

        return b;
    }

    @Override
    public @NotNull Builder getStarter() {
        return new SudokuBuilder(this);
    }

    public static class SudokuBuilder extends Builder {

        public SudokuBuilder(@NotNull Game game) {
            super(game);
        }

        @Override
        public Board<?> build(@NotNull Player player, Class<? extends Board<?>> boardClass) {
            return super.build(player, boardClass);
        }

    }

}
