package de.teddy.minesweeper.game.texture.pack;

import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.entity.Player;

public class DisableResourceHandler implements ResourcePackHandler {

    @Override
    public void apply(Player player) {
        Painter.storePainterClass(player.getPersistentDataContainer(), ArmorStandPainter.class);
    }

    @Override
    public void close() {
    }

}
