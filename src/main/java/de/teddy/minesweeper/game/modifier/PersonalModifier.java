package de.teddy.minesweeper.game.modifier;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.util.CustomPersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PersonalModifier {

    private final Map<ModifierType, Object> modifierTypeObjectMap = new HashMap<>();
    private final PersistentDataContainer container;


    private PersonalModifier(PersistentDataContainer container) {
        this.container = container;
    }

    public static PersonalModifier getPersonalModifier(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        PersonalModifier modifier = new PersonalModifier(container);

        for (ModifierType value : ModifierType.values())
            modifier.modifierTypeObjectMap.put(value, value.get(container));

        return modifier;
    }

    public void set(ModifierType type, Object value) {
        modifierTypeObjectMap.put(type, value);

        type.set(container, value);
    }

    @SuppressWarnings("unchecked")
    public <Z> Optional<Z> get(ModifierType type) {
        return (Optional<Z>) Optional.ofNullable(modifierTypeObjectMap.get(type));
    }


    public enum ModifierType {
        RESOURCE_PACK_URL(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "resource_pack_url"), PersistentDataType.STRING),
        DOUBLE_CLICK_DURATION(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "double_click_duration"), PersistentDataType.INTEGER),
        PAINTER_CLASS(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter_class"), PersistentDataType.STRING),
        ENABLE_QUESTION_MARK(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_question_mark"), CustomPersistentDataType.PERSISTENT_BOOLEAN),
        ENABLE_MARKS(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_marks"), CustomPersistentDataType.PERSISTENT_BOOLEAN),
        ENABLE_DOUBLE_CLICK(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_double_click"), CustomPersistentDataType.PERSISTENT_BOOLEAN),
        HIDE_PLAYER(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player"), CustomPersistentDataType.PERSISTENT_BOOLEAN),
        HIDE_PLAYER_DISTANCE(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player_distance"), PersistentDataType.DOUBLE),
        REVEAL_ON_DOUBLE_CLICK(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "reveal_on_double_click"), CustomPersistentDataType.PERSISTENT_BOOLEAN),
        USE_MULTI_FLAG(new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "use_multi_flag"), CustomPersistentDataType.PERSISTENT_BOOLEAN),
        ;

        private final NamespacedKey namespacedKey;
        private final PersistentDataType<?, ?> persistentDataType;

        ModifierType(NamespacedKey namespacedKey, PersistentDataType<?, ?> persistentDataType) {
            this.namespacedKey = namespacedKey;
            this.persistentDataType = persistentDataType;
        }

        @SuppressWarnings("unchecked")
        public <Z> Z get(PersistentDataContainer container) {
            if (container == null) return null;

            return container.get(namespacedKey, (PersistentDataType<?, Z>) persistentDataType);
        }

        @SuppressWarnings("unchecked")
        public <Z> void set(PersistentDataContainer container, Z value) {
            PersistentDataType<?, Z> persistentDataType1 = (PersistentDataType<?, Z>) persistentDataType;
            if (value != null)
                container.set(namespacedKey, persistentDataType1, value);
            else
                container.remove(namespacedKey);
        }
    }

}
