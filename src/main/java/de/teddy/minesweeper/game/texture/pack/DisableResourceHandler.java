package de.teddy.minesweeper.game.texture.pack;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import org.bukkit.entity.Player;

public class DisableResourceHandler implements ResourcePackHandler {

    @Override
    public void apply(Player player) {
        Game.PLAYER_PAINTER_MAP.put(player, ArmorStandPainter.class);
    }

    @Override
    public void close() {
    }

}
