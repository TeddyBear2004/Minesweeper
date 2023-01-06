package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddy.minesweeper.game.statistic.GameStatistic;
import de.teddy.minesweeper.util.ConnectionBuilder;
import de.teddy.minesweeper.util.IsBetween;
import de.teddy.minesweeper.util.Language;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class Board {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss:SSS");
    public static boolean notTest = true;
    public final Game map;
    private final Plugin plugin;
    private final Language language;
    private final List<Player> viewers = new LinkedList<>();
    private final int width;
    private final int height;
    private final int bombCount;
    private final Location corner;
    private final Field[][] board;
    private final int[][] bombList;
    private final Player player;
    private final Random random;
    private final boolean saveStats;
    private final long seed;
    private final ConnectionBuilder connectionBuilder;
    boolean setSeed;
    private boolean win = false;
    private long started;
    private boolean isGenerated;
    private boolean isFinished;
    private Scoreboard scoreboard;
    private int startX;
    private int startY;


    public Board(Plugin plugin, Language language, ConnectionBuilder connectionBuilder, Game map, int width, int height, int bombCount, Location corner, Player player, long seed, boolean setSeed, boolean saveStats) {
        this.connectionBuilder = connectionBuilder;
        this.setSeed = setSeed;
        this.plugin = plugin;
        this.language = language;
        this.map = map;
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

        if (notTest) {
            new BoardScheduler(this).runTaskTimer(plugin, 0, 1);
            draw();
        }
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

    public void finish(boolean won) {
        this.finish(won, true);
    }

    public void finish(boolean won, boolean saveStats) {
        this.finish(won, saveStats, getActualTimeNeeded());
    }

    public void finish(boolean won, boolean saveStats, long time) {
        this.isFinished = true;

        if (saveStats && this.saveStats) {
            GameStatistic gameStatistic = new GameStatistic(player.getUniqueId().toString(), started, time, bombCount, width + "x" + height, setSeed, seed, startX, startY, won);
            gameStatistic.save(connectionBuilder);
        }
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public boolean isWin() {
        return win;
    }

    public Field[][] getBoard() {
        return this.board;
    }

    public int getBombCount() {
        return bombCount;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Player> getViewers() {
        return viewers;
    }

    public List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>(viewers);

        list.add(player);

        return list;
    }

    public void addViewer(Player player) {
        this.viewers.add(player);
        draw(Collections.singletonList(player));
        setScoreBoard(player);
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

    public void drawBlancField(List<Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if (painter != null)
                painter.drawBlancField(this, players2);
        });
    }

    public boolean isGenerated() {
        return this.isGenerated;
    }

    public void draw() {
        this.draw(this.viewers);
    }

    public void draw(List<Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if (painter != null)
                painter.drawField(this, players2);
        });
    }


    public Field getField(int x, int y) {
        if (x >= 0 && x < board.length && y >= 0 && y < board[0].length) {
            return this.board[x][y];
        } else {
            return null;
        }
    }

    public Field getField(Location location) {
        int x = Math.abs(this.corner.getBlockX() - location.getBlockX());
        int y = Math.abs(this.corner.getBlockZ() - location.getBlockZ());

        return this.getField(x, y);
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

    public void startStarted() {
        if (this.started == 0)
            this.started = System.currentTimeMillis();
    }

    public void checkNumber(int x, int y) throws BombExplodeException {
        x = Math.abs(this.corner.getBlockX() - x);
        y = Math.abs(this.corner.getBlockZ() - y);

        startStarted();

        SurfaceDiscoverer.uncoverFieldsNextToNumber(this, x, y);
    }

    private long getActualTimeNeeded() {
        return this.getActualTimeNeeded(System.currentTimeMillis());
    }

    private long getActualTimeNeeded(long now) {
        return this.started == 0 ? 0 : now - this.started;
    }

    private String getActualTimeNeededString() {
        return SIMPLE_DATE_FORMAT.format(new Date(getActualTimeNeeded()));
    }

    private String getActualTimeNeededString(long now) {
        return SIMPLE_DATE_FORMAT.format(new Date(getActualTimeNeeded(now)));
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

    public Map<Painter, List<Player>> getCurrentPlayerPainters(List<Player> viewers) {
        Map<Class<? extends Painter>, List<Player>> map1 = new HashMap<>();
        viewers.forEach(player -> {
            Class<? extends Painter> painterClass = Painter.loadPainterClass(player.getPersistentDataContainer());
            map1.computeIfAbsent(painterClass, p -> new ArrayList<>()).add(player);
        });

        Class<? extends Painter> playerClass = Painter.loadPainterClass(player.getPersistentDataContainer());
        map1.computeIfAbsent(playerClass, p -> new ArrayList<>()).add(player);

        Map<Painter, List<Player>> map2 = new HashMap<>();
        map1.forEach((painterClass, players) -> map2.put(Game.PAINTER_MAP.get(painterClass), players));
        return map2;
    }

    public Map<Painter, List<Player>> getCurrentPlayerPainters() {
        return getCurrentPlayerPainters(this.viewers);
    }

    public int getCurrentFlagCount() {
        return Arrays.stream(this.board)
                .mapToInt(fields -> (int) Arrays.stream(fields).filter(Field::isMarked).count())
                .sum();
    }

    public void win() {
        this.win = true;
        long now = System.currentTimeMillis();
        long duration = getActualTimeNeeded(now);
        String actualTimeNeededString = getActualTimeNeededString(now);

        this.finish(true, true, duration);

        this.player.sendMessage(language.getString("message_win"));
        this.player.sendMessage(language.getString("field_desc", String.valueOf(this.width), String.valueOf(this.height), String.valueOf(this.bombCount)));
        this.player.sendMessage(language.getString("message_time_needed", actualTimeNeededString));
        this.player.sendMessage("Seed: " + seed);

        this.player.sendTitle(ChatColor.DARK_GREEN + language.getString("title_win"), ChatColor.GREEN + language.getString("message_time_needed", actualTimeNeededString), 10, 70, 20);
        PacketUtil.sendSoundEffect(this.player, Sound.UI_TOAST_CHALLENGE_COMPLETE, .5f, this.player.getLocation());
        PacketUtil.sendActionBar(this.player, actualTimeNeededString);
    }

    public void checkIfWon() {
        for (Field[] fields : this.board)
            for (Field field : fields)
                if (field == null || (field.isCovered() && !field.isBomb()))
                    return;

        win();
    }

    public void breakGame() {
        finish(false);
    }

    public void lose() {
        this.win = false;
        finish(false);

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
            for (int j = -1; j < 2; j++) {
                for (int k = -1; k < 2; k++) {
                    if (!(j == 0 && k == 0)) {
                        int xCoord = ints1[0] + j;
                        int yCoord = ints1[1] + k;

                        if (xCoord >= 0 && xCoord < this.width && yCoord >= 0 && yCoord < this.height) {
                            ints[xCoord][yCoord]++;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < cache.length; i++) {
            for (int j = 0; j < cache[i].length; j++) {
                this.board[i][j] = new Field(this, i, j, cache[i][j], ints[i][j]);
            }
        }

        this.isGenerated = true;
        initScoreboard();
    }


    private boolean couldBombSpawn(int x, int y, int possibleX, int possibleY) {
        return Math.abs(x - possibleX) <= 1 && Math.abs(y - possibleY) <= 1;
    }

    public boolean isBlockOutsideGame(Block block) {
        return !IsBetween.isBetween2D(corner, width, height, block)
                || !IsBetween.isBetween(corner.getBlockY(), corner.getBlockY() + 1, block.getY());
    }

    private void initScoreboard() {
        if (scoreboard != null) {
            Objective objective = scoreboard.registerNewObjective("Minesweeper", Criteria.DUMMY, ChatColor.AQUA + "Minesweeper");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            Team difficulty = scoreboard.registerNewTeam("difficulty");
            difficulty.addEntry(ChatColor.GREEN + map.getDifficulty());
            difficulty.setPrefix(ChatColor.GRAY + "Difficulty:     ");
            objective.getScore(ChatColor.GREEN + map.getDifficulty()).setScore(15);

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

    public void updateScoreBoard() {
        Team flagCounter = scoreboard.getTeam("flagCounter");
        if (flagCounter != null)
            flagCounter.setSuffix(ChatColor.GREEN + getFlagCounterString());
    }

    public void setScoreBoard(Player player) {
        if (scoreboard == null || isFinished)
            return;

        player.setScoreboard(scoreboard);
    }

    private String getFlagCounterString() {
        return (bombCount < getCurrentFlagCount() ? ChatColor.DARK_RED : ChatColor.GREEN) + "" + getCurrentFlagCount() + ChatColor.GREEN + "/" + bombCount;
    }

    public void removeScoreBoard(Player player) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager != null)
            player.setScoreboard(scoreboardManager.getNewScoreboard());
    }

    public void highlightBlocksAround(Field field) {
        getCurrentPlayerPainters().forEach((painter, players) -> painter.highlightField(field, players));
    }

    public static class Field {

        private final boolean isBomb;
        private final int bombCount;
        private final Board board;
        private final int x;
        private final int y;
        private boolean isCovered;
        private MarkType markType;

        public Field(Board board, int x, int y, boolean isBomb, int bombCount) {
            this.board = board;
            this.x = x;
            this.y = y;
            this.isCovered = true;
            this.markType = MarkType.NONE;
            this.isBomb = isBomb;
            this.bombCount = bombCount;
        }

        public int getNeighborCount() {
            return bombCount;
        }

        public void setUncover() {
            this.markType = MarkType.NONE;
            this.isCovered = false;
        }

        public boolean isBomb() {
            return isBomb;
        }

        public boolean isMarked() {
            return !markType.isNone();
        }

        public void reverseMark() {
            PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(board.player);
            if (personalModifier.isEnableMarks().orElse(true))
                this.markType = this.markType.next(board.getPlayer());
            else
                this.markType = MarkType.NONE;

        }

        public Material getMark() {
            return markType.getMaterial();
        }

        public void setMark(MarkType markType) {
            this.markType = markType;
        }

        public boolean isCovered() {
            return isCovered;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Board getBoard() {
            return board;
        }

        public Field getRelativeTo(int i, int j) {
            return board.getField(x + i, y + j);
        }

        public Location getLocation() {
            return board.getCorner().clone().add(x, 0, y);
        }

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
