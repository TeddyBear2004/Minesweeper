package de.teddybear2004.minesweeper.game.modifier;

import de.teddy.minesweeper.game.painter.Painter;
import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.texture.pack.ResourcePackHandler;
import de.teddybear2004.minesweeper.util.CustomPersistentDataType;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public <Z> @NotNull Optional<Z> get(ModifierType type) {
        return (Optional<Z>) Optional.ofNullable(modifierTypeObjectMap.get(type));
    }


    public enum ModifierType {
        RESOURCE_PACK_URL(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "resource_pack_url"),
                PersistentDataType.STRING,
                "custom_resource_pack_url",
                ModifierType::handleResourcePackLinkInput,
                ModifierType::handleTabJustDefault),
        DOUBLE_CLICK_DURATION(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "double_click_duration"),
                PersistentDataType.INTEGER,
                "quick_reveal_duration",
                ModifierType::handleIntegerInput,
                ModifierType::handleTabJustDefault),
        PAINTER_CLASS(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter_class"),
                PersistentDataType.STRING,
                "board_style",
                ModifierType::handlePainterInput,
                ModifierType::handleTabPainter),
        ENABLE_QUESTION_MARK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_question_mark"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "enable_question_mark",
                commandObject1 -> handleBooleanInput(commandObject1, false),
                ModifierType::handleTabBoolean),
        ENABLE_MARKS(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_marks"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "enable_flag",
                commandObject1 -> handleBooleanInput(commandObject1, true),
                ModifierType::handleTabBoolean),
        ENABLE_DOUBLE_CLICK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "enable_double_click"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "quick_reveal",
                commandObject1 -> handleBooleanInput(commandObject1, false),
                ModifierType::handleTabBoolean),
        HIDE_PLAYER(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "hide_player",
                commandObject1 -> handleBooleanInput(commandObject1, false),
                ModifierType::handleTabBoolean),
        HIDE_PLAYER_DISTANCE(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "hide_player_distance"),
                PersistentDataType.DOUBLE,
                "hide_player_distance",
                ModifierType::handleDoubleInput,
                ModifierType::handleTabJustDefault),
        REVEAL_ON_DOUBLE_CLICK(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "reveal_on_double_click"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "reveal_on_double_click",
                commandObject1 -> handleBooleanInput(commandObject1, true),
                ModifierType::handleTabBoolean),
        USE_MULTI_FLAG(
                new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "use_multi_flag"),
                CustomPersistentDataType.PERSISTENT_BOOLEAN,
                "use_multi_flag",
                commandObject1 -> handleBooleanInput(commandObject1, false),
                ModifierType::handleTabBoolean);

        private static List<String> langReferences;
        private final NamespacedKey namespacedKey;
        private final PersistentDataType<?, ?> persistentDataType;
        private final String langReference;
        private final Consumer<CommandObject> commandObject;
        private final Consumer<TabObject> tabConsumer;

        ModifierType(NamespacedKey namespacedKey, PersistentDataType<?, ?> persistentDataType, String langReference, Consumer<CommandObject> commandObject, Consumer<TabObject> tabConsumer) {
            this.namespacedKey = namespacedKey;
            this.persistentDataType = persistentDataType;
            this.langReference = langReference;
            this.commandObject = commandObject;
            this.tabConsumer = tabConsumer;
        }

        public static List<String> getLangReferences() {
            if (langReferences != null)
                return langReferences;

            langReferences = new ArrayList<>(values().length);

            for (ModifierType value : values()) {
                langReferences.add(value.getLangReference());
            }

            return langReferences;
        }

        private static void handleTabJustDefault(@NotNull TabObject tabObject) {
            if (tabObject.language.getString("default").startsWith(tabObject.key.toLowerCase()))
                tabObject.strings.add(tabObject.language.getString("default"));
        }

        private static void handleIntegerInput(@NotNull CommandObject commandObject) {
            handleNumberInput(commandObject, Integer::parseInt);
        }

        private static <T> void handleNumberInput(@NotNull CommandObject commandObject, @NotNull Function<String, T> converter) {
            if (commandObject.arg == null) {
                commandObject.player.sendMessage(commandObject.language.getString("send_current_setting_number",
                                                                                  commandObject.language.getString(commandObject.type.getLangReference()),
                                                                                  commandObject.modifier.get(commandObject.type).isPresent()
                                                                                          ? commandObject.modifier.get(commandObject.type).get().toString()
                                                                                          : commandObject.language.getString("default_setting")
                ) + ".");
                return;
            }
            if (commandObject.arg.equalsIgnoreCase("default")) {
                commandObject.modifier.set(commandObject.type, null);
                return;
            }
            try{
                T t = converter.apply(commandObject.arg);

                commandObject.modifier.set(commandObject.type, t);
                commandObject.player.sendMessage(ChatColor.GREEN + commandObject.language.getString("send_was_applied",
                                                                                                    commandObject.language.getString(commandObject.type.getLangReference())));
            }catch(NumberFormatException e){
                commandObject.player.sendMessage(ChatColor.DARK_RED + commandObject.language.getString("error_no_valid_number"));
            }
        }

        private static void handleDoubleInput(@NotNull CommandObject commandObject) {
            handleNumberInput(commandObject, Double::parseDouble);
        }

        private static void handleTabBoolean(@NotNull TabObject tabObject) {
            if (tabObject.language.getString("true_").toLowerCase().startsWith(tabObject.key.toLowerCase()))
                tabObject.strings.add(tabObject.language.getString("true_"));
            if (tabObject.language.getString("false_").toLowerCase().startsWith(tabObject.key.toLowerCase()))
                tabObject.strings.add(tabObject.language.getString("false_"));
            if (tabObject.language.getString("default").toLowerCase().startsWith(tabObject.key.toLowerCase()))
                tabObject.strings.add(tabObject.language.getString("default"));
        }

        private static void handleBooleanInput(@NotNull CommandObject commandObject, boolean defaultValue) {
            if (commandObject.arg == null) {
                commandObject.player.sendMessage(
                        ChatColor.GREEN + commandObject.language.getString("send_current_setting_boolean",
                                                                           commandObject.language.getString(commandObject.type.getLangReference()),
                                                                           commandObject.modifier.get(commandObject.type).isPresent()
                                                                                   ? commandObject.modifier.<Boolean>get(commandObject.type).orElse(defaultValue)
                                                                                   ? commandObject.language.getString("enabled") : commandObject.language.getString("disabled")
                                                                                   : commandObject.language.getString("default_setting")));
                return;
            }
            if (commandObject.arg.equalsIgnoreCase(commandObject.language.getString("default"))) {
                commandObject.modifier.set(commandObject.type, null);
                return;
            }
            if (commandObject.arg.equalsIgnoreCase(commandObject.language.getString("true_"))) {
                commandObject.modifier.set(commandObject.type, true);
                commandObject.player.sendMessage(ChatColor.GREEN + commandObject.language.getString("send_change_enable", commandObject.language.getString(commandObject.type.getLangReference())));
                return;
            }
            if (commandObject.arg.equalsIgnoreCase(commandObject.language.getString("false_"))) {
                commandObject.modifier.set(commandObject.type, false);
                commandObject.player.sendMessage(ChatColor.GREEN + commandObject.language.getString("send_change_disable", commandObject.language.getString(commandObject.type.getLangReference())));
                return;
            }
            commandObject.player.sendMessage(ChatColor.DARK_RED + commandObject.language.getString("error_no_true_or_false"));
        }

        private static void handleTabPainter(@NotNull TabObject tabObject) {
            Painter.PAINTER_MAP.values().forEach(painter -> {
                if (painter.getName().toLowerCase().startsWith(tabObject.key.toLowerCase()))
                    tabObject.strings.add(painter.getName());

            });
            if (tabObject.language.getString("default").startsWith(tabObject.key.toLowerCase()))
                tabObject.strings.add(tabObject.language.getString("default"));
        }

        private static void handlePainterInput(@NotNull CommandObject commandObject) {
            if (commandObject.arg == null) {
                Optional<String> painterClass = commandObject.modifier.get(PersonalModifier.ModifierType.PAINTER_CLASS);
                if (painterClass.isPresent()) {
                    try{
                        Painter painter = Painter.PAINTER_MAP.get(Class.forName(painterClass.get()));

                        commandObject.player.sendMessage(ChatColor.GREEN + commandObject.language.getString("send_current_board_style", painter.getName()));
                    }catch(ClassNotFoundException e){
                        commandObject.player.sendMessage(ChatColor.DARK_RED + commandObject.language.getString("send_unknown_error"));
                    }
                } else
                    commandObject.player.sendMessage(ChatColor.GREEN + commandObject.language.getString("send_current_board_style", painterClass.orElse("default")));
                return;
            }
            if (commandObject.arg.equalsIgnoreCase(commandObject.language.getString("default"))) {
                GameManager gameManager = Minesweeper.getPlugin(Minesweeper.class).getGameManager();
                Board board = gameManager.getBoard(commandObject.player);
                if (board == null) {
                    board = gameManager.getBoardWatched(commandObject.player);
                }

                if (board != null) {
                    Painter painter = Painter.PAINTER_MAP.get(Painter.loadPainterClass(commandObject.player));
                    painter.drawBlancField(board, Collections.singletonList(commandObject.player));
                }


                commandObject.modifier.set(PersonalModifier.ModifierType.PAINTER_CLASS, null);
                Painter.storePainterClass(commandObject.player.getPersistentDataContainer(), Painter.DEFAULT_PAINTER);


                if (board != null)
                    Painter.getPainter(commandObject.player).drawField(board, Collections.singletonList(commandObject.player));

                return;
            }
            for (Painter painter : Painter.PAINTER_MAP.values()) {
                if (painter.getName().equalsIgnoreCase(commandObject.arg)) {
                    commandObject.player.sendMessage(ChatColor.GREEN + commandObject.language.getString("send_board_style_applied"));

                    GameManager gameManager = Minesweeper.getPlugin(Minesweeper.class).getGameManager();
                    Board board = gameManager.getBoard(commandObject.player);
                    if (board == null) {
                        board = gameManager.getBoardWatched(commandObject.player);
                    }

                    if (board != null) {
                        Painter painter2 = Painter.PAINTER_MAP.get(Painter.loadPainterClass(commandObject.player));
                        painter2.drawBlancField(board, Collections.singletonList(commandObject.player));
                    }


                    commandObject.modifier.set(PersonalModifier.ModifierType.PAINTER_CLASS, painter.getClass().getName());
                    Painter.storePainterClass(commandObject.player.getPersistentDataContainer(), painter.getClass());


                    if (board != null)
                        painter.drawField(board, Collections.singletonList(commandObject.player));

                    return;
                }
            }
            commandObject.player.sendMessage(ChatColor.DARK_RED + commandObject.language.getString("error_no_valid_number"));
        }

        private static void handleResourcePackLinkInput(@NotNull CommandObject commandObject) {
            if (commandObject.arg == null) {
                commandObject.player.sendMessage(ChatColor.GREEN + commandObject.language.getString("send_current_resource_pack_url", commandObject.modifier.<String>get(PersonalModifier.ModifierType.RESOURCE_PACK_URL).orElse("default")));
                return;
            }
            if (commandObject.arg.equalsIgnoreCase(commandObject.language.getString("default"))) {
                commandObject.modifier.set(PersonalModifier.ModifierType.RESOURCE_PACK_URL, null);
                return;
            }
            String url = commandObject.arg;
            if (!(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))) {
                commandObject.player.sendMessage(ChatColor.DARK_RED + commandObject.language.getString("send_start_with_http"));
                return;
            }
            commandObject.modifier.set(PersonalModifier.ModifierType.RESOURCE_PACK_URL, url);
            commandObject.player.sendMessage(ChatColor.GREEN + commandObject.language.getString("send_resource_pack_applied"));
            commandObject.packHandler.apply(commandObject.player);
        }

        @SuppressWarnings("unchecked")
        public <Z> Z get(@Nullable PersistentDataContainer container) {
            if (container == null) return null;

            return container.get(namespacedKey, (PersistentDataType<?, Z>) persistentDataType);
        }

        @SuppressWarnings("unchecked")
        public <Z> void set(@NotNull PersistentDataContainer container, @Nullable Z value) {
            PersistentDataType<?, Z> persistentDataType1 = (PersistentDataType<?, Z>) persistentDataType;
            if (value != null)
                container.set(namespacedKey, persistentDataType1, value);
            else
                container.remove(namespacedKey);
        }

        public void fillList(String arg, List<String> strings, Language language) {
            this.tabConsumer.accept(new TabObject(arg, strings, language));
        }

        public void performAction(ModifierType type, Player player, PersonalModifier modifier, String arg,
                                  Language language, ResourcePackHandler packHandler) {
            this.commandObject.accept(new CommandObject(type, player, modifier, arg, language, packHandler));
        }

        public String getLangReference() {
            return langReference;
        }
    }

    private record TabObject(String key, List<String> strings, Language language) {
    }

    private record CommandObject(ModifierType type, Player player, PersonalModifier modifier, String arg,
                                 Language language, ResourcePackHandler packHandler) {
    }

}
