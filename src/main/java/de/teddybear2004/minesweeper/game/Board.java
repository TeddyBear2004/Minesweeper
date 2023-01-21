package de.teddybear2004.minesweeper.game;

import de.teddy.minesweeper.game.painter.Painter;
import de.teddybear2004.minesweeper.game.event.BoardLoseEvent;
import de.teddybear2004.minesweeper.game.event.BoardWinEvent;
import de.teddybear2004.minesweeper.game.exceptions.BombExplodeException;
import de.teddybear2004.minesweeper.game.statistic.GameStatistic;
import de.teddybear2004.minesweeper.util.ConnectionBuilder;
import de.teddybear2004.minesweeper.util.IsBetween;
import de.teddybear2004.minesweeper.util.Language;
import de.teddybear2004.minesweeper.util.PacketUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.*;

public class Board implements Comparable<Board> {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss:SSS");
    private final Game game;
    private final @NotNull Plugin plugin;
    private final Language language;
    private final List<Player> viewers = new LinkedList<>();
    private final int width;
    private final int height;
    private final int bombCount;
    private final Location corner;
    private final Field[] @NotNull [] board;
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


    public Board(@NotNull Plugin plugin, Language language, ConnectionBuilder connectionBuilder, Game game, int width, int height, int bombCount, Location corner, Player player, long seed, boolean setSeed, boolean saveStats) {
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
        this.board = new Field[width][height];
        if (Bukkit.getScoreboardManager() != null)
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        new BoardScheduler(this).runTaskTimer(plugin, 0, 1);
        draw();

    }

    public void draw() {
        this.draw(this.viewers);
    }

    public void draw(@NotNull List<Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if (painter != null)
                painter.drawField(this, players2);
        });
    }

    public @NotNull Map<Painter, List<Player>> getCurrentPlayerPainters(@NotNull List<Player> viewers) {
        Map<Class<? extends Painter>, List<Player>> map1 = new HashMap<>();
        viewers.forEach(player -> {
            Class<? extends Painter> painterClass = Painter.loadPainterClass(player);
            map1.computeIfAbsent(painterClass, p -> new ArrayList<>()).add(player);
        });

        Class<? extends Painter> playerClass = Painter.loadPainterClass(player);
        map1.computeIfAbsent(playerClass, p -> new ArrayList<>()).add(player);

        Map<Painter, List<Player>> map2 = new HashMap<>();
        map1.forEach((painterClass, players) -> map2.put(Painter.PAINTER_MAP.get(painterClass), players));
        return map2;
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

    public boolean isFinished() {
        return this.isFinished;
    }

    public boolean isLose() {
        return !win;
    }

    public Field[][] getBoard() {
        return this.board;
    }

    public Player getPlayer() {
        return player;
    }

    public @NotNull List<Player> getViewers() {
        return viewers;
    }

    public void addViewer(@NotNull Player player) {
        this.viewers.add(player);
        draw(Collections.singletonList(player));
        setScoreBoard(player);
    }

    public void drawBlancField(@NotNull List<Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if (painter != null)
                painter.drawBlancField(this, players2);
        });
    }

    public void removeViewer(Player player) {
        this.viewers.remove(player);
    }

    public void clearViewers() {
        this.viewers.clear();
    }

    public void drawBlancField() {
        this.drawBlancField(viewers);
    }

    private void generateBoard(int x, int y) {
        if (this.isGenerated)
            throw new RuntimeException(language.getString("error_already_generated"));

        startX = x;
        startY = y;

        boolean[][] cache = new boolean[this.width][this.height];
        int[][] ints = new int[this.width][this.height];

        for (int i = 0; i < this.bombCount; i++) {
            int randWidth, randHeight;

            do{
                randWidth = random.nextInt(this.width);
                randHeight = random.nextInt(this.height);
            }while (cache[randWidth][randHeight] || couldBombSpawn(x, y, randWidth, randHeight));

            cache[randWidth][randHeight] = true;
            this.bombList[i][0] = randWidth;
            this.bombList[i][1] = randHeight;
        }

        for (int[] ints1 : this.bombList) {
            SurfaceDiscoverer.SURROUNDINGS.parallelStream().forEach(ints2 -> {
                int xCoord = ints1[0] + ints2[0];
                int yCoord = ints1[1] + ints2[1];

                if (xCoord >= 0 && xCoord < this.width && yCoord >= 0 && yCoord < this.height) {
                    ints[xCoord][yCoord]++;
                }
            });
        }

        for (int i = 0; i < cache.length; i++) {
            for (int j = 0; j < cache[i].length; j++) {
                this.board[i][j] = new Field(this, i, j, cache[i][j], ints[i][j]);
            }
        }

        this.isGenerated = true;
        initScoreboard();
    }

    public boolean isGenerated() {
        return this.isGenerated;
    }

    public void checkField(int x, int y) throws BombExplodeException {
        checkField(x, y, true);
    }

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

    private boolean couldBombSpawn(int x, int y, int possibleX, int possibleY) {
        return Math.abs(x - possibleX) <= 1 && Math.abs(y - possibleY) <= 1;
    }

    public void startStarted() {
        if (this.started == 0)
            this.started = System.currentTimeMillis();
    }

    private void initScoreboard() {
        if (scoreboard != null) {
            Objective objective = scoreboard.registerNewObjective("Minesweeper", Criteria.DUMMY, ChatColor.AQUA + "Minesweeper");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            Team difficulty = scoreboard.registerNewTeam("difficulty");
            difficulty.addEntry(ChatColor.GREEN + getGame().getDifficulty());
            difficulty.setPrefix(ChatColor.GRAY + "Difficulty:     ");
            objective.getScore(ChatColor.GREEN + getGame().getDifficulty()).setScore(15);

            Team size = scoreboard.registerNewTeam("size");
            size.addEntry(ChatColor.GREEN + " " + width + "x" + height);
            size.setPrefix(ChatColor.GRAY + "Board size:  ");
            objective.getScore(ChatColor.GREEN + " " + width + "x" + height).setScore(14);

            Team flagBombCounter = scoreboard.registerNewTeam("flagCounter");
            flagBombCounter.addEntry(ChatColor.GRAY + "Flags/Bombs: ");
            flagBombCounter.setSuffix(ChatColor.GREEN + getFlagCounterString());
            objective.getScore(ChatColor.GRAY + "Flags/Bombs: ").setScore(13);
        }

        getAllPlayers().forEach(this::setScoreBoard);
    }

    public Game getGame() {
        return game;
    }

    private @NotNull String getFlagCounterString() {
        return (bombCount < getCurrentFlagCount() ? ChatColor.DARK_RED : ChatColor.GREEN) + "" + getCurrentFlagCount() + ChatColor.GREEN + "/" + bombCount;
    }

    public @NotNull List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>(viewers);

        list.add(player);

        return list;
    }

    public void setScoreBoard(@NotNull Player player) {
        if (scoreboard == null || isFinished)
            return;

        player.setScoreboard(scoreboard);
    }

    public int getCurrentFlagCount() {
        return Arrays.stream(this.board)
                .mapToInt(fields -> (int) Arrays.stream(fields).filter(Field::isMarked).count())
                .sum();
    }

    public void checkNumber(int x, int y) throws BombExplodeException {
        x = Math.abs(this.corner.getBlockX() - x);
        y = Math.abs(this.corner.getBlockZ() - y);

        startStarted();

        SurfaceDiscoverer.uncoverFieldsNextToNumber(this, x, y);
    }

    public @Nullable Field getField(@NotNull Location location) {
        int x = Math.abs(this.corner.getBlockX() - location.getBlockX());
        int y = Math.abs(this.corner.getBlockZ() - location.getBlockZ());

        return this.getField(x, y);
    }

    public @Nullable Field getField(int x, int y) {
        if (x >= 0 && x < board.length && y >= 0 && y < board[0].length) {
            return this.board[x][y];
        } else {
            return null;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Location getCorner() {
        return this.corner;
    }

    public int[][] getBombList() {
        return bombList;
    }

    private @NotNull String getActualTimeNeededString() {
        return SIMPLE_DATE_FORMAT.format(new Date(getActualTimeNeeded()));
    }

    public void checkIfWon() {
        for (Field[] fields : this.board)
            for (Field field : fields)
                if (field == null || (field.isCovered() && !field.isBomb()))
                    return;

        win();
    }

    public void win() {
        this.win = true;
        long now = System.currentTimeMillis();
        duration = getActualTimeNeeded(now);
        String actualTimeNeededString = getActualTimeNeededString(now);

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().getPluginManager().callEvent(new BoardWinEvent(this, player, duration, getBombCount())));

        this.finish(true, true, duration);

        this.player.sendMessage(language.getString("message_win"));
        this.player.sendMessage(language.getString("field_desc", String.valueOf(this.width), String.valueOf(this.height), String.valueOf(this.bombCount)));
        this.player.sendMessage(language.getString("message_time_needed", actualTimeNeededString));
        this.player.sendMessage("Seed: " + seed);

        this.player.sendTitle(ChatColor.DARK_GREEN + language.getString("title_win"), ChatColor.GREEN + language.getString("message_time_needed", actualTimeNeededString), 10, 70, 20);
        PacketUtil.sendSoundEffect(this.player, Sound.UI_TOAST_CHALLENGE_COMPLETE, .5f, this.player.getLocation());
        PacketUtil.sendActionBar(this.player, actualTimeNeededString);
    }

    private long getActualTimeNeeded(long now) {
        return this.started == 0 ? 0 : now - this.started;
    }

    private @NotNull String getActualTimeNeededString(long now) {
        return SIMPLE_DATE_FORMAT.format(new Date(getActualTimeNeeded(now)));
    }

    public int getBombCount() {
        return bombCount;
    }

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

    public void lose() {
        this.win = false;
        duration = finish(false);

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().getPluginManager().callEvent(new BoardLoseEvent(this, player, duration, calculateFlagScore())));

        getCurrentPlayerPainters().forEach((painter, players) -> {
            if (painter != null)
                painter.drawBombs(this, players);
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

    public void breakGame() {
        finish(false);
    }

    public long finish(boolean won) {
        return this.finish(won, true);
    }

    public int calculateFlagScore() {
        int flagScore = 0;

        for (Field[] fields : this.board)
            for (Field field : fields)
                if (field.isMarked())
                    flagScore += field.isBomb() ? 1 : -1;

        return flagScore;
    }

    public long finish(boolean won, boolean saveStats) {
        return this.finish(won, saveStats, getActualTimeNeeded());
    }

    private long getActualTimeNeeded() {
        return this.getActualTimeNeeded(System.currentTimeMillis());
    }

    public @NotNull Map<Painter, List<Player>> getCurrentPlayerPainters() {
        return getCurrentPlayerPainters(this.viewers);
    }

    protected void generateBoard(Field[] @NotNull [] board) {
        if (this.isGenerated)
            throw new RuntimeException(language.getString("error_already_generated"));

        System.arraycopy(board, 0, this.board, 0, board.length);
        this.isGenerated = true;
    }

    public void updateScoreBoard() {
        Team flagCounter = scoreboard.getTeam("flagCounter");
        if (flagCounter != null)
            flagCounter.setSuffix(ChatColor.GREEN + getFlagCounterString());
    }

    public boolean isBlockOutsideGame(@NotNull Block block) {
        return IsBetween.isOutside2D(corner, width, height, block)
                || IsBetween.isOutside(corner.getBlockY(), corner.getBlockY() + 1, block.getY());
    }

    public void removeScoreBoard(@NotNull Player player) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager != null)
            player.setScoreboard(scoreboardManager.getNewScoreboard());
    }

    public void highlightBlocksAround(Field field) {
        getCurrentPlayerPainters().forEach((painter, players) -> painter.highlightField(field, players));
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

        return calculateFlagScore() < other.calculateFlagScore() ? 1
                : (calculateFlagScore() == other.calculateFlagScore())
                ? getDuration().compareTo(other.getDuration())
                : -1;

    }

    public boolean isWin() {
        return win;
    }

    public Long getDuration() {
        return duration;
    }

    private static class BoardScheduler extends BukkitRunnable {

        private final Board board;

        private BoardScheduler(Board board) {
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
            PacketUtil.sendActionBar(board.player, board.getActualTimeNeededString());
        }

    }

}
