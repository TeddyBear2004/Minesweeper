package de.teddybear2004.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LeftClickEvent extends PacketAdapter {


    private final GameManager gameManager;

    public LeftClickEvent(Plugin plugin, GameManager gameManager) {
        super(plugin, getPacketTypes());
        this.gameManager = gameManager;
    }

    private static PacketType @NotNull [] getPacketTypes() {
        Set<PacketType> types = new HashSet<>();
        Painter.PAINTER_MAP.values().forEach(painter -> types.addAll(painter.getLeftClickPacketType()));

        return types.toArray(new PacketType[0]);
    }

    @Override
    public void onPacketReceiving(@NotNull PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        Game game = gameManager.getGame(player);
        Painter painter = Painter.getPainter(player);
        if (game == null || painter == null)
            return;

        if (painter.getLeftClickPacketType().contains(packet.getType()))
            painter.onLeftClick(player, event, game, packet);
    }

}