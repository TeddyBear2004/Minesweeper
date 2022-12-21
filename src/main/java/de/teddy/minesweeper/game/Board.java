package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class Board {

    public static boolean notTest = true;
    public final Game map;
    final List<Player> viewers = new LinkedList<>();
    private final int width;
    private final int height;
    private final int bombCount;
    private final Location corner;
    private final Field[][] board;
    private final Point2D[] bombList;
    private final Player player;
    private long started;
    private boolean isGenerated;
    private boolean isFinished;

    public Board(Game map, int width, int height, int bombCount, Location corner, Player player) {
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
            new ActionBarScheduler(this).runTaskTimer(Minesweeper.getPlugin(), 0, 1);
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

    public Field[][] getBoard() {
        return this.board;
    }

    public Player getPlayer() {
        return player;
    }

    public void drawBlancField() {
        this.drawBlancField(viewers);
    }

    public void drawBlancField(List<Player> players) {
        getCurrentPlayerPainters(players).forEach((painter, players2) -> {
            if(painter != null)
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
            if(painter != null)
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

        if (!this.isGenerated)
            generateBoard(x, y);

        SurfaceDiscoverer.uncoverFields(this, x, y);
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
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("mm:ss:SSS");

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
        viewers.forEach(player1 -> {
            Class<? extends Painter> aClass = Painter.loadPainterClass(player1.getPersistentDataContainer());

            List<Player> orDefault = map1.getOrDefault(aClass, new ArrayList<>());
            orDefault.add(player1);
            map1.put(aClass, orDefault);
        });

        Class<? extends Painter> aClass = Painter.loadPainterClass(player.getPersistentDataContainer());
        List<Player> orDefault = map1.getOrDefault(aClass, new ArrayList<>());
        orDefault.add(player);
        map1.put(aClass, orDefault);

        Map<Painter, List<Player>> map2 = new HashMap<>();
        map1.forEach((aClass2, players) -> map2.put(Game.PAINTER_MAP.get(aClass2), players));
        return map2;
    }

    public Map<Painter, List<Player>> getCurrentPlayerPainters() {
        return getCurrentPlayerPainters(this.viewers);
    }

    public void win() {
        this.finish();

        this.player.sendMessage(Minesweeper.getLanguage().getString("message_win"));
        this.player.sendMessage(Minesweeper.getLanguage().getString("field_desc", String.valueOf(this.width), String.valueOf(this.height), String.valueOf(this.bombCount)));
        this.player.sendMessage(Minesweeper.getLanguage().getString("message_time_needed", getActualTimeNeededString()));

        this.player.sendTitle(ChatColor.DARK_GREEN + Minesweeper.getLanguage().getString("title_win"), ChatColor.GREEN + Minesweeper.getLanguage().getString("message_time_needed", getActualTimeNeededString()), 10, 70, 20);
        PacketUtil.sendSoundEffect(this.player, Sound.UI_TOAST_CHALLENGE_COMPLETE, .5f, this.player.getLocation());
        PacketUtil.sendActionBar(this.player, getActualTimeNeededString());
    }

    public void checkIfWon() {
        if (Arrays.stream(this.board).flatMap(Arrays::stream).anyMatch(field -> field.isCovered() && !field.isBomb())) {
            return;
        }
        win();
    }

    public void breakGame() {
        finish();
    }

    public void lose() {
        finish();

        getCurrentPlayerPainters().forEach((painter, players) -> {
            if(painter != null)
                painter.drawBombs(this, players);
        });

        Bukkit.getScheduler().runTaskLater(Minesweeper.getPlugin(), () -> {

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
            throw new RuntimeException(Minesweeper.getLanguage().getString("error_already_generated"));

        this.started = System.currentTimeMillis();
        this.isGenerated = true;

        Random random = new Random();
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
                        try{
                            ints[(int) (point2D.getX() + j)][(int) (point2D.getY() + k)]++;
                        }catch(ArrayIndexOutOfBoundsException ignore){
                        }
                    }
                }
            }
        }

        for (int i = 0; i < cache.length; i++)
            for (int j = 0; j < cache[i].length; j++) {
                this.board[i][j] = new Field(i, j, cache[i][j], ints[i][j]);
            }
    }

    private boolean couldBombSpawn(int x, int y, int possibleX, int possibleY) {
        for (int i = -1; i < 2; i++)
            for (int j = -1; j < 2; j++)
                if (x + i == possibleX && y + j == possibleY)
                    return true;

        return false;
    }
    public static class Field {

        private final boolean isBomb;
        private final int bombCount;
        private final int x;
        private final int y;
        private boolean isCovered;
        private boolean isMarked;

        public Field(int x, int y, boolean isBomb, int bombCount) {
            this.x = x;
            this.y = y;
            this.isCovered = true;
            this.isMarked = false;
            this.isBomb = isBomb;
            this.bombCount = bombCount;
        }

        public int getNeighborCount() {
            return bombCount;
        }

        public void setUncover() {
            this.isMarked = false;
            this.isCovered = false;
        }

        public boolean isBomb() {
            return isBomb;
        }

        public boolean isMarked() {
            return isMarked;
        }

        public void reverseMark() {
            this.isMarked = !this.isMarked();
        }

        public Material getActualMaterial(Painter painter) {
            return painter.getActualMaterial(this);
        }

        public Material getMark() {
            return isMarked ? Material.REDSTONE_TORCH : Material.AIR;
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
