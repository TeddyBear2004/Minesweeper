package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.util.IsBetween;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RightClickEvent implements PacketListener {
    @Override
    public void onPacketSending(PacketEvent event){}

    @Override
    public void onPacketReceiving(PacketEvent event){
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();
        MovingObjectPositionBlock movingObjectPositionBlock = packet.getMovingBlockPositions().getValues().get(0);
        Location location = movingObjectPositionBlock.getBlockPosition().toLocation(player.getWorld());
        EnumWrappers.Hand hand = packet.getHands().read(0);
        Game game = Game.getGame(player);

        if(!Game.isBlockInsideGameField(location.getBlock()))
            return;

        if(game == null)
            return;
        Board board = game.getBoard(player);

        if(!event.isCancelled()){
            event.setCancelled(true);
            if(board == null)
                if(location.getY() == game.getFieldHeight())
                    PacketUtil.sendBlockChange(player, new BlockPosition(location.toVector()), WrappedBlockData.createData(game.getDefaultMaterialAt(location)));
                else if(location.getY() - 1 == game.getFieldHeight())
                    PacketUtil.sendBlockChange(player, new BlockPosition(location.toVector()), WrappedBlockData.createData(Material.AIR));
        }
        if((board == null
                || !IsBetween.isBetween2D(board.getCorner(), board.getWidth(), board.getHeight(), location.getBlock()))
                || !IsBetween.isBetween(board.getCorner().getBlockY(), board.getCorner().getBlockY() + 1, location.getBlockY()))
            return;

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
