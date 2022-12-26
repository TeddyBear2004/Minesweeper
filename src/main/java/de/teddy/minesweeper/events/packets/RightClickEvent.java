package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.GamePhase;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RightClickEvent extends PacketAdapter {
    public RightClickEvent(Plugin plugin){
        super(plugin, getPacketTypes());
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        if (player.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;

        Game game = Game.getGame(player);
        Painter painter = Game.getPainter(player);
        if (game == null || painter == null)
            return;

        if (painter.getRightClickPacketType().contains(packet.getType()))
            painter.onRightClick(player, event, game, packet);
    }

    private static PacketType[] getPacketTypes(){
        Set<PacketType> types = new HashSet<>();
        Game.PAINTER_MAP.values().forEach(painter -> types.addAll(painter.getRightClickPacketType()));

        return types.toArray(new PacketType[0]);
    }
}
