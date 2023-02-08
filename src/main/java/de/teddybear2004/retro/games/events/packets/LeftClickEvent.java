package de.teddybear2004.retro.games.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.game.painter.Painter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LeftClickEvent extends PacketAdapter {

    private final GameManager gameManager;
    private final Atelier atelier;

    public LeftClickEvent(Plugin plugin, GameManager gameManager, Atelier atelier) {
        super(plugin, getPacketTypes(atelier));
        this.gameManager = gameManager;
        this.atelier = atelier;
    }

    @SuppressWarnings("unchecked")
    private static PacketType @NotNull [] getPacketTypes(Atelier atelier) {
        Set<PacketType> types = new HashSet<>();

        atelier.getPainters().forEach(painter -> {
            Set<PacketType> leftClickPacketType = painter.getLeftClickPacketType();

            types.addAll(leftClickPacketType);
        });

        return types.toArray(new PacketType[0]);
    }

    @Override
    public void onPacketReceiving(@NotNull PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        Game game = gameManager.getGame(player);
        Board<?> board = gameManager.getBoard(player);
        if (board == null)
            return;

        Painter<?> painter = atelier.getPainter(player, board.getBoardClass());
        if (game == null || painter == null)
            return;

        if (painter.getLeftClickPacketType().contains(packet.getType()))
            painter.onLeftClick(player, event, game, packet);
    }

}
