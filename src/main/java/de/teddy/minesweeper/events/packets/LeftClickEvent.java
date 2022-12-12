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
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.exceptions.BombExplodeException;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.Painter;
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
    public void onPacketSending(PacketEvent event) { }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        Game game = Game.getGame(player);
        Painter painter = Game.PAINTER_MAP.get(Game.PLAYER_PAINTER_MAP.get(player));
        if (game == null || painter == null)
            return;

        Location location;
        BlockPosition blockPosition;
        boolean useArmorStandPainter = false;


        if (packet.getType() == PacketType.Play.Client.USE_ENTITY) {
            WrappedEnumEntityUseAction read = event.getPacket().getEnumEntityUseActions().read(0);

            if (read.getAction() != EnumWrappers.EntityUseAction.ATTACK) {
                return;
            }
            Integer entityId = event.getPacket().getIntegers().read(0);

            if (!(painter instanceof ArmorStandPainter armorStandPainter))
                return;

            useArmorStandPainter = true;
            Board cache = Game.getBoard(player);
            if (cache == null)
                return;
            location = armorStandPainter.getLocation(cache, entityId);
            if (location == null)
                return;

            blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        } else {
            blockPosition = packet.getBlockPositionModifier().read(0);
            location = blockPosition.toLocation(player.getWorld());
        }

        if (game.isBlockOutsideGame(location.getBlock()))
            return;

        Board board = Game.getBoard(player);

        if (board == null) {
            Board watching = Game.getBoardWatched(player);

            if (watching != null) {
                Board.Field field = watching.getField(location);
                if (field == null)
                    return;
                Material[] materials = new Material[]{field.getActualMaterial(painter), field.getMark()};

                if (game.getFieldHeight() - location.getBlockY() < 0)
                    return;

                PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[game.getFieldHeight() - location.getBlockY()]));
            }
            return;
        }

        Board.Field field = board.getField(location);


        if (!useArmorStandPainter && !board.isFinished()) {
            EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().read(0);
            if (field != null && digType == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
                if (location.getBlockY() - game.getFieldHeight() == 0) {
                    board.draw();
                    event.setCancelled(true);
                } else if (location.getBlockY() - game.getFieldHeight() == 1) {
                    PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(field.getMark()));
                }
            }

            if (digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK && field != null) {
                board.draw();
                return;
            }

            if (digType != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK)
                return;
        }


        if (board.isFinished())
            return;

        try{
            if (field == null) {
                try{
                    board.checkField(location.getBlockX(), location.getBlockZ());
                }catch(IllegalArgumentException ignore){
                }

                board.draw();
                return;
            }

            if (field.isMarked()) {
                if (location.getBlockY() - game.getFieldHeight() == 1)
                    PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(field.getMark()));

                return;
            }

            if (field.isCovered()) {
                board.checkField(location.getBlockX(), location.getBlockZ());
            } else if (System.currentTimeMillis() - lastClicked.getOrDefault(player, (long) -1000) <= 350) {
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
    public ListeningWhitelist getSendingWhitelist() {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return ListeningWhitelist
                .newBuilder()
                .gamePhase(GamePhase.PLAYING)
                .types(PacketType.Play.Client.BLOCK_DIG, PacketType.Play.Client.USE_ENTITY)
                .high()
                .build();
    }

    @Override
    public Plugin getPlugin() {
        return Minesweeper.getPlugin();
    }

}
