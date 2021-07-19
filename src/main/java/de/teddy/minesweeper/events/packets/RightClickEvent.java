package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.util.IsBetween;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class RightClickEvent implements PacketListener {
    @Override
    public void onPacketSending(PacketEvent event){}

    @Override
    public void onPacketReceiving(PacketEvent event){
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();
        Game game = Game.getGame(player);

        EnumWrappers.Hand hand = packet.getHands().read(0);
        List<MovingObjectPositionBlock> movingObjectPositionBlocks = packet.getMovingBlockPositions()
                .getValues();

        if(game == null
                || movingObjectPositionBlocks == null
                || movingObjectPositionBlocks.size() == 0
                || !Game.isBlockInsideGameField(movingObjectPositionBlocks.get(0).getBlockPosition().toLocation(player.getWorld()).getBlock()))
            return;

        Location location = movingObjectPositionBlocks.get(0).getBlockPosition().toLocation(player.getWorld());
        Board board = game.getBoard(player);

        if((board == null
                || !IsBetween.isBetween2D(board.getCorner(), board.getWidth(), board.getHeight(), location.getBlock()))
                || !IsBetween.isBetween(board.getCorner().getBlockY(), board.getCorner().getBlockY() + 1, location.getBlockY()))
            return;
        event.setCancelled(true);

        Board.Field field = board.getField(location.getBlockX(), location.getBlockZ());
        if(field == null || hand == EnumWrappers.Hand.OFF_HAND || board.isFinished())
            return;

        if(field.isCovered())
            field.reverseMark();

        board.draw();
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
                .types(PacketType.Play.Client.USE_ITEM)
                .high()
                .build();
    }

    @Override
    public Plugin getPlugin(){
        return Minesweeper.INSTANCE;
    }
}
