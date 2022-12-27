package de.teddy.minesweeper.game.texture.pack;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.entity.Player;

public class DisableResourceHandler implements ResourcePackHandler {

    @Override
    public void apply(Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        if (personalModifier.getResourcePackUrl().isPresent())
            player.setResourcePack(personalModifier.getResourcePackUrl().get());
        else
            Painter.storePainterClass(player.getPersistentDataContainer(), ArmorStandPainter.class);
    }

    @Override
    public void close() {
    }

}
