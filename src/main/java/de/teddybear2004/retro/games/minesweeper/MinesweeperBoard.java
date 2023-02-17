package de.teddybear2004.retro.games.minesweeper;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.game.statistic.GameStatistic;
import de.teddybear2004.retro.games.minesweeper.exceptions.BombExplodeException;
import de.teddybear2004.retro.games.util.ConnectionBuilder;
import de.teddybear2004.retro.games.util.Language;
import de.teddybear2004.retro.games.util.PacketUtil;
import de.teddybear2004.retro.games.util.Time;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static de.teddybear2004.retro.games.minesweeper.SurfaceDiscoverer.SURROUNDINGS;

public class MinesweeperBoard extends Board<MinesweeperField> {

    private final int bombCount;
    private final int[] @NotNull [] bombList;
    private final @NotNull Random random;


    public MinesweeperBoard(@NotNull Plugin plugin, Language language, ConnectionBuilder connectionBuilder, Game game, int width, int height, int bombCount, Location corner, Player player, long seed, boolean setSeed, boolean saveStats, Atelier atelier) {
        super(plugin, language, connectionBuilder, game, width, height, corner, player, seed, setSeed, saveStats, atelier);
        this.random = new Random(seed);
        if (width * height - 9 <= bombCount || width * height <= bombCount)
            throw new IllegalArgumentException("bombCount cannot be bigger than width * height");

        this.bombList = new int[bombCount][2];
        this.bombCount = bombCount;
        initBoard();

        new BoardScheduler(this).runTaskTimer(plugin, 0, 1);

    }

    public void checkNumber(int x, int y) throws BombExplodeException {
        x = Math.abs(getCorner().getBlockX() - x);
        y = Math.abs(getCorner().getBlockZ() - y);

        startStarted();

        SurfaceDiscoverer.uncoverFieldsNextToNumber(this, x, y);
    }

    public int[][] getBombList() {
        return bombList;
    }

    private @NotNull String getActualTimeNeededString() {
        return Time.parse(false, getActualTimeNeeded());
    }

    private long getActualTimeNeeded() {
        return this.getActualTimeNeeded(System.currentTimeMillis());
    }

    public int getBombCount() {
        return bombCount;
    }

    public void removeScoreBoard(@NotNull Player player) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager != null)
            player.setScoreboard(scoreboardManager.getNewScoreboard());
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

        for (MinesweeperField[] fields : this.getBoard())
            for (MinesweeperField field : fields)
                if (field.isMarked())
                    flagScore += field.isBomb() ? 1 : -1;

        return flagScore;
    }

    private static class BoardScheduler extends BukkitRunnable {

        private final MinesweeperBoard board;

        private BoardScheduler(MinesweeperBoard board) {
            this.board = board;
        }

        @Override
        public void run() {
            if (board.isFinished()) {
                cancel();
                board.getAllPlayers().forEach(board::removeScoreBoard);
                return;
            }
            if (board.isGenerated())
                board.updateScoreBoard();

            board.getViewers().forEach(player1 -> {
                if (!player1.equals(board.getPlayer()))
                    PacketUtil.sendActionBar(player1, ChatColor.GRAY + "Du beobachtet: " + ChatColor.GREEN + board.getPlayer().getName());
            });

            PacketUtil.sendActionBar(board.getPlayer(), board.getActualTimeNeededString());

        }

    }    @Override
    public Class<? extends Board> getBoardClass() {
        return MinesweeperBoard.class;
    }

    public MinesweeperField[][] generateBoard(int x, int y) {
        if (this.isGenerated())
            throw new RuntimeException(getLanguage().getString("error_already_generated"));

        setStartX(x);
        setStartY(y);
        MinesweeperField[][] board = new MinesweeperField[getWidth()][getHeight()];

        boolean[][] cache = new boolean[getWidth()][getHeight()];
        int[][] ints = new int[getWidth()][getHeight()];

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

                if (xCoord >= 0 && xCoord < this.getWidth() && yCoord >= 0 && yCoord < this.getWidth()) {
                    ints[xCoord][yCoord]++;
                }
            });
        }

        for (int i = 0; i < cache.length; i++) {
            for (int j = 0; j < cache[i].length; j++) {
                board[i][j] = new MinesweeperField(this, i, j, cache[i][j], ints[i][j]);
            }
        }


        setGenerated(true);
        return board;
    }

    @Override
    public void checkField(int x, int y) {
        try{
            checkFieldWithException(x, y);
        }catch(BombExplodeException e){
            throw new RuntimeException(e);
        }
    }

    public void checkFieldWithException(int x, int y) throws BombExplodeException {
        checkFieldWithException(x, y, true);
    }

    public void checkFieldWithException(int x, int y, boolean b) throws BombExplodeException {
        x = Math.abs(getCorner().getBlockX() - x);
        y = Math.abs(getCorner().getBlockZ() - y);

        boolean isGenerated1 = this.isGenerated();

        if (!isGenerated1)
            generateBoard(x, y);

        SurfaceDiscoverer.uncoverFields(this, x, y);

        if (b)
            startStarted();
    }

    private boolean couldBombSpawn(int x, int y, int possibleX, int possibleY) {
        return Math.abs(x - possibleX) <= 1 && Math.abs(y - possibleY) <= 1;
    }

    private @NotNull String getFlagCounterString() {
        return (bombCount < getCurrentFlagCount() ? ChatColor.DARK_RED : ChatColor.GREEN) + "" + getCurrentFlagCount() + ChatColor.GREEN + "/" + bombCount;
    }

    public int getCurrentFlagCount() {
        return Arrays.stream(this.getBoard())
                .mapToInt(fields -> (int) Arrays.stream(fields).filter(MinesweeperField::isMarked).count())
                .sum();
    }

    @Override
    public void checkField(int x, int y, boolean b) {
        try{
            this.checkFieldWithException(x, y, b);
        }catch(BombExplodeException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkIfWon() {
        for (MinesweeperField[] fields : this.getBoard())
            for (MinesweeperField field : fields)
                if (field == null || (field.isCovered() && !field.isBomb()))
                    return;

        win();
    }

    @Override
    public void sendWinMessages(String actualTimeNeededString) {
        this.getPlayer().sendMessage(getLanguage().getString("message_win"));
        this.getPlayer().sendMessage(getLanguage().getString("field_desc", String.valueOf(getWidth()), String.valueOf(this.getHeight()), String.valueOf(this.bombCount)));
        this.getPlayer().sendMessage(getLanguage().getString("message_time_needed", actualTimeNeededString));
        this.getPlayer().sendMessage("Seed: " + getSeed());

        this.getPlayer().sendTitle(ChatColor.DARK_GREEN + getLanguage().getString("title_win"), ChatColor.GREEN + getLanguage().getString("message_time_needed", actualTimeNeededString), 10, 70, 20);
        PacketUtil.sendSoundEffect(getPlayer(), Sound.UI_TOAST_CHALLENGE_COMPLETE, .5f, getPlayer().getLocation());
        PacketUtil.sendActionBar(getPlayer(), actualTimeNeededString);

    }

    @Override
    public void saveStats(boolean won, boolean saveStats, long time) {
        GameStatistic gameStatistic = new GameStatistic(getPlayer().getUniqueId().toString(), getStarted(), time, bombCount, getWidth() + "x" + getHeight(), isSetSeed(), getSeed(), getStartX(), getStartY(), won);
        gameStatistic.save(getConnectionBuilder());
    }

    @Override
    public void lose() {
        super.lose();

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {

            for (Player p : this.getViewers()) {
                PacketUtil.sendSoundEffect(p, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, p.getLocation());
                PacketUtil.sendParticleEffect(p,
                                              getCorner().clone().add(((double) this.getWidth()) / 2, 1, (double) this.getHeight() / 2),
                                              Particle.EXPLOSION_LARGE,
                                              (float) this.getWidth() / 5,
                                              (float) this.getHeight() / 5,
                                              this.getWidth() * this.getHeight());
            }
        }, 20);
    }

    @Override
    public void highlightBlocksAround(MinesweeperField field) {
        List<MinesweeperField> surroundings = new ArrayList<>();
        SURROUNDINGS.forEach(ints -> {
            MinesweeperField relativeTo = field.getRelativeTo(ints[0], ints[1]);

            if (relativeTo != null && relativeTo.isCovered() && !relativeTo.isMarked())
                surroundings.add(relativeTo);
        });

        getCurrentPlayerPainters().forEach((painter, players) -> painter.highlightFields(surroundings, players, getGame().getGameManager().getRemoveMarkerScheduler()));

    }

    public void initScoreboard() {
        if (getScoreboard() != null) {
            Objective objective = getScoreboard().registerNewObjective("RetroGames", Criteria.DUMMY, ChatColor.AQUA + "RetroGames");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            Team difficulty = getScoreboard().registerNewTeam("difficulty");
            difficulty.addEntry(ChatColor.GREEN + getLanguage().getString(getGame().getDifficulty()));
            difficulty.setPrefix(ChatColor.GRAY + "Difficulty:     ");
            objective.getScore(ChatColor.GREEN + getLanguage().getString(getGame().getDifficulty())).setScore(15);

            Team size = getScoreboard().registerNewTeam("size");
            size.addEntry(ChatColor.GREEN + " " + getWidth() + "x" + getHeight());
            size.setPrefix(ChatColor.GRAY + "MinesweeperBoard size:  ");
            objective.getScore(ChatColor.GREEN + " " + getWidth() + "x" + getHeight()).setScore(14);

            Team flagBombCounter = getScoreboard().registerNewTeam("flagCounter");
            flagBombCounter.addEntry(ChatColor.GRAY + "Flags/Bombs: ");
            flagBombCounter.setSuffix(ChatColor.GREEN + getFlagCounterString());
            objective.getScore(ChatColor.GRAY + "Flags/Bombs: ").setScore(13);
        }

        getAllPlayers().forEach(this::setScoreBoard);
    }

    @Override
    public void updateScoreBoard() {
        Team flagCounter = getScoreboard().getTeam("flagCounter");
        if (flagCounter != null)
            flagCounter.setSuffix(ChatColor.GREEN + getFlagCounterString());
    }

    public void setScoreBoard(@NotNull Player player) {
        if (getScoreboard() == null || isFinished())
            return;

        player.setScoreboard(getScoreboard());
    }




}
