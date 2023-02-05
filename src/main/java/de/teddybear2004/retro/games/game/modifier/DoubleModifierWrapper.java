package de.teddybear2004.retro.games.game.modifier;

import de.teddybear2004.retro.games.RetroGames;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.util.HeadGenerator;
import de.teddybear2004.retro.games.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class DoubleModifierWrapper implements ModifierWrapper {

    private static final NamespacedKey DOUBLE = new NamespacedKey(RetroGames.getPlugin(RetroGames.class), "double");

    @Override
    public ItemStack get(Player player, Language language, PersonalModifier.ModifierType type) {
        Double d = player == null ? null : type.get(player.getPersistentDataContainer());

        d = d == null ? (Double) type.getDefaultValue() : d;

        ItemStack itemStack = new ItemStack(HeadGenerator.getHeadFromUrl("https://textures.minecraft.net/texture/33cd934f11f0766f5410eba9e7b5f0ceb66f6b317e845cb6a501f37258556a43"));

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            String string = language.getString(type.getLangReference());

            itemMeta.setDisplayName(ChatColor.GREEN + string + ": " + d);
            itemMeta.setLore(List.of(
                    ChatColor.GRAY + "Press left click to" + ChatColor.GREEN + " increment " + ChatColor.GRAY + "by 0,1.",
                    ChatColor.GRAY + "Press right click to" + ChatColor.RED + " decrement " + ChatColor.GRAY + "by 0,1."
            ));

            itemMeta.getPersistentDataContainer().set(DOUBLE, PersistentDataType.DOUBLE, d);
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

        Double d = itemMeta.getPersistentDataContainer().get(DOUBLE, PersistentDataType.DOUBLE);
        if (d == null)
            d = (Double) type.getDefaultValue();

        d += clickType.isLeftClick() ? 0.1 : clickType.isRightClick() ? -0.1 : 0;

        modifier.set(type, d);

        inventory.setItem(clickedSlot, manager.insertItemId(get(player, language, type), itemId).getSecond());

    }

}
