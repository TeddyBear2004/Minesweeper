package de.teddybear2004.retro.games.game.texture.pack;

import de.teddybear2004.retro.games.game.modifier.PersonalModifier;
import de.teddybear2004.retro.games.game.painter.Atelier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisableResourceHandler implements ResourcePackHandler {

    private final Atelier atelier;

    public DisableResourceHandler(Atelier atelier) {
        this.atelier = atelier;
    }

    @Override
    public void apply(@NotNull Player player) {
        String url = getUrl(player);
        if (url != null)
            player.setResourcePack(url);
        else
            atelier.save(player, Atelier.getDefault());
    }

    public @Nullable String getUrl(@NotNull Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        return personalModifier.get(PersonalModifier.ModifierType.RESOURCE_PACK_URL);
    }

    @Override
    public void close() {
    }

}
