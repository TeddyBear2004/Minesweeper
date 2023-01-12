package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.modifier.Modifier;
import de.teddy.minesweeper.util.ConnectionBuilder;
import de.teddy.minesweeper.util.IsBetween;
import de.teddy.minesweeper.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game {

    private final Plugin plugin;
    private final List<Game> games;
    private final Language language;
    private final Location corner;
    private final Location spawn;
    private final int size;
    private final int bombCount;
    private final String difficulty;
    private final int inventoryPosition;
    private final ItemStack itemStack;
    private final GameManager gameManager;
    private final ConnectionBuilder connectionBuilder;

    public Game(Plugin plugin, GameManager gameManager, List<Game> games, Language language, ConnectionBuilder connectionBuilder, Location corner, Location spawn, int borderSize, int bombCount, String difficulty, Material material, int inventoryPosition) {
        this.gameManager = gameManager;
        this.connectionBuilder = connectionBuilder;
        this.plugin = plugin;
        this.games = games;
        this.language = language;
        this.corner = corner;
        this.spawn = spawn;
        this.size = borderSize;
        this.bombCount = bombCount;
        this.difficulty = difficulty;
        this.inventoryPosition = inventoryPosition;

        this.itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(difficulty);
            itemMeta.setLore(Collections.singletonList(language.getString("field_desc", String.valueOf(size), String.valueOf(size), String.valueOf(bombCount))));
        }

        itemStack.setItemMeta(itemMeta);
    }

    public boolean isBlockOutsideGame(Block block) {
        return !IsBetween.isBetween2D(corner, size, size, block)
                || !IsBetween.isBetween(corner.getBlockY(), corner.getBlockY() + 1, block.getY());
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

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Board getRunningGame() {
        for (Board b : gameManager.getRunningGames().values())
            if (b.map == this)
                return b;
        return null;
    }

    public Board startGame(Player p, boolean shouldTeleport, int bombCount, int width, int height, long seed, boolean setSeed, boolean saveStats) {
        gameManager.stopGames(p, saveStats);
        Board b;

        b = new Board(plugin, language, connectionBuilder, this, width, height, bombCount, corner, p, seed, setSeed, saveStats);
        b.drawBlancField(Collections.singletonList(p));
        gameManager.startWatching(p, b);
        gameManager.getRunningGames().put(p, b);

        Bukkit.getOnlinePlayers().forEach(onPlayer -> {
            if (gameManager.getGameWatched().get(onPlayer) == null && b.map == gameManager.getPlayerLocation().get(onPlayer)) {
                gameManager.startWatching(onPlayer, b);
                b.setScoreBoard(onPlayer);
            }
        });

        p.getInventory().clear();
        p.getInventory().setContents(Inventories.GAME_INVENTORY);
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

    protected void finish(Player p, boolean saveStats) {
        gameManager.stopGames(p, saveStats);
        Board b = getRunningGame();
        if (b != null) {
            b.drawBlancField(Collections.singletonList(p));
            Bukkit.getOnlinePlayers().forEach(onPlayer -> {
                if (gameManager.getGameWatched().get(onPlayer) == null && b.map == gameManager.getPlayerLocation().get(onPlayer)) {
                    gameManager.startWatching(onPlayer, b);
                }
            });
        }
    }

    public void startViewing(Player player, Board runningGame) {
        if (runningGame == null) {
            gameManager.switchToMap(player, games.get(0));
        } else {
            gameManager.startWatching(player, runningGame);
        }
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getMap() {
        return size + "x" + size;
    }

    public int getBombCount() {
        return bombCount;
    }

    public Builder getStarter() {
        return new Builder(this);
    }

    public static class Builder {

        private final Game game;
        private boolean shouldTeleport;
        private int bombCount;
        private int width;
        private int height;
        private Long seed;
        private boolean setSeed;
        private boolean saveStats;

        private Builder(Game game) {
            this.game = game;
            this.shouldTeleport = true;
            this.bombCount = game.bombCount;
            this.width = game.size;
            this.height = game.size;
            this.seed = null;
            this.setSeed = false;
            this.saveStats = true;
        }

        public Builder setShouldTeleport(boolean shouldTeleport) {
            this.shouldTeleport = shouldTeleport;
            return this;
        }

        public Builder setBombCount(int bombCount) {
            this.bombCount = bombCount;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setSeed(long seed) {
            this.seed = seed;
            return this;
        }

        public Builder setSetSeed(boolean setSeed) {
            this.setSeed = setSeed;
            return this;
        }

        public Builder setSaveStats(boolean saveStats) {
            this.saveStats = saveStats;
            return this;
        }

        public void build(Player player) {
            if (seed == null)
                seed = new Random().nextLong();

            game.startGame(player, shouldTeleport, bombCount, width, height, seed, setSeed, saveStats);
        }

    }

}
