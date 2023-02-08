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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class RightClickEvent extends PacketAdapter {

    private final GameManager gameManager;
    private final Atelier atelier;

    public RightClickEvent(Plugin plugin, GameManager gameManager, Atelier atelier) {
        super(plugin, getPacketTypes(atelier));
        this.gameManager = gameManager;
        this.atelier = atelier;
    }

    @SuppressWarnings("unchecked")
    private static PacketType @NotNull [] getPacketTypes(Atelier atelier) {
        Set<PacketType> types = new HashSet<>();

        atelier.getPainters().forEach(painter -> {
            Set<PacketType> rightClickPacketType = painter.getRightClickPacketType();

            types.addAll(rightClickPacketType);
        });


        return types.toArray(new PacketType[0]);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onPacketReceiving(@NotNull PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        if (player.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;

        Game game = gameManager.getGame(player);
        Board<?> board = gameManager.getBoard(player);

        Class<? extends Board> boardClass = board.getBoardClass();

        Painter<?> painter = atelier.getPainter(player, boardClass);
        if (game == null || painter == null)
            return;

        if (painter.getRightClickPacketType().contains(packet.getType()))
            painter.onRightClick(player, event, game, packet);
    }

}
