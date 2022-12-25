package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public interface Painter {
    NamespacedKey PAINTER_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter_class");
    Class<? extends Painter> DEFAULT_PAINTER = ArmorStandPainter.class;

    static void storePainterClass(PersistentDataContainer container, Class<? extends Painter> clazz) {
        container.set(PAINTER_KEY, PersistentDataType.STRING, clazz.getName());
    }

    static Class<? extends Painter> loadPainterClass(PersistentDataContainer container) {
        Class<? extends Painter> clazz;
        try{
            String name = container.get(PAINTER_KEY, PersistentDataType.STRING);

            if(name != null)
                clazz = Class.forName(name).asSubclass(Painter.class);
            else
                clazz = DEFAULT_PAINTER;
        }catch(ClassNotFoundException | ClassCastException e){
            clazz = DEFAULT_PAINTER;
        }

        return clazz;
    }

    void drawBlancField(Board board, List<Player> players);

    void drawField(Board board, List<Player> players);

    void drawBombs(Board board, List<Player> players);

    ItemStack getActualItemStack(Board.Field field);

    Material getActualMaterial(Board.Field field);

    List<PacketType> getRightClickPacketType();

    List<PacketType> getLeftClickPacketType();

    void onRightClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void onLeftClick(Player player, PacketEvent event, Game game, PacketContainer packet);

}
