package de.teddy.minesweeper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.teddy.minesweeper.commands.StartCommand;
import de.teddy.minesweeper.events.GenericEvents;
import de.teddy.minesweeper.events.GenericRightClickEvent;
import de.teddy.minesweeper.events.OnInventory;
import de.teddy.minesweeper.events.packets.LeftClickEvent;
import de.teddy.minesweeper.events.packets.OnResourcePackStatus;
import de.teddy.minesweeper.events.packets.RightClickEvent;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.Inventories;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Minesweeper extends JavaPlugin {
    public static World WORLD;
    public static JavaPlugin INSTANCE;

    @Override
    public void onEnable(){
        INSTANCE = this;

        WORLD = new WorldCreator("MineSweeper").createWorld();
        Objects.requireNonNull(this.getCommand("start")).setExecutor(new StartCommand());

        getServer().getPluginManager().registerEvents(new GenericEvents(), this);
        getServer().getPluginManager().registerEvents(new OnInventory(), this);
        getServer().getPluginManager().registerEvents(new GenericRightClickEvent(), this);

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().setContents(Inventories.viewerInventory);
            Game.MAP10X10.startViewing(player, null);
        });

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new RightClickEvent());
        protocolManager.addPacketListener(new LeftClickEvent());
        protocolManager.addPacketListener(new OnResourcePackStatus());

        /*protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event){
                System.out.println(event);
            }

            @Override
            public void onPacketReceiving(PacketEvent event){
                System.out.println(event);
            }
        });*/
    }
}
