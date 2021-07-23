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
import de.teddy.minesweeper.game.Inventories;
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

        Game game = Game.getGame(player);

        if(game == null)
            return;

        BlockPosition blockPosition = packet.getMovingBlockPositions().read(0).getBlockPosition();
        Location location = blockPosition.toLocation(player.getWorld());

        if(!game.isBlockInsideGame(location.getBlock()))
            return;

        Board board = game.getBoard(player);

        if(board == null){
            Board watching = game.getGameWatched(player);

            if(watching != null){
                System.out.println(game);
                Board.Field field = watching.getField(location.getBlockX(), location.getBlockZ());
                Material[] materials = new Material[]{field.getActualMaterial(), field.getMark()};

                PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[location.getBlockY() - watching.getHeight()]));
                player.getInventory().setContents(Inventories.viewerInventory);
                event.setCancelled(true);
            }
            return;
        }

        if(board.isFinished())
            return;

        Board.Field field = board.getField(location.getBlockX(), location.getBlockZ());

        if(field == null)
            return;

        player.getInventory().setContents(Inventories.gameInventory);
        event.setCancelled(true);

        if(packet.getHands().read(0) == EnumWrappers.Hand.OFF_HAND)
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
