package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LeftClickEvent implements PacketListener {

    public static final Map<Player, Long> LAST_CLICKED = new HashMap<>();

    @Override
    public void onPacketSending(PacketEvent event) { }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        Game game = Game.getGame(player);
        Painter painter = Game.PAINTER_MAP.get(Game.PLAYER_PAINTER_MAP.get(player));
        if (game == null || painter == null)
            return;

        if(painter.getLeftClickPacketType().contains(packet.getType()))
            painter.onLeftClick(player, event, game, packet);
    }

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        Set<PacketType> types = new HashSet<>();

        Game.PAINTER_MAP.values().forEach(painter -> types.addAll(painter.getLeftClickPacketType()));

        return ListeningWhitelist
                .newBuilder()
                .gamePhase(GamePhase.PLAYING)
                .types(types.toArray(new PacketType[0]))
                .high()
                .build();
    }

    @Override
    public Plugin getPlugin() {
        return Minesweeper.getPlugin();
    }

}
