package de.teddybear2004.retro.games.game.painter;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.minesweeper.MinesweeperField;
import org.bukkit.entity.Player;

import java.util.List;

public interface MinesweeperPainter extends Painter<MinesweeperField> {

    void drawBombs(Board<MinesweeperField> board, List<Player> players);

}
