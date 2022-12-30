package de.teddy.minesweeper.game.texture.pack;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.entity.Player;

public class ExternalWebServerHandler implements ResourcePackHandler {

    private final String link;

    public ExternalWebServerHandler(String link) {
        this.link = link;
    }

    @Override
    public void apply(Player player) {
        player.setResourcePack(getUrl(player));
    }

    @Override
    public String getUrl(Player player) {
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        return personalModifier.getResourcePackUrl().orElse(link);
    }

    @Override
    public void close() {
    }

}
