package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Field;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Painter {

    NamespacedKey PAINTER_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter_class");

    Class<? extends Painter> DEFAULT_PAINTER = BlockPainter.class;

    Map<Class<? extends Painter>, Painter> PAINTER_MAP = new HashMap<>();

    static void storePainterClass(PersistentDataContainer container, Class<? extends Painter> clazz) {
        container.set(PAINTER_KEY, PersistentDataType.STRING, clazz.getName());
    }

    @SuppressWarnings("unchecked")
    static Class<? extends Painter> loadPainterClass(Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        Class<? extends Painter> clazz;
        try{
            Optional<String> cl = personalModifier.get(PersonalModifier.ModifierType.PAINTER_CLASS);

            clazz = cl.isPresent() ? (Class<? extends Painter>) Class.forName(cl.get()) : DEFAULT_PAINTER;
        }catch(ClassCastException | ClassNotFoundException e){
            e.printStackTrace();
            clazz = DEFAULT_PAINTER;
        }

        return clazz;
    }

    static Painter getPainter(Player player) {
        return PAINTER_MAP.get(loadPainterClass(player));
    }

    String getName();

    void drawBlancField(Board board, List<Player> players);

    void drawField(Board board, List<Player> players);

    void drawBombs(Board board, List<Player> players);

    ItemStack getActualItemStack(Field field);

    Material getActualMaterial(Field field);

    List<PacketType> getRightClickPacketType();

    List<PacketType> getLeftClickPacketType();

    void onRightClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void onLeftClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void highlightField(Field field, List<Player> players);

}
