package de.teddybear2004.retro.games.game.inventory.generator.game.manager;

import com.mojang.datafixers.util.Pair;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.inventory.generator.InventoryGenerator;
import de.teddybear2004.retro.games.util.HeadGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GameManagerGenerator extends InventoryGenerator {

    public GameManagerGenerator(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public @NotNull Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> insertConsumerItems(Inventory inventory, InventoryManager inventoryManager) {
        Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map = new HashMap<>();

        Pair<Integer, ItemStack> list = inventoryManager.insertItemId(new ItemStack(Material.CHEST));
        ItemMeta listMeta = list.getSecond().getItemMeta();
        if (listMeta != null) listMeta.setDisplayName(ChatColor.AQUA + "Game List");
        list.getSecond().setItemMeta(listMeta);

        inventory.setItem(11, list.getSecond());

        map.put(list.getFirst(), player
                -> (inventory1, clickType)
                -> player.openInventory(inventoryManager.getInventory(ListGameGenerator.class, player)));

        Pair<Integer, ItemStack> add = inventoryManager.insertItemId(HeadGenerator.getHeadFromUrl("https://textures.minecraft.net/texture/3edd20be93520949e6ce789dc4f43efaeb28c717ee6bfcbbe02780142f716"));
        ItemMeta addMeta = add.getSecond().getItemMeta();
        if (addMeta != null) addMeta.setDisplayName(ChatColor.AQUA + "Add Game");
        add.getSecond().setItemMeta(addMeta);

        inventory.setItem(15, add.getSecond());

        map.put(add.getFirst(), player
                -> ((inventory1, clickType)
                -> player.openInventory(inventoryManager.getInventory(AddGameGenerator.class, player))));

        return map;
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "Game Manager";
    }

}
