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
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player.getPersistentDataContainer());

        player.setResourcePack(personalModifier.getResourcePackUrl().orElse(link));
    }

    @Override
    public void close() {
    }

}
