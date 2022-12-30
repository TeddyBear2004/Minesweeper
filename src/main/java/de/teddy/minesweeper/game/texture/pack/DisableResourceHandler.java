package de.teddy.minesweeper.game.texture.pack;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.entity.Player;

public class DisableResourceHandler implements ResourcePackHandler {

    @Override
    public void apply(Player player) {
        String url = getUrl(player);
        if (url != null)
            player.setResourcePack(url);
        else
            Painter.storePainterClass(player.getPersistentDataContainer(), ArmorStandPainter.class);
    }

    @Override
    public String getUrl(Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        return personalModifier.getResourcePackUrl().orElse(null);
    }

    @Override
    public void close() {
    }

}
