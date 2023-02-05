package de.teddybear2004.minesweeper.game.modifier;

import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.util.HeadGenerator;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class IntegerModifierWrapper implements ModifierWrapper {

    private static final NamespacedKey INTEGER = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "integer");

    @Override
    public ItemStack get(Player player, Language language, PersonalModifier.ModifierType type) {
        Integer integer = player == null ? null : type.get(player.getPersistentDataContainer());

        integer = integer == null ? (Integer) type.getDefaultValue() : integer;

        ItemStack itemStack = new ItemStack(HeadGenerator.getHeadFromUrl("https://textures.minecraft.net/texture/33cd934f11f0766f5410eba9e7b5f0ceb66f6b317e845cb6a501f37258556a43"));

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            String string = language.getString(type.getLangReference());

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

    @Override
    public void handleClick(PersonalModifier.ModifierType type, Player player, PersonalModifier modifier, Inventory inventory,
                            ClickType clickType, Language language, int clickedSlot, InventoryManager manager, int itemId) {
        ItemStack item = inventory.getItem(clickedSlot);
        if (item == null)
            return;

        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null)
            return;

        Integer integer = itemMeta.getPersistentDataContainer().get(INTEGER, PersistentDataType.INTEGER);
        if (integer == null)
            integer = (Integer) type.getDefaultValue();

        integer += clickType.isLeftClick() ? 1 : clickType.isRightClick() ? -1 : 0;

        modifier.set(type, integer);

        inventory.setItem(clickedSlot, manager.insertItemId(get(player, language, type), itemId).getSecond());

    }

}
