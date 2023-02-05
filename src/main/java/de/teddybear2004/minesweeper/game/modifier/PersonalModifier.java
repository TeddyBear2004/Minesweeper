package de.teddybear2004.minesweeper.game.modifier;

import de.teddy.minesweeper.game.painter.Painter;
import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.util.CustomPersistentDataType;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PersonalModifier {

    private final Map<ModifierType, Object> modifierTypeObjectMap = new HashMap<>();
    private final PersistentDataContainer container;


    private PersonalModifier(PersistentDataContainer container) {
        this.container = container;
    }

    public static @NotNull PersonalModifier getPersonalModifier(@NotNull Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        PersonalModifier modifier = new PersonalModifier(container);

        for (ModifierType value : ModifierType.values())
            modifier.modifierTypeObjectMap.put(value, value.get(container));

        return modifier;
    }

    public void set(@NotNull ModifierType type, Object value) {
        modifierTypeObjectMap.put(type, value);

        type.set(container, value);
    }

    @SuppressWarnings("unchecked")
    public <Z> @NotNull Z get(ModifierType type) {
        return (Z) Optional.ofNullable(modifierTypeObjectMap.get(type)).orElse(type.defaultValue);
    }


    public enum ModifierType {
        RESOURCE_PACK_URL(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "resource_pack_url"),
                PersistentDataType.STRING,
                "custom_resource_pack_url",
                new BooleanModifierWrapper(),
                false),
        DOUBLE_CLICK_DURATION(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "double_click_duration"),
                PersistentDataType.INTEGER,
                "quick_reveal_duration",
                new IntegerModifierWrapper(),
                350),
        PAINTER_CLASS(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter_class"),
                PersistentDataType.STRING,
                "board_style",
                new PainterModifierWrapper(),
                Painter.DEFAULT_PAINTER.getName()),
        ENABLE_QUESTION_MARK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_question_mark"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "enable_question_mark",
                new BooleanModifierWrapper(),
                false),
        ENABLE_MARKS(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_marks"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "enable_flag",
                new BooleanModifierWrapper(),
                true),
        ENABLE_DOUBLE_CLICK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_double_click"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "quick_reveal",
                new BooleanModifierWrapper(),
                true),
        HIDE_PLAYER(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "hide_player",
                new BooleanModifierWrapper(),
                false),
        HIDE_PLAYER_DISTANCE(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player_distance"),
                PersistentDataType.DOUBLE,
                "hide_player_distance",
                new DoubleModifierWrapper(),
                5.0),
        REVEAL_ON_DOUBLE_CLICK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "reveal_on_double_click"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "reveal_on_double_click",
                new BooleanModifierWrapper(),
                false),
        USE_MULTI_FLAG(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "use_multi_flag"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "use_multi_flag",
                new BooleanModifierWrapper(),
                false),
        BREAK_FLAG(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "break_flags"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "break_flags",
                new BooleanModifierWrapper(),
                false),
        JUST_HIDE_WHILE_IN_GAME(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_while_in_game"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "hide_while_in_game",
                new BooleanModifierWrapper(),
                false),
        RESTART_ON_ITEM_SWAP(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "restart_on_item_swap"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "restart_on_item_swap",
                new BooleanModifierWrapper(),
                true);
        private final NamespacedKey namespacedKey;
        private final PersistentDataType<?, ?> persistentDataType;
        private final String langReference;
        private final ModifierWrapper wrapper;
        private final Object defaultValue;

        ModifierType(NamespacedKey namespacedKey, PersistentDataType<?, ?> persistentDataType, String langReference, ModifierWrapper wrapper, Object defaultValue) {
            this.namespacedKey = namespacedKey;
            this.persistentDataType = persistentDataType;
            this.langReference = langReference;
            this.wrapper = wrapper;
            this.defaultValue = defaultValue;
        }

        @SuppressWarnings("unchecked")
        public <Z> Z get(@Nullable PersistentDataContainer container) {
            if (container == null) return null;

            return container.get(namespacedKey, (PersistentDataType<?, Z>) persistentDataType);
        }


        public String getLangReference() {
            return langReference;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        @SuppressWarnings("unchecked")
        public <Z> void set(@NotNull PersistentDataContainer container, @Nullable Z value) {
            PersistentDataType<?, Z> persistentDataType1 = (PersistentDataType<?, Z>) persistentDataType;
            if (value != null)
                container.set(namespacedKey, persistentDataType1, value);
            else
                container.remove(namespacedKey);
        }

        public void click(ModifierType type, Player player, PersonalModifier modifier, Inventory inventory,
                          ClickType clickType, Language language, int clickedSlot, InventoryManager manager, int itemId) {
            this.wrapper.handleClick(type, player, modifier, inventory, clickType, language, clickedSlot, manager, itemId);
        }

        public ItemStack get(Player player, Language language, ModifierType type) {
            return this.wrapper.get(player, language, type);
        }
    }
}
