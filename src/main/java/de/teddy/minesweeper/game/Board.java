package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddy.minesweeper.util.Language;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class Board {

    public static boolean notTest = true;
    public final Game map;
    private final Plugin plugin;
    private final Language language;
    private final List<Player> viewers = new LinkedList<>();
    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("mm:ss:SSS");
    private final int width;
    private final int height;
    private final int bombCount;
    private final Location corner;
    private final Field[][] board;
    private final Point2D[] bombList;
    private final Player player;
    private final Random random = new Random();
    private boolean win = false;
    private long started;
    private boolean isGenerated;
    private boolean isFinished;

    public Board(Plugin plugin, Language language, Game map, int width, int height, int bombCount, Location corner, Player player) {
        this.plugin = plugin;
        this.language = language;
        this.map = map;
        this.player = player;
        if (width * height - 9 <= bombCount || width * height <= bombCount)
            throw new IllegalArgumentException("bombCount cannot be bigger than width * height");

        this.isFinished = this.isGenerated = false;
        this.bombList = new Point2D[bombCount];
        this.corner = corner;
        this.width = width;
        this.height = height;
        this.bombCount = bombCount;
        this.board = new Field[width][height];
        if (notTest) {
            new ActionBarScheduler(this).runTaskTimer(plugin, 0, 1);
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

    public void finish() {
        this.isFinished = true;
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

    public Player getPlayer() {
        return player;
    }

    public List<Player> getViewers() {
        return viewers;
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
        try{
            return this.board[x][y];
        }catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public Field getField(Location location) {
        int x = Math.abs(this.corner.getBlockX() - location.getBlockX());
        int y = Math.abs(this.corner.getBlockZ() - location.getBlockZ());

        return this.getField(x, y);
    }

    public void checkField(int x, int y) throws BombExplodeException {
        x = Math.abs(this.corner.getBlockX() - x);
        y = Math.abs(this.corner.getBlockZ() - y);

        boolean isGenerated1 = this.isGenerated;

        if (!isGenerated1)
            generateBoard(x, y);

        SurfaceDiscoverer.uncoverFields(this, x, y);

        if (!isGenerated1)
            this.started = System.currentTimeMillis();
    }

    public void checkNumber(int x, int y) throws BombExplodeException {
        x = Math.abs(this.corner.getBlockX() - x);
        y = Math.abs(this.corner.getBlockZ() - y);

        SurfaceDiscoverer.uncoverFieldsNextToNumber(this, x, y);
    }

    private long getActualTimeNeeded() {
        return this.started == 0 ? 0 : System.currentTimeMillis() - this.started;
    }

    private String getActualTimeNeededString() {
        return dateTimeFormatter.format(new Date(getActualTimeNeeded()));
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

    public Point2D[] getBombList() {
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

    public void win() {
        this.win = true;
        this.finish();

        this.player.sendMessage(language.getString("message_win"));
        this.player.sendMessage(language.getString("field_desc", String.valueOf(this.width), String.valueOf(this.height), String.valueOf(this.bombCount)));
        this.player.sendMessage(language.getString("message_time_needed", getActualTimeNeededString()));

        this.player.sendTitle(ChatColor.DARK_GREEN + language.getString("title_win"), ChatColor.GREEN + language.getString("message_time_needed", getActualTimeNeededString()), 10, 70, 20);
        PacketUtil.sendSoundEffect(this.player, Sound.UI_TOAST_CHALLENGE_COMPLETE, .5f, this.player.getLocation());
        PacketUtil.sendActionBar(this.player, getActualTimeNeededString());
    }

    public void checkIfWon() {
        for (Field[] fields : this.board)
            for (Field field : fields)
                if (field.isCovered() && !field.isBomb())
                    return;

        win();
    }

    public void breakGame() {
        finish();
    }

    public void lose() {
        this.win = false;
        finish();

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

        boolean[][] cache = new boolean[this.height][this.width];
        int[][] ints = new int[this.height][this.width];

        for (int i = 0; i < this.bombCount; i++) {
            int randWidth, randHeight;

            do{
                randWidth = random.nextInt(this.width);
                randHeight = random.nextInt(this.height);
            }while (cache[randWidth][randHeight] || couldBombSpawn(x, y, randWidth, randHeight));

            cache[randWidth][randHeight] = true;
            this.bombList[i] = new Point(randWidth, randHeight);
        }

        for (Point2D point2D : this.bombList) {
            for (int j = -1; j < 2; j++) {
                for (int k = -1; k < 2; k++) {
                    if (!(j == 0 && k == 0)) {
                        int xCoord = (int) (point2D.getX() + j);
                        int yCoord = (int) (point2D.getY() + k);

                        if (xCoord >= 0 && xCoord < this.height && yCoord >= 0 && yCoord < this.width) {
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
    }


    private boolean couldBombSpawn(int x, int y, int possibleX, int possibleY) {
        return Math.abs(x - possibleX) <= 1 && Math.abs(y - possibleY) <= 1;
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
            this.markType = this.markType.next(board.getPlayer());
        }

        public Material getMark() {
            return markType.getMaterial();
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

    }

    private static class ActionBarScheduler extends BukkitRunnable {

        private final Board board;

        private ActionBarScheduler(Board board) {
            this.board = board;
        }

        @Override
        public void run() {
            if (board.isFinished) {
                cancel();
                return;
            }
            PacketUtil.sendActionBar(board.player, board.getActualTimeNeededString());
        }

    }

}
