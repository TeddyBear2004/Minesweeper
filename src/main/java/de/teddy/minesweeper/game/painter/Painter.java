package de.teddy.minesweeper.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.Field;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Painter {

    NamespacedKey PAINTER_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter_class");

    Class<? extends Painter> DEFAULT_PAINTER = BlockPainter.class;

    Map<Class<? extends Painter>, Painter> PAINTER_MAP = new HashMap<>();

    static void storePainterClass(@NotNull PersistentDataContainer container, @NotNull Class<? extends Painter> clazz) {
        container.set(PAINTER_KEY, PersistentDataType.STRING, clazz.getName());
    }

    static Painter getPainter(String className) {
        try{
            return getPainter(Class.forName(className).asSubclass(Painter.class));
        }catch(ClassNotFoundException e){
            return null;
        }
    }

    static Painter getPainter(Class<? extends Painter> clazz) {
        return PAINTER_MAP.get(clazz);
    }

    static Collection<Painter> getPainter() {
        return PAINTER_MAP.values();
    }

    static Painter getPainter(@NotNull Player player) {
        return PAINTER_MAP.get(loadPainterClass(player));
    }

    static Class<? extends Painter> loadPainterClass(@NotNull Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        Class<? extends Painter> clazz;
        try{
            clazz = Class.forName(personalModifier.get(PersonalModifier.ModifierType.PAINTER_CLASS)).asSubclass(Painter.class);
        }catch(ClassCastException | ClassNotFoundException e){
            e.printStackTrace();
            clazz = DEFAULT_PAINTER;
        }

        return clazz;
    }

    String getName();

    void drawBlancField(Board board, List<Player> players);

    void drawField(Board board, List<Player> players);

    void drawBombs(Board board, List<Player> players);

    List<PacketType> getRightClickPacketType();

    List<PacketType> getLeftClickPacketType();

    void onRightClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void onLeftClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void highlightField(Field field, List<Player> players);

}
