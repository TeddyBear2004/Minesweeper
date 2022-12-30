package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.modifier.Modifier;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.BlockPainter;
import de.teddy.minesweeper.game.painter.Painter;
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

import java.util.*;

public class Game {

    public static final Map<Class<? extends Painter>, Painter> PAINTER_MAP = new HashMap<>();
    private static final Map<Player, Board> gameWatched = new HashMap<>();
    private static final Map<Player, Board> runningGames = new HashMap<>();
    private static final Map<Player, Game> playerLocation = new HashMap<>();

    static {
        PAINTER_MAP.put(BlockPainter.class, new BlockPainter(Minesweeper.getPlugin(Minesweeper.class)));
        PAINTER_MAP.put(ArmorStandPainter.class, new ArmorStandPainter(Minesweeper.getPlugin(Minesweeper.class)));
    }

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

    public Game(Plugin plugin, List<Game> games, Language language, Location corner, Location spawn, int borderSize, int bombCount, String difficulty, Material material, int inventoryPosition) {
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
        assert itemMeta != null;

        itemMeta.setDisplayName(difficulty);
        itemMeta.setLore(Collections.singletonList(language.getString("field_desc", String.valueOf(size), String.valueOf(size), String.valueOf(bombCount))));
        itemStack.setItemMeta(itemMeta);
    }

    private static void switchToMap(Player p, Game g) {
        Game.stopWatching(p);
        Board b = runningGames.get(p);
        if (b != null) {
            Game.finishGame(p);
        }
        playerLocation.put(p, g);
        if (Modifier.getInstance().allowFly() || Modifier.getInstance().isInside(g.getViewingSpawn())) {
            p.setAllowFlight(true);
            p.setFlying(true);
        }
        p.teleport(g.getViewingSpawn());
    }

    private static void startWatching(Player p, Board b) {
        stopWatching(p);
        Game cur = playerLocation.get(p);
        if (cur != b.map) {
            switchToMap(p, b.map);
        }
        b.draw(Collections.singletonList(p));
        gameWatched.put(p, b);
        b.addViewer(p);
    }

    private static void stopWatching(Player p) {
        Board b = gameWatched.remove(p);
        if (b != null) {
            b.removeViewer(p);
        }
    }

    public static void stopGames(Player p) {
        Board b = runningGames.get(p);
        if (b != null) {
            b.drawBlancField();
            b.finish();
            b.getViewers().forEach(gameWatched::remove);
            b.clearViewers();
        } else {
            stopWatching(p);
        }
        runningGames.remove(p);
    }

    public static Game getGame(Player player) {
        return playerLocation.get(player);
    }

    public static Board getBoard(Player Player) {
        return runningGames.get(Player);
    }

    public static Board getBoardWatched(Player player) {
        return gameWatched.get(player);
    }

    public static void finishGame(Player p) {
        Game.getGame(p).finish(p);
    }

    public static Map<Player, Board> getRunningGames() {
        return runningGames;
    }

    public static Painter getPainter(Player player) {
        return PAINTER_MAP.get(Painter.loadPainterClass(player.getPersistentDataContainer()));
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
        for (Board b : runningGames.values())
            if (b.map == this)
                return b;
        return null;
    }

    public void startGame(Player p) {
        startGame(p, true);
    }

    public void startGame(Player p, boolean shouldTeleport) {
        startGame(p, shouldTeleport, bombCount);
    }

    public void startGame(Player p, boolean shouldTeleport, int bombCount) {
        startGame(p, shouldTeleport, bombCount, new Random().nextLong());
    }

    public void startGame(Player p, boolean shouldTeleport, int bombCount, long seed) {
        stopGames(p);
        Board b;
        b = new Board(plugin, language, this, size, size, bombCount, corner, p, seed);
        b.drawBlancField(Collections.singletonList(p));
        startWatching(p, b);
        runningGames.put(p, b);

        Bukkit.getOnlinePlayers().forEach(onPlayer -> {
            if (gameWatched.get(onPlayer) == null && b.map == playerLocation.get(onPlayer)) {
                startWatching(onPlayer, b);
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
    }

    private void finish(Player p) {
        stopGames(p);
        Board b = getRunningGame();
        if (b != null) {
            b.drawBlancField(Collections.singletonList(p));
            Bukkit.getOnlinePlayers().forEach(onPlayer -> {
                if (gameWatched.get(onPlayer) == null && b.map == playerLocation.get(onPlayer)) {
                    startWatching(onPlayer, b);
                }
            });
        }
    }

    public void startViewing(Player player, Board runningGame) {
        if (runningGame == null) {
            Game.switchToMap(player, games.get(0));
        } else {
            Game.startWatching(player, runningGame);
        }
    }

    public String getDifficulty() {
        return difficulty;
    }

}
