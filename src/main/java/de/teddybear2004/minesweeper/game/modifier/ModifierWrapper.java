package de.teddybear2004.minesweeper.game.modifier;

import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface ModifierWrapper {

    ItemStack get(Player player, Language language, PersonalModifier.ModifierType type);

    void handleClick(PersonalModifier.ModifierType type, Player player, PersonalModifier modifier, Inventory inventory,
                     ClickType clickType, Language language, int clickedSlot, InventoryManager manager, int itemId);


}
