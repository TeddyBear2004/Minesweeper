package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LeftClickEvent extends PacketAdapter {

    public static final Map<Player, Long> LAST_CLICKED = new HashMap<>();

    public LeftClickEvent(Plugin plugin) {
        super(plugin, getPacketTypes());
    }

    private static PacketType[] getPacketTypes() {
        Set<PacketType> types = new HashSet<>();
        Game.PAINTER_MAP.values().forEach(painter -> types.addAll(painter.getLeftClickPacketType()));

        return types.toArray(new PacketType[0]);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        Game game = Game.getGame(player);
        Painter painter = Game.getPainter(player);
        if (game == null || painter == null)
            return;

        if (painter.getLeftClickPacketType().contains(packet.getType()))
            painter.onLeftClick(player, event, game, packet);
    }

}
