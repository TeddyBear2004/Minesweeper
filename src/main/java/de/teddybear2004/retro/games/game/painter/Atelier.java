package de.teddybear2004.retro.games.game.painter;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Field;
import de.teddybear2004.retro.games.game.modifier.PersonalModifier;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("rawtypes")
public class Atelier {

    private static final Class<? extends Painter> DEFAULT_PAINTER = ArmorStandPainter.class;

    private final Map<PainterField, Painter<? extends Field>> painterMap
            = new HashMap<>();

    public static Class<? extends Painter> getPainterClass(String painterClassName) {
        try{
            return Class.forName(painterClassName).asSubclass(Painter.class);
        }catch(ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Class<? extends Painter> getDefault() {
        return DEFAULT_PAINTER;
    }

    public Collection<? extends Painter> getPainters() {
        return painterMap.values();
    }

    public void register(Class<? extends Board> boardClass, Class<? extends Painter> painterClass, Painter<?> painter) {
        PainterField fPainterField = new PainterField(boardClass, painterClass);
        painterMap.put(fPainterField, painter);
    }

    public void save(Player player, Class<? extends Painter> painterClassName) {
        PersonalModifier.getPersonalModifier(player).set(PersonalModifier.ModifierType.PAINTER_CLASS, painterClassName.getName());
    }


    public Painter getPainter(Player player, Class<? extends Board> boardClass) {
        return painterMap.get(new PainterField(boardClass, getPainterClass(player)));
    }

    public Class<? extends Painter> getPainterClass(Player player) {
        String painterClassName = PersonalModifier.getPersonalModifier(player).get(PersonalModifier.ModifierType.PAINTER_CLASS);
        try{
            return Class.forName(painterClassName).asSubclass(Painter.class);
        }catch(ClassNotFoundException e){
            e.printStackTrace();
            return DEFAULT_PAINTER;
        }
    }

    public Painter getPainter(Class<? extends Painter> painterClass, Class<? extends Board> boardClass) {
        return painterMap.get(new PainterField(boardClass, painterClass));
    }

    public Painter getPainter(Class<? extends Painter> painterClass) {
        for (Map.Entry<PainterField, Painter<? extends Field>> entry : painterMap.entrySet()) {
            PainterField key = entry.getKey();
            Painter<? extends Field> painter = entry.getValue();

            if (key.painterClass.equals(painterClass)) {
                return painter;
            }
        }
        return null;
    }

    public Set<Class<? extends Painter>> getList() {
        Set<Class<? extends Painter>> painters = new HashSet<>();
        this.painterMap.forEach((painterField, painter) -> painters.add(painterField.painterClass));

        return painters;
    }

    private record PainterField(Class<? extends Board> boardClass,
                                Class<? extends Painter> painterClass) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PainterField that = (PainterField) o;

            if (!Objects.equals(boardClass, that.boardClass)) return false;
            return Objects.equals(painterClass, that.painterClass);
        }

        @Override
        public int hashCode() {
            int result = boardClass != null ? boardClass.hashCode() : 0;
            result = 31 * result + (painterClass != null ? painterClass.hashCode() : 0);
            return result;
        }

    }

}
