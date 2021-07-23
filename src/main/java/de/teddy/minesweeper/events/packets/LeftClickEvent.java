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

        Game game = Game.getGame(player);

        if(game == null)
            return;

        BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
        Location location = blockPosition.toLocation(player.getWorld());

        if(!game.isBlockInsideGame(location.getBlock()))
            return;

        event.setCancelled(true);

        Board board = Game.getBoard(player);

        if(board == null){
            Board watching = Game.getGameWatched(player);

            if(watching != null){
                Board.Field field = watching.getField(location.getBlockX(), location.getBlockZ());
                Material[] materials = new Material[]{field.getActualMaterial(), field.getMark()};

                PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[game.getFieldHeight() - location.getBlockY()]));
            }
            return;
        }

        if(board.isFinished())
            return;

        Board.Field field = board.getField(location.getBlockX(), location.getBlockZ());

        EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().read(0);

        if(digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK){
            Material[] materials = new Material[]{field.getActualMaterial(), field.getMark()};
            PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[game.getFieldHeight() - location.getBlockY()]));
            return;
        }

        if(digType != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK)
            return;

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
        board.checkIfWon();

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
