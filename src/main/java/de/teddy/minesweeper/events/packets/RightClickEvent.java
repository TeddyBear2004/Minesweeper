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
import de.teddy.minesweeper.game.Inventories;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RightClickEvent implements PacketListener {

    @Override
    public void onPacketSending(PacketEvent event) { }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PacketContainer packet = event.getPacket();

        if (player.getInventory().getItemInMainHand().getType() != Material.AIR)
            return;

        Game game = Game.getGame(player);
        Painter painter = Game.PAINTER_MAP.get(Game.PLAYER_PAINTER_MAP.get(player));
        if (game == null || painter == null)
            return;

        BlockPosition blockPosition;
        Location location;
        boolean useArmorStandPainter = false;

        if (packet.getType() == PacketType.Play.Client.USE_ENTITY) {
            WrappedEnumEntityUseAction read = event.getPacket().getEnumEntityUseActions().read(0);

            if (read.getAction() == EnumWrappers.EntityUseAction.ATTACK) {
                return;
            }

            Integer entityId = event.getPacket().getIntegers().read(0);

            if (!(painter instanceof ArmorStandPainter armorStandPainter)) return;

            useArmorStandPainter = true;
            Board cache = Game.getBoard(player);
            if (cache == null) return;
            location = armorStandPainter.getLocation(cache, entityId);
            if (location == null)
                return;

            blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        } else {
            blockPosition = packet.getMovingBlockPositions().read(0).getBlockPosition();
            location = blockPosition.toLocation(player.getWorld());
        }

        if (game.isBlockOutsideGame(location.getBlock()))
            return;

        Board board = Game.getBoard(player);

        if (board == null) {
            Board watching = Game.getBoardWatched(player);

            if (watching != null) {
                Board.Field field = watching.getField(location);
                if (field != null) {
                    Material[] materials = new Material[]{field.getActualMaterial(painter), field.getMark()};
                    PacketUtil.sendBlockChange(player, blockPosition, WrappedBlockData.createData(materials[location.getBlockY() - game.getFieldHeight()]));
                }
                player.getInventory().setContents(Inventories.viewerInventory);
                event.setCancelled(true);
            }
            return;
        }

        Board.Field field = board.getField(location);

        if (field == null)
            return;

        event.setCancelled(true);
        player.getInventory().setContents(Inventories.gameInventory);

        if (board.isFinished() || (!useArmorStandPainter && packet.getHands().read(0) == EnumWrappers.Hand.OFF_HAND))
            return;

        if (field.isCovered())
            field.reverseMark();

        board.draw();
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
                .types(PacketType.Play.Client.USE_ITEM, PacketType.Play.Client.USE_ENTITY)
                .high()
                .build();
    }

    @Override
    public Plugin getPlugin() {
        return Minesweeper.getPlugin();
    }

}
