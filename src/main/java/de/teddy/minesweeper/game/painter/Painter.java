package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Painter {

    void drawBlancField(Board board, List<Player> players);

    void drawField(Board board, List<Player> players);

    void drawBombs(Board board, List<Player> players);

    ItemStack getActualItemStack(Board.Field field);

    Material getActualMaterial(Board.Field field);

    PacketType getRightClickPacketType();
    PacketType getLeftClickPacketType();

    void onRightClick(Player player, PacketEvent event,Game game, PacketContainer packet);
    void onLeftClick(Player player, PacketEvent event, Game game, PacketContainer packet);
}
