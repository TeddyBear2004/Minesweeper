package de.teddy.minesweeper.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PacketUtil {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(Integer.MIN_VALUE + 1000);
    private static final WrappedDataWatcher.Serializer INT_SERIALIZER = WrappedDataWatcher.Registry.get(Integer.class);
    private static final WrappedDataWatcher.Serializer BYTE_SERIALIZER = WrappedDataWatcher.Registry.get(Byte.class);
    private static WrappedDataWatcher armorStandDataWatcher;
    private static WrappedDataWatcher slimeDataWatcher;

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public static void sendParticleEffect(Player player, Location location, Particle particle, float xDifference, float zDifference, int count) {
        player.spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, xDifference, 0, zDifference);
    }

    public static void sendSoundEffect(Player player, Sound sound, float volume, Location blockPosition) {
        player.playSound(blockPosition, sound, volume, 1f);
    }

    public static PacketContainer getBlockChange(BlockPosition blockPosition, WrappedBlockData wrappedBlockData) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);

        packet.getBlockData()
                .write(0, wrappedBlockData);

        packet.getBlockPositionModifier()
                .write(0, blockPosition);

        return packet;
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

    public static PacketContainer getMultiBlockChange(short[] shorts, BlockPosition blockPosition, WrappedBlockData[] wrappedBlockData, boolean b) {
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

    public static PacketContainer getSpawnEntityContainer(Location location, EntityType type) {
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

    public static PacketContainer getArmorStandMetadata(int entityId) {
        WrappedDataWatcher dataWatcher = getDefaultWrappedDataWatcherForArmorStands();

        PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        metadataPacket.getIntegers().write(0, entityId);

        metadataPacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

        return metadataPacket;
    }

    private static WrappedDataWatcher getDefaultWrappedDataWatcherForArmorStands() {
        if (armorStandDataWatcher != null)
            return armorStandDataWatcher;
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();


        wrappedDataWatcher.setObject(0, BYTE_SERIALIZER, (byte) 0x20);
        wrappedDataWatcher.setObject(1, INT_SERIALIZER, 300);
        wrappedDataWatcher.setObject(7, INT_SERIALIZER, 0);
        wrappedDataWatcher.setObject(8, BYTE_SERIALIZER, (byte) 0);
        wrappedDataWatcher.setObject(9, INT_SERIALIZER, 20);
        wrappedDataWatcher.setObject(10, INT_SERIALIZER, 0);
        wrappedDataWatcher.setObject(12, INT_SERIALIZER, 0);
        wrappedDataWatcher.setObject(13, INT_SERIALIZER, 0);
        wrappedDataWatcher.setObject(15, BYTE_SERIALIZER, (byte) 0x08);

        armorStandDataWatcher = wrappedDataWatcher;
        return wrappedDataWatcher;
    }


    public static PacketContainer getItemOnEntityHead(int entityId, ItemStack item) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT, true);

        packet.getIntegers().write(0, entityId);
        packet.getSlotStackPairLists().write(0, Collections.singletonList(new Pair<>(EnumWrappers.ItemSlot.HEAD, item)));

        return packet;
    }

    public static PacketContainer getSlimeMetadata(int entityId) {
        WrappedDataWatcher dataWatcher = getDefaultWrappedDataWatcherForSlimes();

        PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        metadataPacket.getIntegers().write(0, entityId);

        metadataPacket.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());

        return metadataPacket;
    }

    public static PacketContainer joinTeam(String teamName, List<UUID> uuids) {
        PacketContainer scoreboardTeamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        List<String> strings = uuids.stream().map(UUID::toString).toList();

        /*scoreboardTeamPacket.getStrings().write(0, teamName);
        scoreboardTeamPacket.getIntegers().write(0, 3);
        scoreboardTeamPacket.getSpecificModifier(List.class).write(0, strings);*/

        return scoreboardTeamPacket;
    }

    private static WrappedDataWatcher getDefaultWrappedDataWatcherForSlimes() {
        if (slimeDataWatcher != null)
            return slimeDataWatcher;
        WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();


        wrappedDataWatcher.setObject(0, BYTE_SERIALIZER, (byte) (/*0x20 | */0x40));
        wrappedDataWatcher.setObject(9, INT_SERIALIZER, 20);
        wrappedDataWatcher.setObject(15, BYTE_SERIALIZER, (byte) 0x01);
        wrappedDataWatcher.setObject(16, INT_SERIALIZER, 0);

        slimeDataWatcher = wrappedDataWatcher;
        return wrappedDataWatcher;
    }

    public static PacketContainer getRemoveEntity(int... entityId) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY, true);

        packet.getIntLists().write(0, Arrays.stream(entityId)
                .boxed()
                .collect(Collectors.toList()));

        return packet;
    }


}
