package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import de.teddy.minesweeper.events.packets.LeftClickEvent;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.util.HeadGenerator;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ArmorStandPainter implements Painter {

    public static final ItemStack[] ITEM_STACKS = {
            new ItemStack(Material.AIR),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/301d0496e606b74327f03259c7f6e32ba76862d8dda5b639b229d2e6781ff143"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/cb08394f844dc8eeeaadf05d8bdaa045dbcc0db932cfb8d0daeabafcee995f15"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/858b9d62566a0e08f47d849f7d21e3adb9c99ab36d45d54aee987c68dcd2b313"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/4ffb37d1eb6a453fea35eb6d6a71d3bdbd68ef1f59f96be26317ce2ef57170f7"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/d5b6f519bd847dec7aaf003852c60055ff10bd55b1df7666ac90f700513fdd49"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/976ead580e797d6cba3f98ca7f7be1bb642da5485e6afb0ce641a4ac3d37a408"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/581559800cf78b5324519d939f69c83d4a41a00b1bd770cf425fb9a65e3d1d45"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/aae59430f811d4037d548e3891f03667393aee15d0ded4da64ca84973b0d60db")
    };
    public static final Material LIGHT_DEFAULT = Material.LIME_CONCRETE_POWDER;
    public static final Material DARK_DEFAULT = Material.GREEN_CONCRETE_POWDER;
    private final Plugin plugin;
    private Map<Integer, ItemStack> currentItemStackPerEntityId = new HashMap<>();
    private Map<Integer, int[]> armorStandEntityIds;
    private Map<int[], Integer> locationEntityIds;

    public ArmorStandPainter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void drawBlancField(Board board, List<Player> players) {
        if (board == null || !Board.notTest)
            return;

        if (armorStandEntityIds != null) {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

            Set<Integer> set = this.armorStandEntityIds.keySet();
            int[] arr = new int[set.size()];
            int index = 0;
            for (int i : set) {
                arr[index] = i;
                index++;
            }
            PacketContainer removeEntity = PacketUtil.getRemoveEntity(arr);

            players.forEach(player -> {
                try{
                    protocolManager.sendServerPacket(player, removeEntity);
                }catch(InvocationTargetException e){
                    throw new RuntimeException(e);
                }
            });

            this.armorStandEntityIds = null;
            this.locationEntityIds = null;
            this.currentItemStackPerEntityId = null;
        }
    }

    @Override
    public void drawField(Board board, List<Player> players) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        if (this.armorStandEntityIds == null) {
            this.armorStandEntityIds = new HashMap<>();
            this.locationEntityIds = new HashMap<>();
            this.currentItemStackPerEntityId = new HashMap<>();

            // Create a list of packets that need to be sent to each player
            List<PacketContainer> packets = new ArrayList<>();

            for (int i = 0; i < board.getWidth(); i++) {
                for (int j = 0; j < board.getHeight(); j++) {
                    Location location = board.getCorner().clone().add(i, 0, j).add(0.5, -0.775, 0.5);

                    // Create the spawn and metadata packets for the armor stand
                    PacketContainer spawnEntityContainer = PacketUtil.getSpawnEntityContainer(location);
                    int id = spawnEntityContainer.getIntegers().read(0);

                    PacketContainer entityMetadata = PacketUtil.getEntityMetadata(id);

                    // Add the packets to the list
                    packets.add(spawnEntityContainer);
                    packets.add(entityMetadata);

                    // Store the entity ID and current material for the armor stand
                    int[] ints = {i, j};
                    this.armorStandEntityIds.put(id, ints);
                    this.locationEntityIds.put(ints, id);
                    this.currentItemStackPerEntityId.put(id, null);
                }
            }

            // Send the packets to each player
            for (Player player : players) {
                packets.forEach(packetContainer -> {
                    try{
                        protocolManager.sendServerPacket(player, packetContainer);
                    }catch(InvocationTargetException e){
                        throw new RuntimeException(e);
                    }
                });

            }
        }

        // Create a list of packets that need to be sent to each player
        List<PacketContainer> packets = new ArrayList<>();

        // Iterate over the armor stand entity IDs
        for (int entityId : this.armorStandEntityIds.keySet()) {
            int[] coords = this.armorStandEntityIds.get(entityId);
            int i = coords[0];
            int j = coords[1];

            Board.Field field = board.getField(i, j);

            ItemStack itemStack;

            if (field == null) {
                itemStack = new ItemStack(Board.isLightField(i, j) ? LIGHT_DEFAULT : DARK_DEFAULT);
            } else {
                if (field.isMarked()) {
                    itemStack = new ItemStack(Material.REDSTONE_BLOCK);
                } else {
                    itemStack = getActualItemStack(field);
                }
            }

            if (itemStack != this.currentItemStackPerEntityId.get(entityId)) {
                this.currentItemStackPerEntityId.put(entityId, itemStack);
                packets.add(PacketUtil.getItemOnEntityHead(entityId, itemStack));
            }
        }

        // Send the packets to each player
        for (Player player : players) {
            packets.forEach(packetContainer -> {
                try{
                    protocolManager.sendServerPacket(player, packetContainer);
                }catch(InvocationTargetException e){
                    throw new RuntimeException(e);
                }
            });

        }
    }

    @Override
    public void drawBombs(Board board, List<Player> players) {
        double explodeDuration = 0.5d;
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        for (Point2D point2D : board.getBombList()) {
            Location clone = board.getCorner().clone();

            clone.setX(board.getCorner().getBlockX() + point2D.getX());
            clone.setZ(board.getCorner().getBlockZ() + point2D.getY());


            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Integer integer = this.locationEntityIds.get(new int[]{(int) point2D.getX(), (int) point2D.getY()});
                if (integer == null)
                    return;
                PacketContainer itemOnEntityHead = PacketUtil.getItemOnEntityHead(integer, new ItemStack(Material.COAL_BLOCK));

                for (Player p : players) {
                    try{
                        protocolManager.sendServerPacket(p, itemOnEntityHead);
                    }catch(InvocationTargetException e){
                        throw new RuntimeException(e);
                    }
                    PacketUtil.sendSoundEffect(p, Sound.BLOCK_STONE_PLACE, 1f, clone);
                }
            }, (long) (20 * explodeDuration));

            explodeDuration *= 0.7;
        }
    }

    @Override
    public ItemStack getActualItemStack(Board.Field field) {
        boolean lightField = Board.isLightField(field.getX(), field.getY());

        if (field.getBoard().isFinished() && field.isBomb() && (!field.isCovered() || !field.getBoard().isWin()))
            return new ItemStack(Material.COAL_BLOCK);

        if (field.isCovered())
            return new ItemStack(lightField ? LIGHT_DEFAULT : DARK_DEFAULT);

        return ITEM_STACKS[field.getNeighborCount()];
    }

    @Override
    public Material getActualMaterial(Board.Field field) {
        return getActualItemStack(field).getType();
    }

    @Override
    public List<PacketType> getRightClickPacketType() {
        return List.of(PacketType.Play.Client.USE_ENTITY, PacketType.Play.Client.USE_ITEM);
    }

    @Override
    public List<PacketType> getLeftClickPacketType() {
        return List.of(PacketType.Play.Client.USE_ENTITY, PacketType.Play.Client.BLOCK_DIG);
    }

    @Override
    public void onRightClick(Player player, PacketEvent event, Game game, PacketContainer packet) {
        if (packet.getType() == PacketType.Play.Client.USE_ITEM) {
            Game.PAINTER_MAP.get(BlockPainter.class).onRightClick(player, event, game, packet);
            return;
        }

        WrappedEnumEntityUseAction read = event.getPacket().getEnumEntityUseActions().read(0);

        if (read.getAction() == EnumWrappers.EntityUseAction.ATTACK)
            return;

        Integer entityId = event.getPacket().getIntegers().read(0);

        Board board = Game.getBoard(player);
        if (board == null) return;

        Location location = getLocation(board, entityId);
        if (location == null || game.isBlockOutsideGame(location.getBlock()))
            return;

        Board.Field field = board.getField(location);
        if (field == null)
            return;

        event.setCancelled(true);
        player.getInventory().setContents(Inventories.GAME_INVENTORY);

        if (board.isFinished())
            return;

        if (field.isCovered())
            field.reverseMark();

        board.draw();
    }

    @Override
    public void onLeftClick(Player player, PacketEvent event, Game game, PacketContainer packet) {
        if (packet.getType() == PacketType.Play.Client.BLOCK_DIG) {
            Game.PAINTER_MAP.get(BlockPainter.class).onLeftClick(player, event, game, packet);
            return;
        }

        WrappedEnumEntityUseAction read = event.getPacket().getEnumEntityUseActions().read(0);

        if (read.getAction() != EnumWrappers.EntityUseAction.ATTACK)
            return;

        Integer entityId = event.getPacket().getIntegers().read(0);
        Board board = Game.getBoard(player);

        if (board == null)
            return;
        Location location = getLocation(board, entityId);

        if (location == null)
            return;

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (game.isBlockOutsideGame(location.getBlock()))
            return;

        Board.Field field = board.getField(location);


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

                if (field.isCovered()) {
                    board.checkField(location.getBlockX(), location.getBlockZ());
                } else if (System.currentTimeMillis() - LeftClickEvent.LAST_CLICKED.getOrDefault(player, (long) -1000) <= 350) {
                    board.checkNumber(location.getBlockX(), location.getBlockZ());
                }

                LeftClickEvent.LAST_CLICKED.put(player, System.currentTimeMillis());
            }catch(BombExplodeException e){
                board.lose();
            }

            board.draw();
            board.checkIfWon();
        }
    }

    public Location getLocation(Board board, int entityId) {
        int[] ints = this.armorStandEntityIds.get(entityId);
        if (ints == null) return null;
        return board.getCorner().clone().add(ints[0], 0, ints[1]);
    }

}
