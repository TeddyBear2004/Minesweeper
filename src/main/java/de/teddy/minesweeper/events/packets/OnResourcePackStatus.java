package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.teddy.minesweeper.Minesweeper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class OnResourcePackStatus implements PacketListener {
    @Override
    public void onPacketSending(PacketEvent event){}

    @Override
    public void onPacketReceiving(PacketEvent event){
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        EnumWrappers.ResourcePackStatus read = packet.getResourcePackStatus().read(0);

        if(read == EnumWrappers.ResourcePackStatus.SUCCESSFULLY_LOADED)
            return;

        if(read == EnumWrappers.ResourcePackStatus.FAILED_DOWNLOAD){
            player.sendMessage(ChatColor.DARK_RED + Minesweeper.getLanguage().getString("resource_pack_error"));
            return;
        }

        if(read == EnumWrappers.ResourcePackStatus.DECLINED){
            player.sendMessage(ChatColor.DARK_RED + Minesweeper.getLanguage().getString("resource_pack_not_enabled"));
        }
    }

    @Override
    public ListeningWhitelist getSendingWhitelist(){
        return ListeningWhitelist.EMPTY_WHITELIST;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist(){
        return ListeningWhitelist
                .newBuilder()
                .gamePhase(GamePhase.PLAYING)
                .types(PacketType.Play.Client.RESOURCE_PACK_STATUS)
                .high()
                .build();
    }

    @Override
    public Plugin getPlugin(){
        return Minesweeper.getPlugin();
    }
}
