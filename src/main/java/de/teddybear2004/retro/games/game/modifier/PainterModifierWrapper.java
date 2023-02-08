package de.teddybear2004.retro.games.game.modifier;

import de.teddybear2004.retro.games.RetroGames;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.game.painter.Painter;
import de.teddybear2004.retro.games.util.Language;
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

public class PainterModifierWrapper implements ModifierWrapper {

    private static final NamespacedKey PAINTER = new NamespacedKey(RetroGames.getPlugin(RetroGames.class), "painter");
    private final Atelier atelier;

    public PainterModifierWrapper(Atelier atelier) {
        this.atelier = atelier;
    }

    @Override
    public ItemStack get(Player player, Language language, PersonalModifier.ModifierType type) {
        String s = player == null ? null : type.get(player.getPersistentDataContainer());

        s = s == null ? type.getDefaultValue().toString() : s;

        ItemStack itemStack = new ItemStack(Material.LIME_TERRACOTTA);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            Painter<?> painter = atelier.getPainter(Atelier.getPainterClass(s));
            if (painter == null)
                return itemStack;

            String string = language.getString(type.getLangReference());

            itemMeta.setDisplayName(ChatColor.GREEN + string + ": " + painter.getName());

            itemMeta.getPersistentDataContainer().set(PAINTER, PersistentDataType.STRING, s);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }


    @SuppressWarnings({"raw", "unchecked"})
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

        Painter<?> painter = atelier.getPainter(Atelier.getPainterClass(s));
        @SuppressWarnings("rawtypes")
        Board board = RetroGames.getPlugin(RetroGames.class).getGameManager().getBoardWatched(player);
        List<Player> players = Collections.singletonList(player);
        if (painter != null && board != null)
            painter.drawBlancField(board, players);

        Class<? extends Painter<?>> newPainter = null;
        Class<? extends Painter<?>> first = null;
        boolean returnOnNext = false;

        for (Class<? extends Painter<?>> painter1 : atelier.getList()) {
            if (first == null)
                first = painter1;

            if (painter == null || returnOnNext) {
                newPainter = painter1;
                break;
            }

            if (painter1.isAssignableFrom(painter.getClass()))
                returnOnNext = true;
        }


        if (newPainter == null)
            newPainter = first;

        if (newPainter != null) {
            modifier.set(type, newPainter.getName());
            if (board != null) {
                atelier.getPainter(newPainter, board.getBoardClass()).drawField(board, players);
            }
        }

        inventory.setItem(clickedSlot, manager.insertItemId(get(player, language, type), itemId).getSecond());

    }

}
