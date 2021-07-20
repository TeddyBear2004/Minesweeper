package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.util.IsBetween;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class LeftClickEvent implements PacketListener {
    private static final Map<Player, Long> lastClicked = new HashMap<>();

    @Override
    public void onPacketSending(PacketEvent event){}

    @Override
    public void onPacketReceiving(PacketEvent event){
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();
        BlockPosition blockPosition = packet.getBlockPositionModifier().getValues().get(0);
        Location location = blockPosition.toLocation(player.getWorld());
        Game game = Game.getGame(player);

        if(!Game.isBlockInsideGameField(blockPosition.toLocation(player.getWorld()).getBlock()))
            return;

        if(game == null)
            return;
        Board board = game.getBoard(player);

        if(!event.isCancelled()){
            event.setCancelled(true);
            if(board == null)
                if(location.getY() == game.getFieldHeight())
                    PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(game.getDefaultMaterialAt(location)));
                else if(location.getY() + 1 == game.getFieldHeight())
                    PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(Material.AIR));
        }


        if(board == null
                || !IsBetween.isBetween2D(board.getCorner(), board.getWidth(), board.getHeight(), location.getBlock())
                || board.getCorner().getBlockY() != location.getBlockY()
                || !Game.isBlockInsideGameField(location.getBlock()))
            return;

        if(packet.getPlayerDigTypes().getValues().get(0) != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK
                || board.isFinished())
            return;

        Board.Field field = board.getField(location.getBlockX(), location.getBlockZ());

        try{
            if(field == null){
                board.checkField(location.getBlockX(), location.getBlockZ());
                board.draw();
                return;
            }

            if(field.isMarked())
                return;

            if(field.isCovered()){
                board.checkField(location.getBlockX(), location.getBlockZ());
            }else if(System.currentTimeMillis() - lastClicked.getOrDefault(player, (long)-1000) <= 350){
                try{
                    board.checkNumber(location.getBlockX(), location.getBlockZ());
                }catch(ArrayIndexOutOfBoundsException ignore){
                }
            }
            lastClicked.put(player, System.currentTimeMillis());
        }catch(BombExplodeException e){
            board.lose();
        }
        board.draw();
        board.checkIfWon(player);
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
                .types(PacketType.Play.Client.BLOCK_DIG)
                .high()
                .build();
    }

    @Override
    public Plugin getPlugin(){
        return Minesweeper.INSTANCE;
    }
}
