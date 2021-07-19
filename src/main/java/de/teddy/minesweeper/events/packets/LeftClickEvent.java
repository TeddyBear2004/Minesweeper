package de.teddy.minesweeper.events.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.util.IsBetween;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeftClickEvent implements PacketListener {
    private static final Map<Player, Long> lastClicked = new HashMap<>();

    @Override
    public void onPacketSending(PacketEvent event){}

    @Override
    public void onPacketReceiving(PacketEvent event){
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();
        Game game = Game.getGame(player);
        List<BlockPosition> blockPositions = packet.getBlockPositionModifier().getValues();
        List<EnumWrappers.PlayerDigType> playerDigTypes = packet.getPlayerDigTypes().getValues();

        if(Game.isBlockInsideGameField(blockPositions.get(0).toLocation(player.getWorld()).getBlock()))
            event.setCancelled(true);

        if(game == null
                || playerDigTypes == null
                || playerDigTypes.size() == 0)
            return;
        BlockPosition blockPosition = blockPositions.get(0);
        Location location = new Location(player.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        Board board = game.getBoard(player);

        if(board == null
                || !IsBetween.isBetween2D(board.getCorner(), board.getWidth(), board.getHeight(), location.getBlock())
                || board.getCorner().getBlockY() != location.getBlockY()
                || !Game.isBlockInsideGameField(location.getBlock()))
            return;
        event.setCancelled(true);
        if(playerDigTypes.get(0) != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK
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
