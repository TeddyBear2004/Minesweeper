package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import de.teddy.minesweeper.game.GameManager;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class RightClickEvent extends PacketAdapter {

    private final GameManager gameManager;

    public RightClickEvent(Plugin plugin, GameManager gameManager){
        super(plugin, getPacketTypes());
        this.gameManager = gameManager;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        if (player.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;

        Game game = gameManager.getGame(player);
        Painter painter = Painter.getPainter(player);
        if (game == null || painter == null)
            return;

        if (painter.getRightClickPacketType().contains(packet.getType()))
            painter.onRightClick(player, event, game, packet);
    }

    private static PacketType[] getPacketTypes(){
        Set<PacketType> types = new HashSet<>();
        Painter.PAINTER_MAP.values().forEach(painter -> types.addAll(painter.getRightClickPacketType()));

        return types.toArray(new PacketType[0]);
    }
}
