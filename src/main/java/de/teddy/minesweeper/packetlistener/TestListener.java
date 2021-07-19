package de.teddy.minesweeper.packetlistener;

import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.plugin.Plugin;

public class TestListener implements PacketListener {
    @Override
    public void onPacketSending(PacketEvent event){

    }

    @Override
    public void onPacketReceiving(PacketEvent event){

    }

    @Override
    public ListeningWhitelist getSendingWhitelist(){
        return null;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist(){
        return null;
    }

    @Override
    public Plugin getPlugin(){
        return null;
    }
}
