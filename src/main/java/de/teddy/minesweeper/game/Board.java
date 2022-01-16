package de.teddy.minesweeper.game;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.util.PacketUtil;
import de.teddy.minesweeper.util.Tuple2;
import de.teddy.minesweeper.util.Tuple3;
import org.apache.commons.lang.ArrayUtils;
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

    public static final Material[] LIGHT_MATERIALS = {
            Material.WHITE_CONCRETE_POWDER,
            Material.LIME_TERRACOTTA,
            Material.GREEN_CONCRETE,
            Material.YELLOW_TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.MAGENTA_TERRACOTTA,
            Material.PINK_TERRACOTTA,
            Material.PURPLE_TERRACOTTA,
            Material.RED_TERRACOTTA};

    public static final Material[] DARK_MATERIALS = {
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.TERRACOTTA,
            Material.GREEN_TERRACOTTA,
            Material.BROWN_TERRACOTTA,
            Material.BLUE_TERRACOTTA,
            Material.CYAN_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA,
            Material.GRAY_TERRACOTTA,
            Material.LIGHT_BLUE_TERRACOTTA};

    private static final Material LIGHT_DEFAULT = Material.LIME_CONCRETE_POWDER;
    private static final Material DARK_DEFAULT = Material.GREEN_CONCRETE_POWDER;
    private final int width;
    private final int height;
    private final int bombCount;
    private final Location corner;
    private final Field[][] board;
    private final Point2D[] bombList;
    private final Player player;
    final List<Player> viewers = new LinkedList<>();
    private long started;
    private boolean isGenerated;
    private boolean isFinished;
    public final Game map;

    public Board(Game map, int width, int height, int bombCount, Location corner, Player player){
        this.map = map;
        this.player = player;
        if(width * height - 9 <= bombCount || width * height <= bombCount)
            throw new IllegalArgumentException("bombCount cannot be bigger than width * height");

        this.isFinished = this.isGenerated = false;
        this.bombList = new Point2D[bombCount];
        this.corner = corner;
        this.width = width;
        this.height = height;
        this.bombCount = bombCount;

        this.board = new Field[width][height];
        if(notTest){
            new ActionBarScheduler(this).runTaskTimer(Minesweeper.INSTANCE, 0, 1);
            draw();
        }
    }

    private static short convertToLocal(int x, int y, int z){
        x = x & 0xF;
        y = y & 0xF;
        z = z & 0xF;
        return (short)(x << 8 | z << 4 | y);
    }

    public static boolean isLightField(int x, int y){
        return Math.abs(x + y) % 2 == 0;
    }

    public void finish(){
        isFinished = true;
    }

    public boolean isFinished(){
        return isFinished;
    }

    public Field[][] getBoard(){
        return board;
    }

    public void drawBlancField(){
        drawBlancField(viewers);
    }

    public void drawBlancField(List<Player> players){
        if(!notTest)
            return;
        Map<Tuple3<Integer, Integer, Integer>, Tuple2<List<Short>, List<WrappedBlockData>>> subChunkMap = new HashMap<>();

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                Location location = corner.clone();
                location.setX(corner.getBlockX() + i);
                location.setZ(corner.getBlockZ() + j);
                int chunkHeight = (location.getBlockY() - location.getBlockY() % 16) / 16;
                int chunkHeightPlusOne = ((location.getBlockY() + 1) - (location.getBlockY() + 1) % 16) / 16;

                Tuple3<Integer, Integer, Integer> subChunkTuple = new Tuple3<>(location.getChunk().getX(), chunkHeight, location.getChunk().getZ());
                Tuple3<Integer, Integer, Integer> subChunkTuplePlusOne = new Tuple3<>(location.getChunk().getX(), chunkHeightPlusOne, location.getChunk().getZ());

                if(!subChunkMap.containsKey(subChunkTuple))
                    subChunkMap.put(subChunkTuple, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                if(!subChunkMap.containsKey(subChunkTuplePlusOne))
                    subChunkMap.put(subChunkTuplePlusOne, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                boolean b = isLightField(i, j);
                Material m;
                m = (b ? LIGHT_DEFAULT : DARK_DEFAULT);

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2 = subChunkMap.get(subChunkTuple);
                listListTuple2.getA().add(convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.getB().add(WrappedBlockData.createData(m));

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);
                listListTuple2PlusOne.getA().add(convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));
                listListTuple2PlusOne.getB().add(WrappedBlockData.createData(Material.AIR));
            }
        }

        subChunkMap.forEach((xyz, listListTuple2) -> {
            for(Player p : players)
                PacketUtil.sendMultiBlockChange(
                        p,
                        ArrayUtils.toPrimitive(listListTuple2.getA().toArray(new Short[0])),
                        new BlockPosition(xyz.getA(), xyz.getB(), xyz.getC()),
                        listListTuple2.getB().toArray(new WrappedBlockData[0]),
                        true);
        });
    }

    public boolean isGenerated(){
        return isGenerated;
    }

    public void draw(){
        draw(viewers);
    }

    public void draw(List<Player> players){
        Map<Tuple3<Integer, Integer, Integer>, Tuple2<List<Short>, List<WrappedBlockData>>> subChunkMap = new HashMap<>();

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                Location location = corner.clone();
                location.setX(corner.getBlockX() + i);
                location.setZ(corner.getBlockZ() + j);

                int chunkHeight = (location.getBlockY() - location.getBlockY() % 16) / 16;
                int chunkHeightPlusOne = ((location.getBlockY() + 1) - (location.getBlockY() + 1) % 16) / 16;
                Tuple3<Integer, Integer, Integer> subChunkTuple = new Tuple3<>(location.getChunk().getX(), chunkHeight, location.getChunk().getZ());
                Tuple3<Integer, Integer, Integer> subChunkTuplePlusOne = new Tuple3<>(location.getChunk().getX(), chunkHeightPlusOne, location.getChunk().getZ());

                if(!subChunkMap.containsKey(subChunkTuple))
                    subChunkMap.put(subChunkTuple, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                if(!subChunkMap.containsKey(subChunkTuplePlusOne))
                    subChunkMap.put(subChunkTuplePlusOne, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));

                Field field = getBoard()[i][j];

                boolean b = isLightField(i, j);
                Material m;

                if(field == null || field.isCovered()){
                    m = (b ? LIGHT_DEFAULT : DARK_DEFAULT);
                }else{
                    if(field.isBomb){
                        m = Material.COAL_BLOCK;
                    }else
                        m = (b ? LIGHT_MATERIALS : DARK_MATERIALS)[field.getNeighborCount()];
                }

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2 = subChunkMap.get(subChunkTuple);

                listListTuple2.getA().add(convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.getB().add(WrappedBlockData.createData(m));

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);

                listListTuple2PlusOne.getA().add(convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));

                if(field != null && field.isMarked){
                    listListTuple2PlusOne.getB().add(WrappedBlockData.createData(Material.REDSTONE_TORCH));
                }else
                    listListTuple2PlusOne.getB().add(WrappedBlockData.createData(Material.AIR));
            }
        }

        subChunkMap.forEach((xyz, listListTuple2) -> {
            for(Player p : players)
                PacketUtil.sendMultiBlockChange(
                        p,
                        ArrayUtils.toPrimitive(listListTuple2.getA().toArray(new Short[0])),
                        new BlockPosition(xyz.getA(), xyz.getB(), xyz.getC()),
                        listListTuple2.getB().toArray(new WrappedBlockData[0]),
                        true);
        });
    }


    public Field getField(int x, int y){
        try{
            return board[x][y];
        }catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public Field getField(Location location){
        int x = Math.abs(corner.getBlockX() - location.getBlockX());
        int y = Math.abs(corner.getBlockZ() - location.getBlockZ());

        return getField(x, y);
    }

    public void checkField(int x, int y) throws BombExplodeException{
        x = Math.abs(corner.getBlockX() - x);
        y = Math.abs(corner.getBlockZ() - y);

        if(!isGenerated)
            generateBoard(x, y);

        SurfaceDiscoverer.uncoverFields(this, x, y);
    }

    public void checkNumber(int x, int y) throws BombExplodeException{
        x = Math.abs(corner.getBlockX() - x);
        y = Math.abs(corner.getBlockZ() - y);

        SurfaceDiscoverer.uncoverFieldsNextToNumber(this, x, y);
    }

    private long getActualTimeNeeded(){
        return started == 0 ? 0 : System.currentTimeMillis() - started;
    }

    private String getActualTimeNeededString(){
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("mm:ss:SSS");

        return dateTimeFormatter.format(new Date(getActualTimeNeeded()));
    }

    public void win(){
        finish();

        player.sendMessage(Minesweeper.language.getString("message_win"));
        player.sendMessage(Minesweeper.language.getString("field_desc", String.valueOf(width), String.valueOf(height), String.valueOf(bombCount)));
        player.sendMessage(Minesweeper.language.getString("message_time_needed", getActualTimeNeededString()));

        player.sendTitle(ChatColor.DARK_GREEN + Minesweeper.language.getString("title_win"), ChatColor.GREEN + Minesweeper.language.getString("message_time_needed", getActualTimeNeededString()), 10, 70, 20);
        PacketUtil.sendSoundEffect(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, .5f, player.getLocation());
        PacketUtil.sendActionBar(player, getActualTimeNeededString());
    }

    public void checkIfWon(){
        if(Arrays.stream(this.board).flatMap(Arrays::stream).anyMatch(field -> field.isCovered() && !field.isBomb())){
            return;
        }
        win();
    }

    public void breakGame(){
        finish();
    }

    public void lose(){
        finish();
        double explodeDuration = 0.5d;

        for(Point2D point2D : this.bombList){
            Location clone = this.corner.clone();

            clone.setX(this.corner.getBlockX() + point2D.getX());
            clone.setZ(this.corner.getBlockZ() + point2D.getY());


            Bukkit.getScheduler().runTaskLater(Minesweeper.INSTANCE, () -> {
                for(Player p : viewers){
                    PacketUtil.sendBlockChange(p, new BlockPosition(clone.toVector()), WrappedBlockData.createData(Material.COAL_BLOCK));
                    PacketUtil.sendSoundEffect(p, Sound.BLOCK_STONE_PLACE, 1f, clone);
                }
            }, (long)(20 * explodeDuration));

            explodeDuration *= 0.7;
        }
        Bukkit.getScheduler().runTaskLater(Minesweeper.INSTANCE, () -> {

            for(Player p : viewers){
                PacketUtil.sendSoundEffect(p, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, p.getLocation());
                PacketUtil.sendParticleEffect(p,
                        corner.clone().add(((double)width) / 2, 1, (double)height / 2),
                        Particle.EXPLOSION_LARGE,
                        (float)width / 5,
                        (float)height / 5,
                        width * height);
            }
        }, 20);
    }

    private void generateBoard(int x, int y){
        if(isGenerated)
            throw new RuntimeException(Minesweeper.language.getString("error_already_generated"));

        started = System.currentTimeMillis();
        isGenerated = true;

        Random random = new Random();
        boolean[][] cache = new boolean[this.height][this.width];
        int[][] ints = new int[this.height][this.width];

        for(int i = 0; i < bombCount; i++){
            int randWidth, randHeight;

            do{
                randWidth = random.nextInt(this.width);
                randHeight = random.nextInt(this.height);
            }while(cache[randWidth][randHeight] || couldBombSpawn(x, y, randWidth, randHeight));

            cache[randWidth][randHeight] = true;
            this.bombList[i] = new Point(randWidth, randHeight);
        }

        for(Point2D point2D : this.bombList){
            for(int j = -1; j < 2; j++){
                for(int k = -1; k < 2; k++){
                    if(!(j == 0 && k == 0)){
                        try{
                            ints[(int)(point2D.getX() + j)][(int)(point2D.getY() + k)]++;
                        }catch(ArrayIndexOutOfBoundsException ignore){
                        }
                    }
                }
            }
        }

        for(int i = 0; i < cache.length; i++)
            for(int j = 0; j < cache[i].length; j++){
                this.board[i][j] = new Field(i, j, cache[i][j], ints[i][j]);
            }
    }

    private boolean couldBombSpawn(int x, int y, int possibleX, int possibleY){
        for(int i = -1; i < 2; i++)
            for(int j = -1; j < 2; j++)
                if(x + i == possibleX && y + j == possibleY)
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

        public Field(int x, int y, boolean isBomb, int bombCount){
            this.x = x;
            this.y = y;
            this.isCovered = true;
            this.isMarked = false;
            this.isBomb = isBomb;
            this.bombCount = bombCount;
        }

        public int getNeighborCount(){
            return bombCount;
        }

        public void setUncover(){
            this.isMarked = false;
            this.isCovered = false;
        }

        public boolean isBomb(){
            return isBomb;
        }

        public boolean isMarked(){
            return isMarked;
        }

        public void reverseMark(){
            this.isMarked = !this.isMarked();
        }

        public Material getActualMaterial(){
            boolean lightField = isLightField(x, y);
            if(isCovered)
                return lightField ? LIGHT_DEFAULT : DARK_DEFAULT;

            if(isBomb)
                return Material.COAL_BLOCK;
            else
                return (lightField ? LIGHT_MATERIALS : DARK_MATERIALS)[getNeighborCount()];
        }

        public Material getMark(){
            return isMarked ? Material.REDSTONE_TORCH : Material.AIR;
        }

        public boolean isCovered(){
            return isCovered;
        }
    }

    private static class ActionBarScheduler extends BukkitRunnable {
        private final Board board;

        private ActionBarScheduler(Board board){
            this.board = board;
        }

        @Override
        public void run(){
            if(board.isFinished){
                cancel();
                return;
            }
            PacketUtil.sendActionBar(board.player, board.getActualTimeNeededString());
        }
    }
}
