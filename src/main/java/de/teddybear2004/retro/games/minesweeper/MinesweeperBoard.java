package de.teddybear2004.retro.games.minesweeper;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.event.BoardLoseEvent;
import de.teddybear2004.retro.games.game.event.BoardWinEvent;
import de.teddybear2004.retro.games.game.painter.MinesweeperPainter;
import de.teddybear2004.retro.games.game.painter.Painter;
import de.teddybear2004.retro.games.game.statistic.GameStatistic;
import de.teddybear2004.retro.games.minesweeper.exceptions.BombExplodeException;
import de.teddybear2004.retro.games.util.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static de.teddybear2004.retro.games.minesweeper.SurfaceDiscoverer.SURROUNDINGS;

public class MinesweeperBoard implements Board<MinesweeperField> {

    private final Game game;
    private final @NotNull Plugin plugin;
    private final Language language;
    private final List<Player> viewers = new LinkedList<>();
    private final int width;
    private final int height;
    private final int bombCount;
    private final Location corner;
    private final MinesweeperField[] @NotNull [] board;
    private final int[] @NotNull [] bombList;
    private final Player player;
    private final @NotNull Random random;
    private final boolean saveStats;
    private final long seed;
    private final ConnectionBuilder connectionBuilder;
    private final boolean setSeed;
    private boolean win = false;
    private long started;
    private Long duration;
    private boolean isGenerated;
    private boolean isFinished;
    private Scoreboard scoreboard;
    private int startX;
    private int startY;


    public MinesweeperBoard(@NotNull Plugin plugin, Language language, ConnectionBuilder connectionBuilder, Game game, int width, int height, int bombCount, Location corner, Player player, long seed, boolean setSeed, boolean saveStats) {
        this.connectionBuilder = connectionBuilder;
        this.setSeed = setSeed;
        this.plugin = plugin;
        this.language = language;
        this.game = game;
        this.player = player;
        this.seed = seed;
        this.random = new Random(seed);
        this.saveStats = saveStats;
        if (width * height - 9 <= bombCount || width * height <= bombCount)
            throw new IllegalArgumentException("bombCount cannot be bigger than width * height");

        this.isFinished = this.isGenerated = false;
        this.bombList = new int[bombCount][2];
        this.corner = corner;
        this.width = width;
        this.height = height;
        this.bombCount = bombCount;
        this.board = new MinesweeperField[width][height];
        if (Bukkit.getScoreboardManager() != null)
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        new BoardScheduler(this).runTaskTimer(plugin, 0, 1);
        draw();

    }

    @Override
    public void draw() {
        this.draw(this.viewers);
    }

    @Override
    public void draw(@NotNull List<Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if (painter != null)
                painter.drawField(this, players2);
        });
    }

    @Override
    public @NotNull Map<Painter<MinesweeperField>, List<Player>> getCurrentPlayerPainters(@NotNull List<Player> viewers) {
        Map<Class<? extends Painter<MinesweeperField>>, List<Player>> map1 = new HashMap<>();

        viewers.forEach(player -> {
            Class<? extends Painter<MinesweeperField>> painterClass = Painter.loadPainterClass(player);
            map1.computeIfAbsent(painterClass, p -> new ArrayList<>()).add(player);
        });

        Class<? extends Painter<MinesweeperField>> playerClass = Painter.loadPainterClass(player);
        map1.computeIfAbsent(playerClass, p -> new ArrayList<>()).add(player);

        Map<Painter<MinesweeperField>, List<Player>> map2 = new HashMap<>();
        map1.forEach((painterClass, players) -> {
            Painter<?> painter = Painter.PAINTER_MAP.get(MinesweeperField.class).get(painterClass);
            if (painter instanceof MinesweeperPainter minesweeperPainter)
                map2.put(minesweeperPainter, players);
        });
        return map2;
    }

    @Override
    public boolean isFinished() {
        return this.isFinished;
    }

    @Override
    public boolean isLose() {
        return !win;
    }

    @Override
    public MinesweeperField[][] getBoard() {
        return this.board;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull List<Player> getViewers() {
        return viewers;
    }

    @Override
    public void addViewer(@NotNull Player player) {
        this.viewers.add(player);
        draw(Collections.singletonList(player));
        setScoreBoard(player);
    }

    @Override
    public void setScoreBoard(@NotNull Player player) {
        if (scoreboard == null || isFinished)
            return;

        player.setScoreboard(scoreboard);
    }

    @Override
    public void removeViewer(Player player) {
        this.viewers.remove(player);
    }

    @Override
    public void clearViewers() {
        this.viewers.clear();
    }

    @Override
    public void drawBlancField() {
        this.drawBlancField(viewers);
    }

    @Override
    public void drawBlancField(@NotNull List<Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if (painter != null)
                painter.drawBlancField(this, players2);
        });
    }

    @Override
    public boolean isGenerated() {
        return this.isGenerated;
    }

    @Override
    public void checkField(int x, int y) throws BombExplodeException {
        checkField(x, y, true);
    }

    @Override
    public void checkField(int x, int y, boolean b) throws BombExplodeException {
        x = Math.abs(this.corner.getBlockX() - x);
        y = Math.abs(this.corner.getBlockZ() - y);

        boolean isGenerated1 = this.isGenerated;

        if (!isGenerated1)
            generateBoard(x, y);

        SurfaceDiscoverer.uncoverFields(this, x, y);

        if (b)
            startStarted();
    }

    @Override
    public void startStarted() {
        if (this.started == 0)
            this.started = System.currentTimeMillis();
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public @NotNull List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>(viewers);

        list.add(player);

        return list;
    }

    @Override
    public @Nullable MinesweeperField getField(@NotNull Location location) {
        int x = Math.abs(this.corner.getBlockX() - location.getBlockX());
        int y = Math.abs(this.corner.getBlockZ() - location.getBlockZ());

        return this.getField(x, y);
    }

    @Override
    public @Nullable MinesweeperField getField(int x, int y) {
        if (x >= 0 && x < board.length && y >= 0 && y < board[0].length) {
            return this.board[x][y];
        } else {
            return null;
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public Location getCorner() {
        return this.corner;
    }

    @Override
    public void checkIfWon() {
        for (MinesweeperField[] fields : this.board)
            for (MinesweeperField field : fields)
                if (field == null || (field.isCovered() && !field.isBomb()))
                    return;

        win();
    }

    @Override
    public void win() {
        this.win = true;
        long now = System.currentTimeMillis();
        duration = getActualTimeNeeded(now);
        String actualTimeNeededString = getActualTimeNeededString(now);

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().getPluginManager().callEvent(new BoardWinEvent(this, player)));

        this.finish(true, true, duration);

        this.player.sendMessage(language.getString("message_win"));
        this.player.sendMessage(language.getString("field_desc", String.valueOf(this.width), String.valueOf(this.height), String.valueOf(this.bombCount)));
        this.player.sendMessage(language.getString("message_time_needed", actualTimeNeededString));
        this.player.sendMessage("Seed: " + seed);

        this.player.sendTitle(ChatColor.DARK_GREEN + language.getString("title_win"), ChatColor.GREEN + language.getString("message_time_needed", actualTimeNeededString), 10, 70, 20);
        PacketUtil.sendSoundEffect(this.player, Sound.UI_TOAST_CHALLENGE_COMPLETE, .5f, this.player.getLocation());
        PacketUtil.sendActionBar(this.player, actualTimeNeededString);
    }

    @Override
    public @NotNull String getActualTimeNeededString(long now) {
        return Time.parse(false, getActualTimeNeeded(now));
    }

    public long getActualTimeNeeded(long now) {
        return this.started == 0 ? 0 : now - this.started;
    }

    @Override
    public long finish(boolean won, boolean saveStats, long time) {
        if (isFinished)
            return time;

        this.isFinished = true;

        if (saveStats && this.saveStats) {
            GameStatistic gameStatistic = new GameStatistic(player.getUniqueId().toString(), started, time, bombCount, width + "x" + height, setSeed, seed, startX, startY, won);
            gameStatistic.save(connectionBuilder);
        }
        return time;
    }

    @Override
    public void lose() {
        this.win = false;
        duration = finish(false);

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().getPluginManager().callEvent(new BoardLoseEvent(this, player)));

        getCurrentPlayerPainters().forEach((painter, players) -> {
            if (painter != null)
                ((MinesweeperPainter) painter).drawBombs(this, players);
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            for (Player p : this.viewers) {
                PacketUtil.sendSoundEffect(p, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, p.getLocation());
                PacketUtil.sendParticleEffect(p,
                                              corner.clone().add(((double) this.width) / 2, 1, (double) this.height / 2),
                                              Particle.EXPLOSION_LARGE,
                                              (float) this.width / 5,
                                              (float) this.height / 5,
                                              this.width * this.height);
            }
        }, 20);
    }

    @Override
    public long finish(boolean won) {
        return this.finish(won, true);
    }

    @Override
    public @NotNull Map<Painter<MinesweeperField>, List<Player>> getCurrentPlayerPainters() {
        return getCurrentPlayerPainters(this.viewers);
    }

    @Override
    public long finish(boolean won, boolean saveStats) {
        return this.finish(won, saveStats, getActualTimeNeeded());
    }

    @Override
    public void breakGame() {
        finish(false);
    }

    @Override
    public void updateScoreBoard() {
        Team flagCounter = scoreboard.getTeam("flagCounter");
        if (flagCounter != null)
            flagCounter.setSuffix(ChatColor.GREEN + getFlagCounterString());
    }

    @Override
    public boolean isBlockOutsideGame(@NotNull Location location) {
        return IsBetween.isOutside2D(corner, width, height, location)
                || IsBetween.isOutside(corner.getBlockY(), corner.getBlockY() + 1, location.getY());
    }

    @Override
    public void removeScoreBoard(@NotNull Player player) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager != null)
            player.setScoreboard(scoreboardManager.getNewScoreboard());
    }

    @Override
    public void highlightBlocksAround(MinesweeperField field) {
        List<MinesweeperField> surroundings = new ArrayList<>();
        SURROUNDINGS.forEach(ints -> {
            MinesweeperField relativeTo = field.getRelativeTo(ints[0], ints[1]);

            if (relativeTo != null && relativeTo.isCovered() && !relativeTo.isMarked())
                surroundings.add(relativeTo);
        });

        getCurrentPlayerPainters().forEach((painter, players) -> painter.highlightFields(surroundings, players, game.getGameManager().getRemoveMarkerScheduler()));
    }

    @Override
    public int compareTo(@NotNull Board other) {
        if (isWin() && other.isWin()) {
            if (getDuration() != null && other.getDuration() != null)
                return getDuration().compareTo(other.getDuration());
            return 0;
        } else if (isWin() ^ other.isWin())
            return isWin() ? -1 : 1;

        if (getDuration() == null || other.getDuration() == null)
            return 0;

        if (other instanceof MinesweeperBoard board1)
            return calculateFlagScore() < board1.calculateFlagScore() ? 1
                    : (calculateFlagScore() == board1.calculateFlagScore())
                    ? getDuration().compareTo(other.getDuration())
                    : -1;
        else return getDuration().compareTo(other.getDuration());

    }

    public int calculateFlagScore() {
        int flagScore = 0;

        for (MinesweeperField[] fields : this.board)
            for (MinesweeperField field : fields)
                if (field.isMarked())
                    flagScore += field.isBomb() ? 1 : -1;

        return flagScore;
    }

    @Override
    public boolean isWin() {
        return win;
    }

    @Override
    public Long getDuration() {
        return duration;
    }

    @Override
    public Class<MinesweeperField> getFieldClass() {
        return MinesweeperField.class;
    }

    @Override
    public Class<? extends Painter<?>> getPainterClass() {
        return MinesweeperPainter.class;
    }

    private long getActualTimeNeeded() {
        return this.getActualTimeNeeded(System.currentTimeMillis());
    }

    private void generateBoard(int x, int y) {
        if (this.isGenerated)
            throw new RuntimeException(language.getString("error_already_generated"));

        startX = x;
        startY = y;

        boolean[][] cache = new boolean[this.width][this.height];
        int[][] ints = new int[this.width][this.height];

        List<int[]> freeFields = new ArrayList<>();
        for (int i = 0; i < cache.length; i++) {
            for (int j = 0; j < cache[i].length; j++) {
                if (!couldBombSpawn(x, y, i, j)) {
                    freeFields.add(new int[]{i, j});
                    cache[i][j] = false;
                }
            }
        }

        int flipped = 0;
        while (flipped < bombCount && freeFields.size() > 0) {
            int randomIndex = random.nextInt(freeFields.size());
            int[] randomField = freeFields.get(randomIndex);

            cache[randomField[0]][randomField[1]] = true;
            bombList[flipped] = randomField;
            freeFields.remove(randomIndex);
            flipped++;
        }

        for (int[] ints1 : this.bombList) {
            SURROUNDINGS.parallelStream().forEach(ints2 -> {
                int xCoord = ints1[0] + ints2[0];
                int yCoord = ints1[1] + ints2[1];

                if (xCoord >= 0 && xCoord < this.width && yCoord >= 0 && yCoord < this.height) {
                    ints[xCoord][yCoord]++;
                }
            });
        }

        for (int i = 0; i < cache.length; i++) {
            for (int j = 0; j < cache[i].length; j++) {
                this.board[i][j] = new MinesweeperField(this, i, j, cache[i][j], ints[i][j]);
            }
        }

        this.isGenerated = true;
        initScoreboard();
    }

    private boolean couldBombSpawn(int x, int y, int possibleX, int possibleY) {
        return Math.abs(x - possibleX) <= 1 && Math.abs(y - possibleY) <= 1;
    }

    private void initScoreboard() {
        if (scoreboard != null) {
            Objective objective = scoreboard.registerNewObjective("RetroGames", Criteria.DUMMY, ChatColor.AQUA + "RetroGames");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            Team difficulty = scoreboard.registerNewTeam("difficulty");
            difficulty.addEntry(ChatColor.GREEN + language.getString(getGame().getDifficulty()));
            difficulty.setPrefix(ChatColor.GRAY + "Difficulty:     ");
            objective.getScore(ChatColor.GREEN + language.getString(getGame().getDifficulty())).setScore(15);

            Team size = scoreboard.registerNewTeam("size");
            size.addEntry(ChatColor.GREEN + " " + width + "x" + height);
            size.setPrefix(ChatColor.GRAY + "MinesweeperBoard size:  ");
            objective.getScore(ChatColor.GREEN + " " + width + "x" + height).setScore(14);

            Team flagBombCounter = scoreboard.registerNewTeam("flagCounter");
            flagBombCounter.addEntry(ChatColor.GRAY + "Flags/Bombs: ");
            flagBombCounter.setSuffix(ChatColor.GREEN + getFlagCounterString());
            objective.getScore(ChatColor.GRAY + "Flags/Bombs: ").setScore(13);
        }

        getAllPlayers().forEach(this::setScoreBoard);
    }

    private @NotNull String getFlagCounterString() {
        return (bombCount < getCurrentFlagCount() ? ChatColor.DARK_RED : ChatColor.GREEN) + "" + getCurrentFlagCount() + ChatColor.GREEN + "/" + bombCount;
    }

    public int getCurrentFlagCount() {
        return Arrays.stream(this.board)
                .mapToInt(fields -> (int) Arrays.stream(fields).filter(MinesweeperField::isMarked).count())
                .sum();
    }

    public void checkNumber(int x, int y) throws BombExplodeException {
        x = Math.abs(this.corner.getBlockX() - x);
        y = Math.abs(this.corner.getBlockZ() - y);

        startStarted();

        SurfaceDiscoverer.uncoverFieldsNextToNumber(this, x, y);
    }

    public int[][] getBombList() {
        return bombList;
    }

    private @NotNull String getActualTimeNeededString() {
        return Time.parse(false, getActualTimeNeeded());
    }

    public int getBombCount() {
        return bombCount;
    }

    private static class BoardScheduler extends BukkitRunnable {

        private final MinesweeperBoard board;

        private BoardScheduler(MinesweeperBoard board) {
            this.board = board;
        }

        @Override
        public void run() {
            if (board.isFinished) {
                cancel();
                board.getAllPlayers().forEach(board::removeScoreBoard);
                return;
            }
            if (board.isGenerated)
                board.updateScoreBoard();

            board.getViewers().forEach(player1 -> {
                if (!player1.equals(board.player))
                    PacketUtil.sendActionBar(player1, ChatColor.GRAY + "Du beobachtet: " + ChatColor.GREEN + board.player.getName());
            });

            PacketUtil.sendActionBar(board.player, board.getActualTimeNeededString());

        }

    }

}
