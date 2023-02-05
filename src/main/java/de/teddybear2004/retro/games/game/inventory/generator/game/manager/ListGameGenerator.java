package de.teddybear2004.retro.games.game.inventory.generator.game.manager;

import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.inventory.generator.InventoryGenerator;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ListGameGenerator extends InventoryGenerator {

    public ListGameGenerator(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public @NotNull Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> insertConsumerItems(Inventory inventory, InventoryManager manager) {
        return new HashMap<>();
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "Game List";
    }

}
