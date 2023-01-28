package de.teddybear2004.minesweeper.game.texture.pack;

import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExternalWebServerHandler implements ResourcePackHandler {

    private final @NotNull String link;

    public ExternalWebServerHandler(@NotNull String link) {
        this.link = link;
    }

    @Override
    public void apply(@NotNull Player player) {
        player.setResourcePack(this.link);
    }

    public @NotNull String getUrl(@NotNull Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        return personalModifier.get(PersonalModifier.ModifierType.RESOURCE_PACK_URL);
    }

    @Override
    public void close() {
    }

}
