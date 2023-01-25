package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.Field;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.click.ClickHandler;
import de.teddybear2004.minesweeper.game.inventory.Inventories;
import de.teddybear2004.minesweeper.util.PacketUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final ClickHandler clickHandler;
    private final GameManager gameManager;
    private BukkitTask bombTask;

    public BlockPainter(Plugin plugin, ClickHandler clickHandler, GameManager gameManager) {
        this.plugin = plugin;
        this.clickHandler = clickHandler;
        this.gameManager = gameManager;
    }

    @Override
    public @NotNull String getName() {
        return "classic";
    }

    @Override
    public void drawBlancField(@Nullable Board board, @NotNull List<Player> players) {
        if (board == null)
            return;
        Map<BlockPosition, Pair<List<Short>, List<WrappedBlockData>>> subChunkMap = new HashMap<>();

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
                    subChunkMap.put(subChunkTuple, Pair.of(new ArrayList<>(), new ArrayList<>()));
                if (!subChunkMap.containsKey(subChunkTuplePlusOne))
                    subChunkMap.put(subChunkTuplePlusOne, Pair.of(new ArrayList<>(), new ArrayList<>()));
                boolean b = Board.isLightField(i, j);
                Material m;
                m = (b ? LIGHT_DEFAULT : DARK_DEFAULT);

                Pair<List<Short>, List<WrappedBlockData>> listListTuple2 = subChunkMap.get(subChunkTuple);
                listListTuple2.getLeft().add(Board.convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.getRight().add(WrappedBlockData.createData(m));

                Pair<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);
                listListTuple2PlusOne.getLeft().add(Board.convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));
                listListTuple2PlusOne.getRight().add(WrappedBlockData.createData(Material.AIR));
            }
        }

        sendMultiBlockChange(players, subChunkMap);
        if (bombTask != null)
            this.bombTask.cancel();
    }

    private static void sendMultiBlockChange(@NotNull List<Player> players, @NotNull Map<BlockPosition, Pair<List<Short>, List<WrappedBlockData>>> subChunkMap) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        subChunkMap.forEach((blockPosition, listListTuple) -> {
            PacketContainer multiBlockChange = PacketUtil.getMultiBlockChange(
                    ArrayUtils.toPrimitive(listListTuple.getLeft().toArray(new Short[0])),
                    blockPosition,
                    listListTuple.getRight().toArray(new WrappedBlockData[0]),
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
    public void drawField(@Nullable Board board, @NotNull List<Player> players) {
        if (board == null) return;
        Map<BlockPosition, Pair<List<Short>, List<WrappedBlockData>>> subChunkMap = new HashMap<>();

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
                    subChunkMap.put(subChunkTuple, Pair.of(new ArrayList<>(), new ArrayList<>()));
                if (!subChunkMap.containsKey(subChunkTuplePlusOne))
                    subChunkMap.put(subChunkTuplePlusOne, Pair.of(new ArrayList<>(), new ArrayList<>()));

                Field field = board.getBoard()[i][j];

                boolean b = Board.isLightField(i, j);
                Material m;

                if (field == null) {
                    m = (b ? LIGHT_DEFAULT : DARK_DEFAULT);
                } else {
                    m = getActualMaterial(field);
                }

                Pair<List<Short>, List<WrappedBlockData>> listListTuple2 = subChunkMap.get(subChunkTuple);

                listListTuple2.getLeft().add(Board.convertToLocal(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                listListTuple2.getRight().add(WrappedBlockData.createData(m));

                Pair<List<Short>, List<WrappedBlockData>> listListTuple2PlusOne = subChunkMap.get(subChunkTuplePlusOne);

                listListTuple2PlusOne.getLeft().add(Board.convertToLocal(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ()));

                if (field != null && field.isMarked()) {
                    listListTuple2PlusOne.getRight().add(WrappedBlockData.createData(field.getMark()));
                } else
                    listListTuple2PlusOne.getRight().add(WrappedBlockData.createData(Material.AIR));
            }
        }

        sendMultiBlockChange(players, subChunkMap);
    }

    @Override
    public void drawBombs(@NotNull Board board, @NotNull List<Player> players) {
        double explodeDuration = 0.5d;

        for (int[] point2D : board.getBombList()) {
            Location clone = board.getCorner().clone();

            clone.setX(board.getCorner().getBlockX() + point2D[0]);
            clone.setZ(board.getCorner().getBlockZ() + point2D[1]);

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
    public @NotNull List<PacketType> getRightClickPacketType() {
        return Collections.singletonList(PacketType.Play.Client.USE_ITEM);
    }

    @Override
    public @NotNull List<PacketType> getLeftClickPacketType() {
        return Collections.singletonList(PacketType.Play.Client.BLOCK_DIG);
    }

    @Override
    public void onRightClick(@NotNull Player player, @NotNull PacketEvent event, @NotNull Game game, @NotNull PacketContainer packet) {
        BlockPosition blockPosition = packet.getMovingBlockPositions().read(0).getBlockPosition();
        Location location = blockPosition.toLocation(player.getWorld());

        Board board = gameManager.getBoard(player);

        if (board != null && board.isBlockOutsideGame(location))
            return;

        if (board == null) {
            Board watching = gameManager.getBoardWatched(player);

            if (watching != null) {
                Field field = watching.getField(location);

                if (watching.isBlockOutsideGame(location))
                    return;

                if (field != null) {
                    Material[] materials = new Material[]{getActualMaterial(field), field.getMark()};
                    int i = location.getBlockY() - game.getFieldHeight();
                    if (i < materials.length && i > 0)
                        PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[i]));
                }
                player.getInventory().setContents(Inventories.VIEWER_INVENTORY);
                event.setCancelled(true);
            }
            return;
        }

        Field field = board.getField(location);
        if (packet.getHands().read(0) == EnumWrappers.Hand.OFF_HAND) {
            event.setCancelled(true);
            return;
        }


        clickHandler.rightClick(player, board, field, event);
    }

    @Override
    public void onLeftClick(@NotNull Player player, @NotNull PacketEvent event, @NotNull Game game, @NotNull PacketContainer packet) {
        BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
        Location location = blockPosition.toLocation(player.getWorld());

        Board board = gameManager.getBoard(player);
        if (board != null && board.isBlockOutsideGame(location))
            return;

        if (board == null)
            board = gameManager.getBoardWatched(player);

        if (board == null) {
            Board watching = gameManager.getBoardWatched(player);

            if (watching != null) {
                Field field = watching.getField(location);
                if (field == null)
                    return;
                Material[] materials = new Material[]{getActualMaterial(field), field.getMark()};

                if (game.getFieldHeight() - location.getBlockY() < 0)
                    return;

                PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[game.getFieldHeight() - location.getBlockY()]));
            }
            return;
        }

        Field field = board.getField(location);

        EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().read(0);
        if (field != null && digType == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
            if (location.getBlockY() - game.getFieldHeight() == 0) {
                event.setCancelled(true);
            } else if (location.getBlockY() - game.getFieldHeight() == 1) {
                PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(field.getMark()));
            }
        }

        if (digType != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK)
            return;

        clickHandler.leftClick(player, game, blockPosition, board, field, location);
    }

    @Override
    public void highlightField(@NotNull Field field, @NotNull List<Player> players) {
        PacketContainer spawnEntityContainer = PacketUtil.getSpawnEntityContainer(field.getLocation().clone().add(0.5, 0, 0.5), EntityType.SLIME);
        int entityId = spawnEntityContainer.getIntegers().read(0);

        PacketContainer slimeMetadata = PacketUtil.getSlimeMetadata(entityId);
        PacketContainer noCollision = PacketUtil.joinTeam("no_collision", Collections.singletonList(spawnEntityContainer.getUUIDs().read(0)));

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        players.forEach(player -> {
            try{
                protocolManager.sendServerPacket(player, spawnEntityContainer);
                protocolManager.sendServerPacket(player, slimeMetadata);
                protocolManager.sendServerPacket(player, noCollision);
            }catch(InvocationTargetException e){
                throw new RuntimeException(e);
            }
        });
    }

    public Material getActualMaterial(@NotNull Field field) {
        boolean lightField = Board.isLightField(field.getX(), field.getY());

        if (field.getBoard().isFinished() && field.isBomb() && (!field.isCovered() || field.getBoard().isLose()))
            return Material.COAL_BLOCK;

        if (field.isCovered())
            return lightField ? LIGHT_DEFAULT : DARK_DEFAULT;

        return (lightField ? LIGHT_MATERIALS : DARK_MATERIALS)[field.getNeighborCount()];
    }

}
