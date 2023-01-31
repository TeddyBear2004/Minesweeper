package de.teddybear2004.minesweeper.game.inventory.generator;

import com.mojang.datafixers.util.Pair;
import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SettingsGenerator extends InventoryGenerator {

    private final Language language;

    public SettingsGenerator(GameManager gameManager) {
        super(gameManager);
        this.language = Minesweeper.getPlugin(Minesweeper.class).getLanguage();
    }

    @Override
    public @NotNull Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> insertConsumerItems(Inventory inventory, InventoryManager manager) {
        Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map = new HashMap<>();

        AtomicInteger integer = new AtomicInteger(0);
        for (PersonalModifier.ModifierType value : PersonalModifier.ModifierType.values()) {
            int i = manager.getNextId();
            int slot = integer.getAndIncrement();

            map.put(i, player -> {
                Pair<Integer, ItemStack> integerItemStackPair = manager.insertItemId(value.get(player, language, value), i);
                inventory.setItem(slot, integerItemStackPair.getSecond());

                return (inventory1, clickType) ->
                        value.click(value, player, PersonalModifier.getPersonalModifier(player), inventory1, clickType, language, slot, manager, i);
            });
        }

        return map;
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "Settings";
    }

}
