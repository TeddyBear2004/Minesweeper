package de.teddybear2004.retro.games.game;

import de.teddybear2004.retro.games.game.event.BoardLoseEvent;
import de.teddybear2004.retro.games.game.event.BoardWinEvent;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.game.painter.Painter;
import de.teddybear2004.retro.games.util.ConnectionBuilder;
import de.teddybear2004.retro.games.util.IsBetween;
import de.teddybear2004.retro.games.util.Language;
import de.teddybear2004.retro.games.util.Time;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static de.teddybear2004.retro.games.minesweeper.SurfaceDiscoverer.SURROUNDINGS;

public abstract class Board<F extends Field<F>> implements Comparable<Board<?>> {

    private final Game game;
    private final @NotNull Plugin plugin;
    private final Language language;
    private final List<Player> viewers = new LinkedList<>();
    private final int width;
    private final int height;
    private final Location corner;
    private final Player player;
    private final boolean saveStats;
    private final Atelier atelier;
    private final long seed;
    private final ConnectionBuilder connectionBuilder;
    private final boolean setSeed;
    private F[][] board;
    private boolean isGenerated;
    private boolean win = false;
    private boolean isFinished;
    private long started;
    private Long duration;
    private int startX;
    private int startY;
    private Scoreboard scoreboard;

    public Board(@NotNull Plugin plugin, Language language, ConnectionBuilder connectionBuilder, Game game, int width, int height, Location corner, Player player, long seed, boolean setSeed, boolean saveStats, Atelier atelier) {
        this.connectionBuilder = connectionBuilder;
        this.setSeed = setSeed;
        this.plugin = plugin;
        this.language = language;
        this.game = game;
        this.player = player;
        this.seed = seed;
        this.saveStats = saveStats;
        this.atelier = atelier;

        this.isFinished = this.isGenerated = false;
        this.corner = corner;
        this.width = width;
        this.height = height;

        if (Bukkit.getScoreboardManager() != null)
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    }

    public static short convertToLocal(int x, int y, int z) {
        x &= 0xF;
        y &= 0xF;
        z &= 0xF;
        return (short) (x << 8 | z << 4 | y);
    }

    public static boolean isLightField(int x, int y) {
        return Math.abs(x + y) % 2 == 0;
    }

    public void initBoard() {
        this.board = generateBoard(width, height);
        initScoreboard();
        draw();
    }

    public abstract F[][] generateBoard(int width, int height);

    public abstract void initScoreboard();

    public final void draw() {
        this.draw(this.viewers);
    }

    public final void draw(List<? extends Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if (painter != null) {
                painter.drawField(this, players2);
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    public final @NotNull Map<Painter<F>, List<Player>> getCurrentPlayerPainters(List<? extends Player> viewers) {
        Map<Painter<F>, List<Player>> map1 = new HashMap<>();

        viewers.forEach(player -> {
            Painter<F> painter = atelier.getPainter(player, getBoardClass());
            map1.computeIfAbsent(painter, p -> new ArrayList<>()).add(player);
        });

        Painter<F> painter1 = atelier.getPainter(player, getBoardClass());
        map1.computeIfAbsent(painter1, p -> new ArrayList<>()).add(player);

        return map1;
    }

    @SuppressWarnings("rawtypes")
    public abstract Class<? extends Board> getBoardClass();

    public final boolean isLose() {
        return !win;
    }

    public final F[][] getBoard() {
        return this.board;
    }

    public final Player getPlayer() {
        return player;
    }

    public final @NotNull List<Player> getViewers() {
        return viewers;
    }

    public final void addViewer(@NotNull Player player) {
        this.viewers.add(player);
        draw(Collections.singletonList(player));
    }

    public final void removeViewer(Player player) {
        this.viewers.remove(player);
    }

    public final void clearViewers() {
        this.viewers.clear();
    }

    public final void drawBlancField() {
        this.drawBlancField(viewers);
    }

    public final void drawBlancField(List<? extends Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if (painter != null)
                painter.drawBlancField(this, players2);
        });
    }

    public final boolean isGenerated() {
        return this.isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }

    public void checkField(int x, int y) {
        checkField(x, y, true);
    }

    public void checkField(int x, int y, boolean b) {
        x = Math.abs(this.corner.getBlockX() - x);
        y = Math.abs(this.corner.getBlockZ() - y);

        boolean isGenerated1 = this.isGenerated;

        if (!isGenerated1)
            generateBoard(x, y);

        if (b)
            startStarted();
    }

    public final void startStarted() {
        if (this.started == 0)
            this.started = System.currentTimeMillis();
    }

    public InventoryManager.PlayerInventory getPlayerInventory() {
        return InventoryManager.PlayerInventory.GAME;
    }

    public final Game getGame() {
        return game;
    }

    public final @NotNull List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>(viewers);

        list.add(player);

        return list;
    }

    public final @Nullable F getField(@NotNull Location location) {
        int x = Math.abs(this.corner.getBlockX() - location.getBlockX());
        int y = Math.abs(this.corner.getBlockZ() - location.getBlockZ());

        return this.getField(x, y);
    }

    public final @Nullable F getField(int x, int y) {
        if (x >= 0 && x < board.length && y >= 0 && y < board[0].length) {
            return this.board[x][y];
        } else {
            return null;
        }
    }

    public final int getWidth() {
        return this.width;
    }

    public final int getHeight() {
        return this.height;
    }

    public final Location getCorner() {
        return this.corner;
    }

    public void checkIfWon() {
        for (F[] fields : this.board)
            for (F field : fields)
                if (field == null)
                    return;

        win();
    }

    public final void win() {
        this.win = true;
        long now = System.currentTimeMillis();
        duration = getActualTimeNeeded(now);
        String actualTimeNeededString = getActualTimeNeededString(now);

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().getPluginManager().callEvent(new BoardWinEvent(this, player)));

        this.finish(true, true, duration);

        sendWinMessages(actualTimeNeededString);
    }

    public final long getActualTimeNeeded(long now) {
        return this.started == 0 ? 0 : now - this.started;
    }

    public final @NotNull String getActualTimeNeededString(long now) {
        return Time.parse(false, getActualTimeNeeded(now));
    }

    public final long finish(boolean won, boolean saveStats, long time) {
        if (isFinished)
            return time;

        this.isFinished = true;

        if (saveStats && this.saveStats) {
            saveStats(won, true, time);
        }
        return time;
    }

    public abstract void sendWinMessages(String actualTimeNeededString);

    public abstract void saveStats(boolean won, boolean saveStats, long time);

    public void lose() {
        this.win = false;
        duration = finish(false);

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().getPluginManager().callEvent(new BoardLoseEvent(this, player)));
    }

    public long finish(boolean won) {
        return this.finish(won, true);
    }

    public long finish(boolean won, boolean saveStats) {
        return this.finish(won, saveStats, getActualTimeNeeded(System.currentTimeMillis()));
    }

    public void breakGame() {
        finish(false);
    }

    public boolean isBlockOutsideGame(@NotNull Location location) {
        return IsBetween.isOutside2D(corner, width, height, location)
                || IsBetween.isOutside(corner.getBlockY(), corner.getBlockY() + 1, location.getY());
    }

    public void highlightBlocksAround(F field) {
        List<F> surroundings = new ArrayList<>();
        SURROUNDINGS.forEach(ints -> {
            F relativeTo = field.getRelativeTo(ints[0], ints[1]);

            if (relativeTo != null)
                surroundings.add(relativeTo);
        });

        getCurrentPlayerPainters().forEach((painter, players) -> painter.highlightFields(surroundings, players, game.getGameManager().getRemoveMarkerScheduler()));

    }

    public @NotNull Map<Painter<F>, List<Player>> getCurrentPlayerPainters() {
        return getCurrentPlayerPainters(this.viewers);
    }

    public boolean isWin() {
        return win;
    }

    public Long getDuration() {
        return duration;
    }

    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    public boolean isSetSeed() {
        return setSeed;
    }

    public ConnectionBuilder getConnectionBuilder() {
        return connectionBuilder;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public long getStarted() {
        return started;
    }

    public long getSeed() {
        return seed;
    }

    public Language getLanguage() {
        return language;
    }

    public abstract void updateScoreBoard();

    public void setScoreBoard(@NotNull Player player) {
        if (scoreboard == null || isFinished())
            return;

        player.setScoreboard(scoreboard);
    }

    public final boolean isFinished() {
        return this.isFinished;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

}
