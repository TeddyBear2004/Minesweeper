package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddy.minesweeper.events.packets.LeftClickEvent;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.util.PacketUtil;
import de.teddy.minesweeper.util.Tuple2;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    private final Plugin plugin;
    private BukkitTask bombTask;

    public BlockPainter(Plugin plugin) {
        this.plugin = plugin;
    }

    private static void sendMultiBlockChange(List<Player> players, Map<BlockPosition, Tuple2<List<Short>, List<WrappedBlockData>>> subChunkMap) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        subChunkMap.forEach((blockPosition, listListTuple) -> {
            PacketContainer multiBlockChange = PacketUtil.getMultiBlockChange(
                    ArrayUtils.toPrimitive(listListTuple.a().toArray(new Short[0])),
                    blockPosition,
                    listListTuple.b().toArray(new WrappedBlockData[0]),
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
    public String getName() {
        return "classic";
    }

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
                listListTuple2.a().add(Board.convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.b().add(WrappedBlockData.createData(m));

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);
                listListTuple2PlusOne.a().add(Board.convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));
                listListTuple2PlusOne.b().add(WrappedBlockData.createData(Material.AIR));
            }
        }

        sendMultiBlockChange(players, subChunkMap);
        if (bombTask != null)
            this.bombTask.cancel();
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

                listListTuple2.a().add(Board.convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.b().add(WrappedBlockData.createData(m));

                Tuple2<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);

                listListTuple2PlusOne.a().add(Board.convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));

                if (field != null && field.isMarked()) {
                    listListTuple2PlusOne.b().add(WrappedBlockData.createData(field.getMark()));
                } else
                    listListTuple2PlusOne.b().add(WrappedBlockData.createData(Material.AIR));
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

            if (bombTask != null)
                bombTask.cancel();

            bombTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
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

        if (field.getBoard().isFinished() && field.isBomb() && (!field.isCovered() || !field.getBoard().isWin()))
            return Material.COAL_BLOCK;

        if (field.isCovered())
            return lightField ? LIGHT_DEFAULT : DARK_DEFAULT;

        return (lightField ? LIGHT_MATERIALS : DARK_MATERIALS)[field.getNeighborCount()];
    }

    @Override
    public List<PacketType> getRightClickPacketType() {
        return Collections.singletonList(PacketType.Play.Client.USE_ITEM);
    }

    @Override
    public List<PacketType> getLeftClickPacketType() {
        return Collections.singletonList(PacketType.Play.Client.BLOCK_DIG);
    }

    @Override
    public void onRightClick(Player player, PacketEvent event, Game game, PacketContainer packet) {
        BlockPosition blockPosition = packet.getMovingBlockPositions().read(0).getBlockPosition();
        Location location = blockPosition.toLocation(player.getWorld());

        if (game.isBlockOutsideGame(location.getBlock()))
            return;

        Board board = Game.getBoard(player);

        if (board == null) {
            Board watching = Game.getBoardWatched(player);

            if (watching != null) {
                Board.Field field = watching.getField(location);
                if (field != null) {
                    Material[] materials = new Material[]{getActualMaterial(field), field.getMark()};
                    PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[location.getBlockY() - game.getFieldHeight()]));
                }
                player.getInventory().setContents(Inventories.VIEWER_INVENTORY);
                event.setCancelled(true);
            }
            return;
        }

        Board.Field field = board.getField(location);

        if (field == null)
            return;

        event.setCancelled(true);
        player.getInventory().setContents(Inventories.GAME_INVENTORY);

        if (board.isFinished() || packet.getHands().read(0) == EnumWrappers.Hand.OFF_HAND)
            return;

        if (field.isCovered())
            field.reverseMark();

        board.draw();
    }

    @Override
    public void onLeftClick(Player player, PacketEvent event, Game game, PacketContainer packet) {
        BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
        Location location = blockPosition.toLocation(player.getWorld());

        if (game.isBlockOutsideGame(location.getBlock()))
            return;

        Board board = Game.getBoard(player);
        if (board == null)
            board = Game.getBoardWatched(player);

        if (board == null) {
            Board watching = Game.getBoardWatched(player);

            if (watching != null) {
                Board.Field field = watching.getField(location);
                if (field == null)
                    return;
                Material[] materials = new Material[]{getActualMaterial(field), field.getMark()};

                if (game.getFieldHeight() - location.getBlockY() < 0)
                    return;

                PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[game.getFieldHeight() - location.getBlockY()]));
            }
            return;
        }

        Board.Field field = board.getField(location);

        EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().read(0);
        if (field != null && digType == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
            if (location.getBlockY() - game.getFieldHeight() == 0) {
                board.draw();
                event.setCancelled(true);
            } else if (location.getBlockY() - game.getFieldHeight() == 1) {
                PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(field.getMark()));
            }
        }

        if (digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK && field != null) {
            board.draw();
            return;
        }

        if (digType != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK)
            return;


        if (board.isFinished())
            return;

        if (board.getPlayer().equals(player)) {
            try{
                if (field == null) {
                    try{
                        board.checkField(location.getBlockX(), location.getBlockZ());
                    }catch(IllegalArgumentException ignore){
                    }

                    board.draw();
                    return;
                }

                if (field.isMarked()) {
                    if (location.getBlockY() - game.getFieldHeight() == 1)
                        PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(field.getMark()));

                    return;
                }

                PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

                if (field.isCovered()) {
                    board.checkField(location.getBlockX(), location.getBlockZ());
                } else if (System.currentTimeMillis() - LeftClickEvent.LAST_CLICKED.getOrDefault(player, (long) -1000) <= personalModifier.getDoubleClickDuration().orElse(350)) {
                    try{
                        board.checkNumber(location.getBlockX(), location.getBlockZ());
                    }catch(ArrayIndexOutOfBoundsException ignore){
                    }
                }

                if (personalModifier.isEnableDoubleClick().orElse(true))
                    LeftClickEvent.LAST_CLICKED.put(player, System.currentTimeMillis());
            }catch(BombExplodeException e){
                board.lose();
            }
        }

        board.draw();
        board.checkIfWon();

    }

}
