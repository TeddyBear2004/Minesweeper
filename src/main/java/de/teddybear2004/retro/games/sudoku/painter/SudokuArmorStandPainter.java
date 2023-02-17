package de.teddybear2004.retro.games.sudoku.painter;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.click.ClickHandler;
import de.teddybear2004.retro.games.game.painter.ArmorStandPainter;
import de.teddybear2004.retro.games.sudoku.SudokuBoard;
import de.teddybear2004.retro.games.sudoku.SudokuField;
import de.teddybear2004.retro.games.util.HeadGenerator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SudokuArmorStandPainter extends ArmorStandPainter<SudokuField> implements SudokuPainter {

    public static final ItemStack[] ITEM_STACKS = {
            new ItemStack(Material.AIR),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/301d0496e606b74327f03259c7f6e32ba76862d8dda5b639b229d2e6781ff143"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/cb08394f844dc8eeeaadf05d8bdaa045dbcc0db932cfb8d0daeabafcee995f15"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/858b9d62566a0e08f47d849f7d21e3adb9c99ab36d45d54aee987c68dcd2b313"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/4ffb37d1eb6a453fea35eb6d6a71d3bdbd68ef1f59f96be26317ce2ef57170f7"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/d5b6f519bd847dec7aaf003852c60055ff10bd55b1df7666ac90f700513fdd49"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/976ead580e797d6cba3f98ca7f7be1bb642da5485e6afb0ce641a4ac3d37a408"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/581559800cf78b5324519d939f69c83d4a41a00b1bd770cf425fb9a65e3d1d45"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/aae59430f811d4037d548e3891f03667393aee15d0ded4da64ca84973b0d60db"),
            HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/aae59430f811d4037d548e3891f03667393aee15d0ded4da64ca84973b0d60db")
    };

    public SudokuArmorStandPainter(Plugin plugin, ClickHandler<? super SudokuField, ? super Board<SudokuField>> clickHandler, GameManager gameManager) {
        super(plugin, clickHandler, gameManager, SudokuBoard.class);
    }

    @Override
    public ItemStack getActualItemStack(@NotNull SudokuField field) {
        return ITEM_STACKS[field.getNumber()];
    }

}
