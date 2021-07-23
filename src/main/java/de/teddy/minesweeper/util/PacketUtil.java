package de.teddy.minesweeper.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
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

    public static void sendExplosion(Player player, double x, double y, double z, float strength){
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.EXPLOSION, true);

        packet.getDoubles()
                .write(0, x)
                .write(1, y)
                .write(2, z);
        packet.getFloat()
                .write(0, strength)
                .write(1, (float)0)
                .write(1, (float)0)
                .write(1, (float)0);

        try{
            protocolManager.sendServerPacket(player, packet);
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public static void sendParticleEffect(Player player, Location location){
        /*
        private double
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.a,

        private double
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.b,

        private double
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.c,

        private float
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.d,

        private float
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.e,

        private float
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.f,

        private float
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.g,

        private int
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.h,

        private boolean
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.i,

        private net.minecraft.server.v1_16_R3.ParticleParam
        net.minecraft.server.v1_16_R3.PacketPlayOutWorldParticles.j
         */
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES, true);

        packet.getParticles()
                .write(0, EnumWrappers.Particle.EXPLOSION_NORMAL);

        packet.getBooleans()
                .write(0, false);

        packet.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        packet.getIntegers()
                .write(0,100);

        try{
            protocolManager.sendServerPacket(player,packet);
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
