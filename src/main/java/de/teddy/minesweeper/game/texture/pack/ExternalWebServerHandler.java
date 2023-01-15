package de.teddy.minesweeper.game.texture.pack;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExternalWebServerHandler implements ResourcePackHandler {

    private final @NotNull String link;

    public ExternalWebServerHandler(@NotNull String link) {
        this.link = link;
    }

    @Override
    public void apply(@NotNull Player player) {
        player.setResourcePack(getUrl(player));
    }

    public @NotNull String getUrl(@NotNull Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        return personalModifier.<String>get(PersonalModifier.ModifierType.RESOURCE_PACK_URL).orElse(link);
    }

    @Override
    public void close() {
    }

}
