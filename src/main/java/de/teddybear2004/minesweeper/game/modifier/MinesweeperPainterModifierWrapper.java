package de.teddybear2004.minesweeper.game.modifier;

import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.MinesweeperBoard;
import de.teddybear2004.minesweeper.game.MinesweeperField;
import de.teddybear2004.minesweeper.game.inventory.InventoryManager;
import de.teddybear2004.minesweeper.game.painter.MinesweeperPainter;
import de.teddybear2004.minesweeper.game.painter.Painter;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;

public class MinesweeperPainterModifierWrapper implements PainterModifierWrapper {

    private static final NamespacedKey PAINTER = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "painter");

    @Override
    public ItemStack get(Player player, Language language, PersonalModifier.ModifierType type) {
        String s = player == null ? null : type.get(player.getPersistentDataContainer());

        s = s == null ? (String) type.getDefaultValue() : s;

        ItemStack itemStack = new ItemStack(Material.LIME_TERRACOTTA);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            Painter<?> painter = Painter.getPainter(s);
            if (painter == null)
                return itemStack;

            String string = language.getString(type.getLangReference());

            itemMeta.setDisplayName(ChatColor.GREEN + string + ": " + painter.getName());

            itemMeta.getPersistentDataContainer().set(PAINTER, PersistentDataType.STRING, s);
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

        String s = itemMeta.getPersistentDataContainer().get(PAINTER, PersistentDataType.STRING);
        if (s == null)
            return;

        Painter<MinesweeperField> painter = Painter.getPainter(s, MinesweeperPainter.class, MinesweeperField.class);
        MinesweeperBoard board = Minesweeper.getPlugin(Minesweeper.class).getGameManager().getBoardWatched(player, MinesweeperBoard.class);
        List<Player> players = Collections.singletonList(player);
        if (painter != null && board != null)
            painter.drawBlancField(board, players);

        Painter<MinesweeperField> newPainter = null;
        boolean returnOnNext = false;

        Painter<MinesweeperField> first = null;
        for (Painter<MinesweeperField> painter1 : Painter.getPainter(MinesweeperPainter.class, MinesweeperField.class)) {
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
            modifier.set(type, newPainter.getClass().getName());
            if (board != null)
                newPainter.drawField(board, players);
        }

        inventory.setItem(clickedSlot, manager.insertItemId(get(player, language, type), itemId).getSecond());

    }

}
