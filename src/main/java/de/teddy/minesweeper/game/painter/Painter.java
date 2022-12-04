package de.teddy.minesweeper.game.painter;

import de.teddy.minesweeper.game.Board;
import org.bukkit.entity.Player;

import java.util.List;

public interface Painter {

    void applyBoard(Board board);

    void drawBlancField(List<Player> players);

    void drawField(List<Player> players);

}
