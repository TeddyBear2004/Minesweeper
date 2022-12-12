package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.util.PacketUtil;
import de.teddy.minesweeper.util.Tuple2;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPainter implements Painter {

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
    public static final Material LIGHT_DEFAULT = Material.LIME_CONCRETE_POWDER;
    public static final Material DARK_DEFAULT = Material.GREEN_CONCRETE_POWDER;

    @Override
    public void drawBlancField(Board board, List<Player> players) {
        if (board == null || !Board.notTest)
            return;
        Map<BlockPosition, Tuple2<List<Short>, List<WrappedBlockData>>> subChunkMap = new HashMap<>();

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Location location = board.getCorner().clone();
                location.setX(board.getCorner().getBlockX() + i);
                location.setZ(board.getCorner().getBlockZ() + j);
                int chunkHeight = (location.getBlockY() - location.getBlockY() % 16) / 16;
                int chunkHeightPlusOne = ((location.getBlockY() + 1) - (location.getBlockY() + 1) % 16) / 16;

                BlockPosition subChunkTuple = new BlockPosition(location.getChunk().getX(), chunkHeight, location.getChunk().getZ());
                BlockPosition subChunkTuplePlusOne = new BlockPosition(location.getChunk().getX(), chunkHeightPlusOne, location.getChunk().getZ());

                if (!subChunkMap.containsKey(subChunkTuple))
                    subChunkMap.put(subChunkTuple, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                if (!subChunkMap.containsKey(subChunkTuplePlusOne))
                    subChunkMap.put(subChunkTuplePlusOne, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                boolean b = Board.isLightField(i, j);
                Material m;
                m = (b ? LIGHT_DEFAULT : DARK_DEFAULT);

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2 = subChunkMap.get(subChunkTuple);
                listListTuple2.getA().add(Board.convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.getB().add(WrappedBlockData.createData(m));

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);
                listListTuple2PlusOne.getA().add(Board.convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));
                listListTuple2PlusOne.getB().add(WrappedBlockData.createData(Material.AIR));
            }
        }

        sendMultiBlockChange(players, subChunkMap);
    }

    private static void sendMultiBlockChange(List<Player> players, Map<BlockPosition, Tuple2<List<Short>, List<WrappedBlockData>>> subChunkMap) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        subChunkMap.forEach((blockPosition, listListTuple) -> {
            PacketContainer multiBlockChange = PacketUtil.getMultiBlockChange(
                    ArrayUtils.toPrimitive(listListTuple.getA().toArray(new Short[0])),
                    blockPosition,
                    listListTuple.getB().toArray(new WrappedBlockData[0]),
                    true);
            for (Player p : players) {
                try{
                    protocolManager.sendServerPacket(p, multiBlockChange);
                }catch(InvocationTargetException e){
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void drawField(Board board, List<Player> players) {
        if (board == null) return;
        Map<BlockPosition, Tuple2<List<Short>, List<WrappedBlockData>>> subChunkMap = new HashMap<>();

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Location location = board.getCorner().clone();
                location.setX(board.getCorner().getBlockX() + i);
                location.setZ(board.getCorner().getBlockZ() + j);

                int chunkHeight = (location.getBlockY() - location.getBlockY() % 16) / 16;
                int chunkHeightPlusOne = ((location.getBlockY() + 1) - (location.getBlockY() + 1) % 16) / 16;
                BlockPosition subChunkTuple = new BlockPosition(location.getChunk().getX(), chunkHeight, location.getChunk().getZ());
                BlockPosition subChunkTuplePlusOne = new BlockPosition(location.getChunk().getX(), chunkHeightPlusOne, location.getChunk().getZ());

                if (!subChunkMap.containsKey(subChunkTuple))
                    subChunkMap.put(subChunkTuple, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                if (!subChunkMap.containsKey(subChunkTuplePlusOne))
                    subChunkMap.put(subChunkTuplePlusOne, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));

                Board.Field field = board.getBoard()[i][j];

                boolean b = Board.isLightField(i, j);
                Material m;

                if (field == null) {
                    m = (b ? LIGHT_DEFAULT : DARK_DEFAULT);
                } else {
                    m = getActualMaterial(field);
                }

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2 = subChunkMap.get(subChunkTuple);

                listListTuple2.getA().add(Board.convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.getB().add(WrappedBlockData.createData(m));

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);

                listListTuple2PlusOne.getA().add(Board.convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));

                if (field != null && field.isMarked()) {
                    listListTuple2PlusOne.getB().add(WrappedBlockData.createData(Material.REDSTONE_TORCH));
                } else
                    listListTuple2PlusOne.getB().add(WrappedBlockData.createData(Material.AIR));
            }
        }

        sendMultiBlockChange(players, subChunkMap);
    }

    @Override
    public void drawBombs(Board board, List<Player> players) {
        double explodeDuration = 0.5d;

        for (Point2D point2D : board.getBombList()) {
            Location clone = board.getCorner().clone();

            clone.setX(board.getCorner().getBlockX() + point2D.getX());
            clone.setZ(board.getCorner().getBlockZ() + point2D.getY());


            Bukkit.getScheduler().runTaskLater(Minesweeper.getPlugin(), () -> {
                for (Player p : players) {
                    PacketUtil.sendBlockChange(p, new BlockPosition(clone.toVector()), WrappedBlockData.createData(Material.COAL_BLOCK));
                    PacketUtil.sendSoundEffect(p, Sound.BLOCK_STONE_PLACE, 1f, clone);
                }
            }, (long) (20 * explodeDuration));

            explodeDuration *= 0.7;
        }
    }

    @Override
    public ItemStack getActualItemStack(Board.Field field) {
        return new ItemStack(getActualMaterial(field));
    }

    @Override
    public Material getActualMaterial(Board.Field field) {
        boolean lightField = Board.isLightField(field.getX(), field.getY());
        if (field.isCovered())
            return lightField ? LIGHT_DEFAULT : DARK_DEFAULT;

        if (field.isBomb())
            return Material.COAL_BLOCK;
        else
            return (lightField ? LIGHT_MATERIALS : DARK_MATERIALS)[field.getNeighborCount()];
    }

}
