package de.teddybear2004.retro.games.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.click.ClickHandler;
import de.teddybear2004.retro.games.minesweeper.Field;
import de.teddybear2004.retro.games.minesweeper.MinesweeperField;
import de.teddybear2004.retro.games.scheduler.RemoveMarkerScheduler;
import de.teddybear2004.retro.games.util.HeadGenerator;
import de.teddybear2004.retro.games.util.PacketUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class ArmorStandPainter<F extends Field> implements Painter<F> {

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
    private final ClickHandler<F, Board<F>> clickHandler;
    private final GameManager gameManager;
    private final Class<? extends Board<F>> boardClass;
    private @Nullable Map<Integer, ItemStack> currentItemStackPerEntityId = new HashMap<>();
    private @Nullable Map<Integer, int[]> armorStandEntityIds;
    private @Nullable Map<int[], Integer> locationEntityIds;
    private BukkitTask bombTask;
    public ArmorStandPainter(Plugin plugin, ClickHandler<F, Board<F>> clickHandler, GameManager gameManager, Class<? extends Board<F>> boardClass) {
        this.plugin = plugin;
        this.clickHandler = clickHandler;
        this.gameManager = gameManager;
        this.boardClass = boardClass;
    }

    public @Nullable Map<int[], Integer> getLocationEntityIds() {
        return locationEntityIds;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public BukkitTask getBombTask() {
        return bombTask;
    }

    public void setBombTask(BukkitTask bombTask) {
        this.bombTask = bombTask;
    }

    @Override
    public @NotNull String getName() {
        return "heads";
    }

    @Override
    public void drawBlancField(@Nullable Board<F> board, @NotNull List<Player> players) {
        if (board == null)
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

            if (bombTask != null)
                this.bombTask.cancel();
        }
    }

    @Override
    public void drawField(@NotNull Board<F> board, @NotNull List<Player> players) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        if (this.armorStandEntityIds == null || this.locationEntityIds == null || this.currentItemStackPerEntityId == null) {
            this.armorStandEntityIds = new HashMap<>();
            this.locationEntityIds = new HashMap<>();
            this.currentItemStackPerEntityId = new HashMap<>();

            // Create a list of packets that need to be sent to each player
            List<PacketContainer> packets = new ArrayList<>();

            for (int i = 0; i < board.getWidth(); i++) {
                for (int j = 0; j < board.getHeight(); j++) {
                    Location location = board.getCorner().clone().add(i, 0, j).add(0.5, -0.775, 0.5);

                    // Create the spawn and metadata packets for the armor stand
                    PacketContainer spawnEntityContainer = PacketUtil.getSpawnEntityContainer(location, EntityType.ARMOR_STAND);
                    int id = spawnEntityContainer.getIntegers().read(0);

                    PacketContainer entityMetadata = PacketUtil.getArmorStandMetadata(id);

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

            F field = board.getField(i, j);

            ItemStack itemStack;

            if (field == null) {
                itemStack = new ItemStack(Board.isLightField(i, j) ? LIGHT_DEFAULT : DARK_DEFAULT);
            } else {
                itemStack = getActualItemStack(field);
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

    public @NotNull List<PacketType> getRightClickPacketType() {
        return List.of(PacketType.Play.Client.USE_ENTITY, PacketType.Play.Client.USE_ITEM);
    }

    @Override
    public @NotNull List<PacketType> getLeftClickPacketType() {
        return List.of(PacketType.Play.Client.USE_ENTITY, PacketType.Play.Client.BLOCK_DIG);
    }

    @Override
    public void onRightClick(@NotNull Player player, @NotNull PacketEvent event, Game game, @NotNull PacketContainer packet) {
        if (packet.getType() == PacketType.Play.Client.USE_ITEM) {
            PAINTER_MAP.get(MinesweeperField.class).get(BlockPainter.class).onRightClick(player, event, game, packet);
            return;
        }

        WrappedEnumEntityUseAction read = event.getPacket().getEnumEntityUseActions().read(0);

        if (read.getAction() == EnumWrappers.EntityUseAction.ATTACK)
            return;

        Integer entityId = event.getPacket().getIntegers().read(0);

        Board<F> board = boardClass.cast(gameManager.getBoard(player));
        if (board == null) return;

        Location location = getLocation(board, entityId);
        if (location == null || board.isBlockOutsideGame(location))
            return;

        F field = board.getField(location);


        clickHandler.rightClick(player, board, field, event);
    }

    @Override
    public void onLeftClick(Player player, @NotNull PacketEvent event, @NotNull Game game, @NotNull PacketContainer packet) {
        if (packet.getType() == PacketType.Play.Client.BLOCK_DIG) {
            PAINTER_MAP.get(MinesweeperField.class).get(BlockPainter.class).onLeftClick(player, event, game, packet);
            return;
        }

        WrappedEnumEntityUseAction read = event.getPacket().getEnumEntityUseActions().read(0);

        if (read.getAction() != EnumWrappers.EntityUseAction.ATTACK)
            return;

        Integer entityId = event.getPacket().getIntegers().read(0);
        Board<F> board = gameManager.getBoard(player, boardClass);

        if (board == null)
            return;
        Location location = getLocation(board, entityId);

        if (location == null)
            return;

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        if (board.isBlockOutsideGame(location))
            return;

        F field = board.getField(location);

        clickHandler.leftClick(player, game, blockPosition, board, field, location);
    }

    @Override
    public void highlightFields(List<F> fields, List<Player> players, RemoveMarkerScheduler removeMarkerScheduler) {

    }

    public @Nullable Location getLocation(@NotNull Board<F> board, int entityId) {
        if (this.armorStandEntityIds == null)
            return null;
        int[] ints = this.armorStandEntityIds.get(entityId);
        if (ints == null) return null;
        return board.getCorner().clone().add(ints[0], 0, ints[1]);
    }

    public abstract ItemStack getActualItemStack(@NotNull F field);

}
