package de.teddy.minesweeper.game.painter;

import de.teddy.minesweeper.game.Board;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Painter {

    void drawBlancField(Board board, List<Player> players);

    void drawField(Board board, List<Player> players);

    void drawBombs(Board board, List<Player> players);

    ItemStack getActualItemStack(Board.Field field);

    Material getActualMaterial(Board.Field field);

}
