package de.teddybear2004.retro.games.game;

import de.teddybear2004.retro.games.game.modifier.Modifier;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
import de.teddybear2004.retro.games.util.ConnectionBuilder;
import de.teddybear2004.retro.games.util.IsBetween;
import de.teddybear2004.retro.games.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Random;

public class Game {

    private final Plugin plugin;
    private final @NotNull Language language;
    private final Location corner;
    private final Location spawn;
    private final int width;
    private final int height;
    private final int bombCount;
    private final String difficulty;
    private final int inventoryPosition;
    private final @NotNull ItemStack itemStack;
    private final Atelier atelier;
    private final GameManager gameManager;
    private final ConnectionBuilder connectionBuilder;

    public Game(Plugin plugin, GameManager gameManager, @NotNull Language language, ConnectionBuilder connectionBuilder, Location corner, Location spawn, int width, int height, int bombCount, String difficulty, @NotNull Material material, int inventoryPosition, Atelier atelier) {
        this.gameManager = gameManager;
        this.connectionBuilder = connectionBuilder;
        this.plugin = plugin;
        this.language = language;
        this.corner = corner;
        this.spawn = spawn;
        this.width = width;
        this.height = height;
        this.bombCount = bombCount;
        this.difficulty = difficulty;
        this.inventoryPosition = inventoryPosition;

        this.itemStack = new ItemStack(material);
        this.atelier = atelier;
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(language.getString(difficulty));
            itemMeta.setLore(Collections.singletonList(language.getString("field_desc", String.valueOf(width), String.valueOf(height), String.valueOf(bombCount))));
        }

        itemStack.setItemMeta(itemMeta);
    }

    public boolean isOutside(@NotNull Location location) {
        return IsBetween.isOutside2D(corner, width, height, location)
                || IsBetween.isOutside(corner.getBlockY(), corner.getBlockY() + 1, location.getY());
    }

    public int getFieldHeight() {
        return corner.getBlockY();
    }

    public Location getViewingSpawn() {
        return spawn;
    }

    public int getInventoryPosition() {
        return inventoryPosition;
    }

    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public @Nullable Board<?> startGame(@NotNull Player p, boolean shouldTeleport, int bombCount, int width, int height, long seed, boolean setSeed, boolean saveStats, Class<? extends Board<?>> boardClass) {
        gameManager.stopGames(p, saveStats);
        Board<?> b;

        b = new MinesweeperBoard(plugin, language, connectionBuilder, this, width, height, bombCount, corner, p, seed, setSeed, saveStats, atelier);
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

    public Atelier getAtelier() {
        return atelier;
    }

    public Location getCorner() {
        return corner;
    }

    public ConnectionBuilder getConnectionBuilder() {
        return connectionBuilder;
    }

    public @NotNull Language getLanguage() {
        return language;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    protected void finish(Player p, boolean saveStats) {
        gameManager.stopGames(p, saveStats);
        Board<?> b = getRunningGame();
        if (b != null) {
            b.drawBlancField(Collections.singletonList(p));
            Bukkit.getOnlinePlayers().forEach(onPlayer -> {
                if (gameManager.getGameWatched().get(onPlayer) == null && b.getGame() == gameManager.getPlayerLocation().get(onPlayer)) {
                    gameManager.startWatching(onPlayer, b);
                }
            });
        }
    }

    public @Nullable Board<?> getRunningGame() {
        for (Board<?> b : gameManager.getRunningGames().values())
            if (b.getGame() == this)
                return b;
        return null;
    }

    public void startViewing(@NotNull Player player, @Nullable Board<?> runningGame) {
        if (runningGame == null) {
            gameManager.switchToMap(player, gameManager.getGames().get(0));
        } else {
            gameManager.startWatching(player, runningGame);
        }
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public @NotNull String getMap() {
        return width + "x" + height;
    }

    public int getBombCount() {
        return bombCount;
    }

    public @NotNull Builder getStarter() {
        return new Builder(this);
    }

    public static class Builder {

        private final @NotNull Game game;
        private boolean shouldTeleport;
        private int bombCount;
        private int width;
        private int height;
        private @Nullable Long seed;
        private boolean setSeed;
        private boolean saveStats;

        public Builder(@NotNull Game game) {
            this.game = game;
            this.shouldTeleport = true;
            this.bombCount = game.bombCount;
            this.width = game.width;
            this.height = game.height;
            this.seed = null;
            this.setSeed = false;
            this.saveStats = true;
        }

        public @NotNull Builder setShouldTeleport(boolean shouldTeleport) {
            this.shouldTeleport = shouldTeleport;
            return this;
        }

        public @NotNull Builder setBombCount(int bombCount) {
            this.bombCount = bombCount;
            return this;
        }

        public @NotNull Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public @NotNull Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public @NotNull Builder setSeed(long seed) {
            this.seed = seed;
            return this;
        }

        public @NotNull Builder setSetSeed(boolean setSeed) {
            this.setSeed = setSeed;
            return this;
        }

        public @NotNull Builder setSaveStats(boolean saveStats) {
            this.saveStats = saveStats;
            return this;
        }

        public Board<?> build(@NotNull Player player, Class<? extends Board<?>> boardClass) {
            if (seed == null)
                seed = new Random().nextLong();

            return game.startGame(player, shouldTeleport, bombCount, width, height, seed, setSeed, saveStats, boardClass);
        }

    }

}
