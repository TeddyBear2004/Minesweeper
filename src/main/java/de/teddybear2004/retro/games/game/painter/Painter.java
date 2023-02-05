package de.teddybear2004.retro.games.game.painter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.teddybear2004.retro.games.RetroGames;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Field;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.modifier.PersonalModifier;
import de.teddybear2004.retro.games.minesweeper.painter.MinesweeperPainter;
import de.teddybear2004.retro.games.scheduler.RemoveMarkerScheduler;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface Painter<F extends Field> {

    NamespacedKey PAINTER_KEY = new NamespacedKey(RetroGames.getPlugin(RetroGames.class), "painter_class");

    Class<? extends Painter<?>> DEFAULT_PAINTER = MinesweeperPainter.class;

    Map<Class<? extends Field>, Map<Class<? extends Painter<?>>, Painter<?>>> PAINTER_MAP = new HashMap<>();

    static void storePainterClass(@NotNull PersistentDataContainer container, @NotNull Class<? extends Painter<? extends Field>> clazz) {
        container.set(PAINTER_KEY, PersistentDataType.STRING, clazz.getName());
    }


    static Painter<?> getPainter(String className) {
        try{
            Class<?> aClass = Class.forName(className);
            for (Map<Class<? extends Painter<?>>, Painter<?>> value : PAINTER_MAP.values()) {
                for (Painter<?> painter : value.values()) {
                    if (painter.getClass().equals(aClass))
                        return painter;
                }
            }
        }catch(ClassNotFoundException e){
            return null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static <F extends Field> Painter<F> getPainter(String className, Class<? extends Painter<F>> painterClass, Class<F> fieldClass) {
        try{
            Class<? extends Painter<F>> aClass = (Class<? extends Painter<F>>) Class.forName(className).cast(Painter.class);
            return painterClass.cast(PAINTER_MAP.get(fieldClass).get(aClass));
        }catch(ClassNotFoundException e){
            return null;
        }
    }

    static <F extends Field> List<Painter<F>> getPainter(Class<? extends Painter<F>> painterClass, Class<F> fieldClass) {
        Collection<Painter<?>> values = PAINTER_MAP.get(fieldClass).values();
        List<Painter<F>> list = new ArrayList<>();

        values.forEach(painter -> {
            if (painterClass.isInstance(painter))
                list.add(painterClass.cast(painter));
        });

        return list;
    }

    static <F extends Field> Painter<F> getPainter(@NotNull Player player, Class<? extends Painter<F>> painterClass, Class<F> fieldClass) {
        Painter<?> painter = PAINTER_MAP.get(fieldClass).get(loadPainterClass(player));

        if (painterClass.isInstance(painter))
            return painterClass.cast(painter);

        return null;
    }

    @SuppressWarnings("unchecked")
    static <F extends Field> Class<? extends Painter<F>> loadPainterClass(@NotNull Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        Class<? extends Painter<F>> clazz;
        try{
            clazz = (Class<? extends Painter<F>>) Class.forName(personalModifier.get(PersonalModifier.ModifierType.PAINTER_CLASS));
        }catch(ClassCastException | ClassNotFoundException e){
            e.printStackTrace();
            clazz = (Class<? extends Painter<F>>) DEFAULT_PAINTER;
        }

        return clazz;
    }

    String getName();

    void drawBlancField(Board<F> board, List<Player> players);

    void drawField(Board<F> board, List<Player> players);

    List<PacketType> getRightClickPacketType();

    List<PacketType> getLeftClickPacketType();

    void onRightClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void onLeftClick(Player player, PacketEvent event, Game game, PacketContainer packet);

    void highlightFields(List<F> field, List<Player> players, RemoveMarkerScheduler removeMarkerScheduler);

}
