package de.teddybear2004.retro.games.game.texture.pack;

import de.teddybear2004.retro.games.game.modifier.PersonalModifier;
import de.teddybear2004.retro.games.game.painter.Painter;
import de.teddybear2004.retro.games.minesweeper.painter.MinesweeperArmorStandPainter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisableResourceHandler implements ResourcePackHandler {

    @Override
    public void apply(@NotNull Player player) {
        String url = getUrl(player);
        if (url != null)
            player.setResourcePack(url);
        else
            Painter.storePainterClass(player.getPersistentDataContainer(), MinesweeperArmorStandPainter.class);
    }

    public @Nullable String getUrl(@NotNull Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        return personalModifier.get(PersonalModifier.ModifierType.RESOURCE_PACK_URL);
    }

    @Override
    public void close() {
    }

}
