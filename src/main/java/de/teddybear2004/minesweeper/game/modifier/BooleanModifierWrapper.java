package de.teddybear2004.minesweeper.game.modifier;

import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.util.CustomPersistentDataType;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BooleanModifierWrapper implements ModifierWrapper {

    private static final NamespacedKey BOOLEAN = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "boolean");

    @Override
    public ItemStack get(Player player, Language language, PersonalModifier.ModifierType type) {
        Boolean bool = player == null ? null : type.get(player.getPersistentDataContainer());

        bool = bool == null ? (Boolean) type.getDefaultValue() : bool;

        ItemStack itemStack = new ItemStack(bool ? Material.LIME_CONCRETE : Material.RED_CONCRETE);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            String string = language.getString(type.getLangReference());

            itemMeta.setDisplayName(
                    bool ? ChatColor.GREEN + string + ": " + language.getString("enabled")
                            : ChatColor.RED + string + ": " + language.getString("disabled"));

            itemMeta.getPersistentDataContainer().set(BOOLEAN, CustomPersistentDataType.PERSISTENT_BOOLEAN, bool);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public void handleClick(PersonalModifier.ModifierType type, Player player, PersonalModifier modifier, Inventory inventory,
                            ClickType clickType, Language language, int clickedSlot, InventoryManager manager, int itemId) {
        ItemStack item = inventory.getItem(clickedSlot);
        if (item == null)
            return;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null)
            return;

        Boolean aBoolean = itemMeta.getPersistentDataContainer().get(BOOLEAN, CustomPersistentDataType.PERSISTENT_BOOLEAN);
        if (aBoolean == null)
            aBoolean = (Boolean) type.getDefaultValue();

        modifier.set(type, !aBoolean);

        inventory.setItem(clickedSlot,
                          manager.insertItemId(get(player, language, type), itemId).getSecond());

    }

}
