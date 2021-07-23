package de.teddy.minesweeper.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class PacketUtil {
    public static void sendActionBar(Player player, String message){
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.CHAT, true);

        packet.getChatComponents()
                .write(0, WrappedChatComponent.fromLegacyText(message));

        packet.getChatTypes()
                .write(0, EnumWrappers.ChatType.GAME_INFO);
        try{
            protocolManager.sendServerPacket(player, packet);
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public static void sendParticleEffect(Player player, Location location, int size, WrappedParticle<?> wrappedParticle, float xDifference, float zDifference){
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES, true);

        packet.getBooleans()
                .write(0, false);

        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        packet.getFloat()
                .write(0, xDifference)
                .write(1, 0f)
                .write(2, zDifference);

        packet.getIntegers()
                .write(0, size);

        packet.getNewParticles()
                .write(0, wrappedParticle);

        try{
            protocolManager.sendServerPacket(player, packet);
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }

    }

    public static void sendSoundEffect(Player player, Sound sound, EnumWrappers.SoundCategory soundCategory, float volume, Location blockPosition){
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.NAMED_SOUND_EFFECT, true);

        packet.getSoundEffects()
                .write(0, sound);

        packet.getSoundCategories()
                .write(0, soundCategory);

        packet.getIntegers()
                .write(0, blockPosition.getBlockX() * 8)
                .write(1, blockPosition.getBlockY() * 8)
                .write(2, blockPosition.getBlockZ() * 8);

        packet.getFloat()
                .write(0, volume);

        try{
            protocolManager.sendServerPacket(player, packet);
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public static void sendBlockChange(Player player, BlockPosition blockPosition, WrappedBlockData wrappedBlockData){
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);

        packet.getBlockData()
                .write(0, wrappedBlockData);

        packet.getBlockPositionModifier()
                .write(0, blockPosition);

        try{
            protocolManager.sendServerPacket(player, packet);
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }
    }


    public static void sendMultiBlockChange(Player player, short[] shorts, BlockPosition blockPosition, WrappedBlockData[] wrappedBlockData, boolean b){
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

        try{
            protocolManager.sendServerPacket(player, packet);
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }
    }
}
