package de.teddybear2004.retro.games.game.texture.pack;

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

    @Override
    public void close() {
    }

}
