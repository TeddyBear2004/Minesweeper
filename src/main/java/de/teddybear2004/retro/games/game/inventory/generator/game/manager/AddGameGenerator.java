package de.teddybear2004.retro.games.game.inventory.generator.game.manager;

import com.mojang.datafixers.util.Pair;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.inventory.generator.InventoryGenerator;
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

public class AddGameGenerator extends InventoryGenerator {

    public AddGameGenerator(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public @NotNull Map<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> insertConsumerItems(Inventory inventory, InventoryManager manager) {
        HashMap<Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map = new HashMap<>();

        insertSave(inventory, manager, map);
        insertCorner(inventory, manager, map);
        insertSpawn(inventory, manager, map);
        insertLangPath(inventory, manager, map);
        insertBombCount(inventory, manager, map);

        return map;
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "Add Game";
    }

    private void insertSave(Inventory inventory, InventoryManager manager, HashMap<? super Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map) {
        Pair<Integer, ItemStack> save = manager.insertItemId(new ItemStack(Material.LIME_DYE));
        ItemMeta saveMeta = save.getSecond().getItemMeta();
        if (saveMeta != null) saveMeta.setDisplayName(ChatColor.AQUA + "Save");
        save.getSecond().setItemMeta(saveMeta);

        inventory.setItem(17, save.getSecond());

        map.put(save.getFirst(), player
                -> (inventory1, clickType)
                -> {
            //todo
        });
    }

    private void insertCorner(Inventory inventory, InventoryManager manager, HashMap<? super Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map) {
        Pair<Integer, ItemStack> save = manager.insertItemId(new ItemStack(Material.COMPASS));
        ItemMeta saveMeta = save.getSecond().getItemMeta();
        if (saveMeta != null) saveMeta.setDisplayName(ChatColor.AQUA + "Set corner!");
        save.getSecond().setItemMeta(saveMeta);

        inventory.setItem(9, save.getSecond());

        map.put(save.getFirst(), player
                -> (inventory1, clickType)
                -> {
            GameManager.Creator instance = GameManager.Creator.getInstance(player, manager);
            instance.setActive(GameManager.Creator.Type.CORNER);
            player.closeInventory();
            player.sendTitle("Set corner...", "", 1, 200, 1);
        });
    }

    private void insertSpawn(Inventory inventory, InventoryManager manager, HashMap<? super Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map) {
        Pair<Integer, ItemStack> save = manager.insertItemId(new ItemStack(Material.COMPASS));
        ItemMeta saveMeta = save.getSecond().getItemMeta();
        if (saveMeta != null) saveMeta.setDisplayName(ChatColor.AQUA + "Set spawn!");
        save.getSecond().setItemMeta(saveMeta);

        inventory.setItem(10, save.getSecond());

        map.put(save.getFirst(), player
                -> (inventory1, clickType)
                -> {
            GameManager.Creator instance = GameManager.Creator.getInstance(player, manager);
            instance.setActive(GameManager.Creator.Type.SPAWN);
            player.closeInventory();
            player.sendTitle("Set corner...", "", 1, 200, 1);
        });
    }

    private void insertLangPath(Inventory inventory, InventoryManager manager, HashMap<? super Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map) {
        Pair<Integer, ItemStack> save = manager.insertItemId(new ItemStack(Material.PAPER));
        ItemMeta saveMeta = save.getSecond().getItemMeta();
        if (saveMeta != null) saveMeta.setDisplayName(ChatColor.AQUA + "Set Language Path!");
        save.getSecond().setItemMeta(saveMeta);

        inventory.setItem(13, save.getSecond());

        map.put(save.getFirst(), player
                -> (inventory1, clickType)
                -> {
            GameManager.Creator instance = GameManager.Creator.getInstance(player, manager);
            instance.setActive(GameManager.Creator.Type.LANG_PATH);
            player.closeInventory();
            player.sendTitle("Set corner...", "", 1, 200, 1);

        });
    }

    private void insertBombCount(Inventory inventory, InventoryManager manager, HashMap<? super Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map) {
        Pair<Integer, ItemStack> save = manager.insertItemId(new ItemStack(Material.COMPASS));
        ItemMeta saveMeta = save.getSecond().getItemMeta();
        if (saveMeta != null) saveMeta.setDisplayName(ChatColor.AQUA + "Set Bomb count!");
        save.getSecond().setItemMeta(saveMeta);

        inventory.setItem(14, save.getSecond());

        map.put(save.getFirst(), player
                -> (inventory1, clickType)
                -> {
            GameManager.Creator instance = GameManager.Creator.getInstance(player, manager);
            instance.setActive(GameManager.Creator.Type.BOMB_COUNT);
            player.closeInventory();
            player.sendTitle("Set corner...", "", 1, 200, 1);

        });
    }

    private void insertSecondCorner(Inventory inventory, InventoryManager manager, HashMap<? super Integer, Function<Player, BiConsumer<Inventory, ClickType>>> map) {
        Pair<Integer, ItemStack> save = manager.insertItemId(new ItemStack(Material.COMPASS));
        ItemMeta saveMeta = save.getSecond().getItemMeta();
        if (saveMeta != null) saveMeta.setDisplayName(ChatColor.AQUA + "Set second corner!");
        save.getSecond().setItemMeta(saveMeta);

        inventory.setItem(12, save.getSecond());

        map.put(save.getFirst(), player
                -> (inventory1, clickType)
                -> {
            GameManager.Creator instance = GameManager.Creator.getInstance(player, manager);
            instance.setActive(GameManager.Creator.Type.OTHER_CORNER);
            player.closeInventory();
            player.sendTitle("Set corner...", "", 1, 200, 1);

        });
    }

}
