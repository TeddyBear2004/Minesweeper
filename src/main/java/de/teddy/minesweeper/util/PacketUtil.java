package de.teddy.minesweeper.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class PacketUtil {
    public static void sendActionBar(Player player, String message){
        /*
        PacketEvent[player=CraftPlayer{name=TeddyBear_2004},
        packet=PacketContainer[type=CHAT[class=PacketPlayOutChat, id=15],
        structureModifier=StructureModifier[fieldType=class java.lang.Object, data=[

        private net.minecraft.server.v1_16_R3.IChatBaseComponent
        net.minecraft.server.v1_16_R3.PacketPlayOutChat.a,

        public net.kyori.adventure.text.Component
        net.minecraft.server.v1_16_R3.PacketPlayOutChat.adventure$message,

        public net.md_5.bungee.api.chat.BaseComponent[]
        net.minecraft.server.v1_16_R3.PacketPlayOutChat.components,

        private net.minecraft.server.v1_16_R3.ChatMessageType
        net.minecraft.server.v1_16_R3.PacketPlayOutChat.b,

        private java.util.UUID
        net.minecraft.server.v1_16_R3.PacketPlayOutChat.c]]]]
         */
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

    public static void sendSoundEffect(Player player, Sound sound, EnumWrappers.SoundCategory soundCategory, BlockPosition blockPosition, float volume, float pitch){
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.NAMED_SOUND_EFFECT, true);

        packet.getSoundEffects()
                .write(0, sound);

        packet.getIntegers()
                .write(0,blockPosition.getX())
                .write(1,blockPosition.getY())
                .write(2,blockPosition.getZ());

        packet.getFloat()
                .write(0, volume);
        packet.getFloat()
                .write(1, pitch);

        packet.getSoundCategories()
                .write(0, soundCategory);

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
