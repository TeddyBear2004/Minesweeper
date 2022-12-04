package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.util.PacketUtil;
import de.teddy.minesweeper.util.Tuple2;
import de.teddy.minesweeper.util.Tuple3;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPainter implements Painter {

    private Board board;

    public BlockPainter() {

    }

    @Override
    public void applyBoard(Board board) {
        this.board = board;
    }

    @Override
    public void drawBlancField(List<Player> players) {
        if (board == null || !board.notTest)
            return;
        Map<Tuple3<Integer, Integer, Integer>, Tuple2<List<Short>, List<WrappedBlockData>>> subChunkMap = new HashMap<>();

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Location location = board.getCorner().clone();
                location.setX(board.getCorner().getBlockX() + i);
                location.setZ(board.getCorner().getBlockZ() + j);
                int chunkHeight = (location.getBlockY() - location.getBlockY() % 16) / 16;
                int chunkHeightPlusOne = ((location.getBlockY() + 1) - (location.getBlockY() + 1) % 16) / 16;

                Tuple3<Integer, Integer, Integer> subChunkTuple = new Tuple3<>(location.getChunk().getX(), chunkHeight, location.getChunk().getZ());
                Tuple3<Integer, Integer, Integer> subChunkTuplePlusOne = new Tuple3<>(location.getChunk().getX(), chunkHeightPlusOne, location.getChunk().getZ());

                if (!subChunkMap.containsKey(subChunkTuple))
                    subChunkMap.put(subChunkTuple, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                if (!subChunkMap.containsKey(subChunkTuplePlusOne))
                    subChunkMap.put(subChunkTuplePlusOne, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                boolean b = Board.isLightField(i, j);
                Material m;
                m = (b ? Board.LIGHT_DEFAULT : Board.DARK_DEFAULT);

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2 = subChunkMap.get(subChunkTuple);
                listListTuple2.getA().add(Board.convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.getB().add(WrappedBlockData.createData(m));

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);
                listListTuple2PlusOne.getA().add(Board.convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));
                listListTuple2PlusOne.getB().add(WrappedBlockData.createData(Material.AIR));
            }
        }

        subChunkMap.forEach((xyz, listListTuple2) -> {
            for (Player p : players)
                PacketUtil.sendMultiBlockChange(
                        p,
                        ArrayUtils.toPrimitive(listListTuple2.getA().toArray(new Short[0])),
                        new BlockPosition(xyz.getA(), xyz.getB(), xyz.getC()),
                        listListTuple2.getB().toArray(new WrappedBlockData[0]),
                        true);
        });
    }

    @Override
    public void drawField(List<Player> players) {
        if (this.board == null) return;
        Map<Tuple3<Integer, Integer, Integer>, Tuple2<List<Short>, List<WrappedBlockData>>> subChunkMap = new HashMap<>();

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                Location location = board.getCorner().clone();
                location.setX(board.getCorner().getBlockX() + i);
                location.setZ(board.getCorner().getBlockZ() + j);

                int chunkHeight = (location.getBlockY() - location.getBlockY() % 16) / 16;
                int chunkHeightPlusOne = ((location.getBlockY() + 1) - (location.getBlockY() + 1) % 16) / 16;
                Tuple3<Integer, Integer, Integer> subChunkTuple = new Tuple3<>(location.getChunk().getX(), chunkHeight, location.getChunk().getZ());
                Tuple3<Integer, Integer, Integer> subChunkTuplePlusOne = new Tuple3<>(location.getChunk().getX(), chunkHeightPlusOne, location.getChunk().getZ());

                if (!subChunkMap.containsKey(subChunkTuple))
                    subChunkMap.put(subChunkTuple, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));
                if (!subChunkMap.containsKey(subChunkTuplePlusOne))
                    subChunkMap.put(subChunkTuplePlusOne, new Tuple2<>(new ArrayList<>(), new ArrayList<>()));

                Board.Field field = board.getBoard()[i][j];

                boolean b = Board.isLightField(i, j);
                Material m;

                if (field == null || field.isCovered()) {
                    m = (b ? Board.LIGHT_DEFAULT : Board.DARK_DEFAULT);
                } else {
                    if (field.isBomb()) {
                        m = Material.COAL_BLOCK;
                    } else
                        m = (b ? Board.LIGHT_MATERIALS : Board.DARK_MATERIALS)[field.getNeighborCount()];
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

        subChunkMap.forEach((xyz, listListTuple2) -> {
            for (Player p : players)
                PacketUtil.sendMultiBlockChange(
                        p,
                        ArrayUtils.toPrimitive(listListTuple2.getA().toArray(new Short[0])),
                        new BlockPosition(xyz.getA(), xyz.getB(), xyz.getC()),
                        listListTuple2.getB().toArray(new WrappedBlockData[0]),
                        true);
        });
    }

}
