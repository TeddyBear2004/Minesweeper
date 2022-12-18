package de.teddy.minesweeper.game.texture.pack;

import org.bukkit.entity.Player;

public class ExternalWebServerHandler implements ResourcePackHandler {

    private final String link;

    public ExternalWebServerHandler(String link){
        this.link = link;
    }

    @Override
    public void apply(Player player) {
        player.setResourcePack(link);
    }

    @Override
    public void close() {
    }

}
