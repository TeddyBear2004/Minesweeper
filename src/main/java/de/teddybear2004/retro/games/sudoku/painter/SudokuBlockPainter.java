package de.teddybear2004.retro.games.sudoku.painter;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.click.ClickHandler;
import de.teddybear2004.retro.games.game.painter.BlockPainter;
import de.teddybear2004.retro.games.sudoku.SudokuBoard;
import de.teddybear2004.retro.games.sudoku.SudokuField;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SudokuBlockPainter extends BlockPainter<SudokuField> implements SudokuPainter {

    public static final Material[] LIGHT_MATERIALS = {
            Material.WHITE_CONCRETE_POWDER,
            Material.LIME_TERRACOTTA,
            Material.GREEN_CONCRETE,
            Material.YELLOW_TERRACOTTA,
            Material.ORANGE_TERRACOTTA,
            Material.MAGENTA_TERRACOTTA,
            Material.PINK_TERRACOTTA,
            Material.PURPLE_TERRACOTTA,
            Material.RED_TERRACOTTA,
            Material.WHITE_TERRACOTTA};
    public static final Material[] DARK_MATERIALS = {
            Material.LIGHT_GRAY_CONCRETE_POWDER,
            Material.TERRACOTTA,
            Material.GREEN_TERRACOTTA,
            Material.BROWN_TERRACOTTA,
            Material.BLUE_TERRACOTTA,
            Material.CYAN_TERRACOTTA,
            Material.LIGHT_GRAY_TERRACOTTA,
            Material.GRAY_TERRACOTTA,
            Material.LIGHT_BLUE_TERRACOTTA,
            Material.BLACK_TERRACOTTA,
    };

    public SudokuBlockPainter(Plugin plugin, ClickHandler<? super SudokuField, ? super Board<SudokuField>> clickHandler, GameManager gameManager) {
        super(plugin, clickHandler, gameManager, SudokuBoard.class);
    }


    @Override
    public Material[] getActualMaterial(@NotNull SudokuField field) {
        return new Material[]{
                (Board.isLightField(field.getX(), field.getY()) ? LIGHT_MATERIALS : DARK_MATERIALS)[field.getNumber()]
        };
    }

}
