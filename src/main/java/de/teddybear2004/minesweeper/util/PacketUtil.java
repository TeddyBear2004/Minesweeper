package de.teddybear2004.minesweeper.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PacketUtil {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(Integer.MIN_VALUE + 1000);
    private static final WrappedDataWatcher.Serializer BYTE_SERIALIZER = WrappedDataWatcher.Registry.get(Byte.class);
    private static WrappedDataWatcher armorStandDataWatcher;

    public static void sendActionBar(@NotNull Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public static void sendParticleEffect(@NotNull Player player, @NotNull Location location, @NotNull Particle particle, float xDifference, float zDifference, int count) {
        player.spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, xDifference, 0, zDifference);
    }

    public static void sendSoundEffect(@NotNull Player player, @NotNull Sound sound, float volume, @NotNull Location blockPosition) {
        player.playSound(blockPosition, sound, volume, 1f);
    }

    public static void sendBlockChange(Player player, BlockPosition blockPosition, WrappedBlockData wrappedBlockData) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = getBlockChange(blockPosition, wrappedBlockData);

        try{
            protocolManager.sendServerPacket(player, packet);
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public static @NotNull PacketContainer getBlockChange(BlockPosition blockPosition, WrappedBlockData wrappedBlockData) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);

        packet.getBlockData()
                .write(0, wrappedBlockData);

        packet.getBlockPositionModifier()
                .write(0, blockPosition);

        return packet;
    }

    public static @NotNull PacketContainer getMultiBlockChange(short[] shorts, BlockPosition blockPosition, WrappedBlockData[] wrappedBlockData, boolean b) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE, true);

        packet.getSectionPositions()
                .write(0, blockPosition);

        packet.getShortArrays()
                .write(0, shorts);

        packet.getBlockDataArrays()
                .write(0, wrappedBlockData);

        packet.getBooleans()
                .write(0, b);

        return packet;
    }

    public static @NotNull PacketContainer getSpawnEntityContainer(@NotNull Location location, EntityType type) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY, true);

        if (ATOMIC_INTEGER.get() > -10000) {
            ATOMIC_INTEGER.set(Integer.MIN_VALUE + 1000);
        }
        int i = ATOMIC_INTEGER.incrementAndGet();

        packet.getIntegers().write(0, i);

        packet.getUUIDs()
                .write(0, UUID.randomUUID());

        packet.getEntityTypeModifier()
                .write(0, type);


        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        return packet;
    }

    public static @NotNull PacketContainer getArmorStandMetadata(int entityId) {
        WrappedDataWatcher dataWatcher = getDefaultWrappedDataWatcherForArmorStands();

        PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metadataPacket.getIntegers().write(0, entityId);
        metadataPacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

        return metadataPacket;
    }

    private static @NotNull WrappedDataWatcher getDefaultWrappedDataWatcherForArmorStands() {
        if (armorStandDataWatcher != null)
            return armorStandDataWatcher;

        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();

        wrappedDataWatcher.setObject(0, BYTE_SERIALIZER, (byte) 0x20);
        wrappedDataWatcher.setObject(15, BYTE_SERIALIZER, (byte) 0x08);

        armorStandDataWatcher = wrappedDataWatcher;
        return wrappedDataWatcher;
    }


    public static @NotNull PacketContainer getItemOnEntityHead(int entityId, ItemStack item) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT, true);

        packet.getIntegers().write(0, entityId);
        packet.getSlotStackPairLists().write(0, Collections.singletonList(new Pair<>(EnumWrappers.ItemSlot.HEAD, item)));

        return packet;
    }

    public static void sendBlockHighlight(Player pl, int x, int y, int z, int alpha, int time) {
        ByteBuf packet = Unpooled.buffer();
        packet.writeLong(PacketUtil.blockPosToLong(x, y, z));
        packet.writeInt(new Color(0xF0, 0xF8, 0xFF, alpha).getRGB());

        packet.writeInt(time);
        writeString(packet);

        PacketUtil.sendPayload(pl, "debug/game_test_add_marker", packet);
    }

    private static long blockPosToLong(int x, int y, int z) {
        return ((long) x & 67108863L) << 38 | (long) y & 4095L | ((long) z & 67108863L) << 12;
    }

    private static void writeString(ByteBuf packet) {
        byte[] abyte = " ".getBytes(StandardCharsets.UTF_8);
        d(packet, abyte.length);
        packet.writeBytes(abyte);
    }

    private static void sendPayload(Player receiver, String channel, ByteBuf bytes) {
        PacketContainer handle = new PacketContainer(PacketType.Play.Server.CUSTOM_PAYLOAD);
        handle.getMinecraftKeys().write(0, new MinecraftKey(channel));

        Object serializer = MinecraftReflection.getPacketDataSerializer(bytes);
        handle.getModifier().withType(ByteBuf.class).write(0, serializer);

        try{
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, handle);
        }catch(InvocationTargetException e){
            throw new RuntimeException("Unable to send the packet", e);
        }
    }

    private static void d(ByteBuf packet, int i) {
        while ((i & -128) != 0) {
            packet.writeByte(i & 127 | 128);
            i >>>= 7;
        }

        packet.writeByte(i);
    }

    public static void removeBlockHighlights(Player pl) {
        PacketUtil.sendPayload(pl, "debug/game_test_clear", Unpooled.wrappedBuffer(new byte[0]));
    }

    public static @NotNull PacketContainer getRemoveEntity(int @NotNull ... entityId) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY, true);

        packet.getIntLists().write(0, Arrays.stream(entityId)
                .boxed()
                .collect(Collectors.toList()));

        return packet;
    }

}
