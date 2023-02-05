package de.teddybear2004.minesweeper.game.painter;

import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.MinesweeperField;
import org.bukkit.entity.Player;

import java.util.List;

public interface MinesweeperPainter extends Painter<MinesweeperField> {

    void drawBombs(Board<MinesweeperField> board, List<Player> players);

}
