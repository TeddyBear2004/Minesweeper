package de.teddybear2004.minesweeper.game.modifier;

import de.teddy.minesweeper.game.painter.Painter;
import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.util.CustomPersistentDataType;
import de.teddybear2004.minesweeper.util.HeadGenerator;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

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
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                false),
        DOUBLE_CLICK_DURATION(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "double_click_duration"),
                PersistentDataType.INTEGER,
                "quick_reveal_duration",
                ModifierType::getInteger,
                ModifierType::handleIntegerClick,
                350),
        PAINTER_CLASS(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter_class"),
                PersistentDataType.STRING,
                "board_style",
                ModifierType::getPainter,
                ModifierType::handlePainterClick,
                Painter.DEFAULT_PAINTER.getName()),
        ENABLE_QUESTION_MARK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_question_mark"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "enable_question_mark",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                false),
        ENABLE_MARKS(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_marks"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "enable_flag",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                true),
        ENABLE_DOUBLE_CLICK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_double_click"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "quick_reveal",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                true),
        HIDE_PLAYER(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "hide_player",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                false),
        HIDE_PLAYER_DISTANCE(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player_distance"),
                PersistentDataType.DOUBLE,
                "hide_player_distance",
                ModifierType::getDouble,
                ModifierType::handleDoubleClick,
                5.0),
        REVEAL_ON_DOUBLE_CLICK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "reveal_on_double_click"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "reveal_on_double_click",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                false),
        USE_MULTI_FLAG(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "use_multi_flag"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "use_multi_flag",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                false),
        BREAK_FLAG(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "break_flags"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "break_flags",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                false),
        JUST_HIDE_WHILE_IN_GAME(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_while_in_game"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "hide_while_in_game",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                false),
        RESTART_ON_ITEM_SWAP(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "restart_on_item_swap"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "restart_on_item_swap",
                ModifierType::getBoolean,
                ModifierType::handleBooleanClick,
                true);
        private static final NamespacedKey BOOLEAN = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "boolean");
        private static final NamespacedKey PAINTER = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter");
        private static final NamespacedKey INTEGER = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "integer");
        private static final NamespacedKey DOUBLE = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "double");
        private static final DecimalFormat df = new DecimalFormat(".0");
        private final NamespacedKey namespacedKey;
        private final PersistentDataType<?, ?> persistentDataType;
        private final String langReference;
        private final Function<GetObject, ItemStack> getObjectConsumer;
        private final Consumer<ClickObject> clickObjectConsumer;
        private final Object defaultValue;

        ModifierType(NamespacedKey namespacedKey, PersistentDataType<?, ?> persistentDataType, String langReference, Function<GetObject, ItemStack> getObjectConsumer, Consumer<ClickObject> clickObjectConsumer, Object defaultValue) {
            this.namespacedKey = namespacedKey;
            this.persistentDataType = persistentDataType;
            this.langReference = langReference;
            this.getObjectConsumer = getObjectConsumer;
            this.clickObjectConsumer = clickObjectConsumer;
            this.defaultValue = defaultValue;
        }

        private static void handlePainterClick(@NotNull ClickObject clickObject) {
            ItemStack item = clickObject.inventory.getItem(clickObject.clickedSlot);
            if (item == null)
                return;

            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta == null)
                return;

            String s = itemMeta.getPersistentDataContainer().get(PAINTER, PersistentDataType.STRING);
            if (s == null)
                return;

            Painter painter = Painter.getPainter(s);
            Board board = Minesweeper.getPlugin(Minesweeper.class).getGameManager().getBoardWatched(clickObject.player());
            List<Player> players = Collections.singletonList(clickObject.player);
            if (painter != null && board != null)
                painter.drawBlancField(board, players);

            Painter newPainter = null;
            boolean returnOnNext = false;

            Painter first = null;
            for (Painter painter1 : Painter.getPainter()) {
                if (first == null)
                    first = painter1;

                if (painter == null || returnOnNext) {
                    newPainter = painter1;
                    break;
                }

                if (painter.equals(painter1))
                    returnOnNext = true;
            }
            if (newPainter == null)
                newPainter = first;

            if (newPainter != null) {
                clickObject.modifier.set(clickObject.type(), newPainter.getClass().getName());
                if (board != null)
                    newPainter.drawField(board, players);
            }

            clickObject.inventory.setItem(clickObject.clickedSlot,
                                          clickObject.manager.insertItemId(getPainter(clickObject.player(), clickObject.language(), clickObject.type()), clickObject.itemId).getSecond());
        }

        public static ItemStack getPainter(Player player, Language language, ModifierType type) {
            String s = player == null ? null : type.get(player.getPersistentDataContainer());

            s = s == null ? (String) type.defaultValue : s;

            ItemStack itemStack = new ItemStack(Material.LIME_TERRACOTTA);

            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                Painter painter = Painter.getPainter(s);
                if (painter == null)
                    return itemStack;

                String string = language.getString(type.langReference);

                itemMeta.setDisplayName(ChatColor.GREEN + string + ": " + painter.getName());

                itemMeta.getPersistentDataContainer().set(PAINTER, PersistentDataType.STRING, s);
            }

            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @SuppressWarnings("unchecked")
        public <Z> Z get(@Nullable PersistentDataContainer container) {
            if (container == null) return null;

            return container.get(namespacedKey, (PersistentDataType<?, Z>) persistentDataType);
        }

        public static ItemStack getPainter(GetObject getObject) {
            return getPainter(getObject.player, getObject.language, getObject.type);
        }

        private static void handleIntegerClick(@NotNull ClickObject clickObject) {
            ItemStack item = clickObject.inventory.getItem(clickObject.clickedSlot);
            if (item == null)
                return;

            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta == null)
                return;

            Integer integer = itemMeta.getPersistentDataContainer().get(INTEGER, PersistentDataType.INTEGER);
            if (integer == null)
                integer = (Integer) clickObject.type.defaultValue;

            integer += clickObject.clickType.isLeftClick() ? 1 : clickObject.clickType.isRightClick() ? -1 : 0;

            clickObject.modifier.set(clickObject.type(), integer);

            clickObject.inventory.setItem(clickObject.clickedSlot,
                                          clickObject.manager.insertItemId(getInteger(clickObject.player(), clickObject.language(), clickObject.type()), clickObject.itemId).getSecond());
        }

        public static ItemStack getInteger(Player player, Language language, ModifierType type) {
            Integer integer = player == null ? null : type.get(player.getPersistentDataContainer());

            integer = integer == null ? (Integer) type.defaultValue : integer;

            ItemStack itemStack = new ItemStack(HeadGenerator.getHeadFromUrl("https://textures.minecraft.net/texture/33cd934f11f0766f5410eba9e7b5f0ceb66f6b317e845cb6a501f37258556a43"));

            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                String string = language.getString(type.langReference);

                itemMeta.setDisplayName(ChatColor.GREEN + string + ": " + integer);
                itemMeta.setLore(List.of(
                        ChatColor.GRAY + "Press left click to" + ChatColor.GREEN + " increment " + ChatColor.GRAY + "by one.",
                        ChatColor.GRAY + "Press right click to" + ChatColor.RED + " decrement " + ChatColor.GRAY + "by one."
                ));

                itemMeta.getPersistentDataContainer().set(INTEGER, PersistentDataType.INTEGER, integer);
            }

            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        public static ItemStack getInteger(GetObject getObject) {
            return getInteger(getObject.player, getObject.language, getObject.type);
        }

        private static void handleDoubleClick(@NotNull ClickObject clickObject) {
            ItemStack item = clickObject.inventory.getItem(clickObject.clickedSlot);
            if (item == null)
                return;

            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta == null)
                return;

            Double d = itemMeta.getPersistentDataContainer().get(DOUBLE, PersistentDataType.DOUBLE);
            if (d == null)
                d = (Double) clickObject.type.defaultValue;

            d += clickObject.clickType.isLeftClick() ? 0.1 : clickObject.clickType.isRightClick() ? -0.1 : 0;

            clickObject.modifier.set(clickObject.type(), d);

            clickObject.inventory.setItem(clickObject.clickedSlot,
                                          clickObject.manager.insertItemId(getDouble(clickObject.player(), clickObject.language(), clickObject.type()), clickObject.itemId).getSecond());
        }

        public static ItemStack getDouble(Player player, Language language, ModifierType type) {
            Double d = player == null ? null : type.get(player.getPersistentDataContainer());

            d = d == null ? (Double) type.defaultValue : d;

            ItemStack itemStack = new ItemStack(HeadGenerator.getHeadFromUrl("https://textures.minecraft.net/texture/33cd934f11f0766f5410eba9e7b5f0ceb66f6b317e845cb6a501f37258556a43"));

            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                String string = language.getString(type.langReference);

                itemMeta.setDisplayName(ChatColor.GREEN + string + ": " + df.format(d));
                itemMeta.setLore(List.of(
                        ChatColor.GRAY + "Press left click to" + ChatColor.GREEN + " increment " + ChatColor.GRAY + "by 0,1.",
                        ChatColor.GRAY + "Press right click to" + ChatColor.RED + " decrement " + ChatColor.GRAY + "by 0,1."
                ));

                itemMeta.getPersistentDataContainer().set(DOUBLE, PersistentDataType.DOUBLE, d);
            }

            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        public static ItemStack getDouble(GetObject getObject) {
            return getDouble(getObject.player, getObject.language, getObject.type);
        }

        private static void handleBooleanClick(@NotNull ClickObject clickObject) {
            ItemStack item = clickObject.inventory.getItem(clickObject.clickedSlot);
            if (item == null)
                return;

            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta == null)
                return;

            Boolean aBoolean = itemMeta.getPersistentDataContainer().get(BOOLEAN, CustomPersistentDataType.PERSISTENT_BOOLEAN);
            if (aBoolean == null)
                aBoolean = (Boolean) clickObject.type.defaultValue;

            clickObject.modifier.set(clickObject.type(), !aBoolean);

            clickObject.inventory.setItem(clickObject.clickedSlot,
                                          clickObject.manager.insertItemId(getBoolean(clickObject.player(), clickObject.language(), clickObject.type()), clickObject.itemId).getSecond());
        }

        public static ItemStack getBoolean(Player player, Language language, ModifierType type) {
            Boolean bool = player == null ? null : type.get(player.getPersistentDataContainer());

            bool = bool == null ? (Boolean) type.defaultValue : bool;

            ItemStack itemStack = new ItemStack(bool ? Material.LIME_CONCRETE : Material.RED_CONCRETE);

            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                String string = language.getString(type.langReference);

                itemMeta.setDisplayName(
                        bool ? ChatColor.GREEN + string + ": " + language.getString("enabled")
                                : ChatColor.RED + string + ": " + language.getString("disabled"));

                itemMeta.getPersistentDataContainer().set(BOOLEAN, CustomPersistentDataType.PERSISTENT_BOOLEAN, bool);
            }

            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        public static ItemStack getBoolean(GetObject getObject) {
            return getBoolean(getObject.player, getObject.language, getObject.type);
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
            this.clickObjectConsumer.accept(new ClickObject(type, player, modifier, inventory, clickType, language, clickedSlot, manager, itemId));
        }

        public ItemStack get(Player player, Language language, ModifierType type) {
            return this.getObjectConsumer.apply(new GetObject(player, language, type));
        }
    }

    private record ClickObject(ModifierType type, Player player, PersonalModifier modifier, Inventory inventory,
                               ClickType clickType, Language language, int clickedSlot, InventoryManager manager,
                               int itemId) {
    }

    private record GetObject(Player player, Language language, ModifierType type) {
    }

}
