package de.teddy.minesweeper.game.texture.pack;

import org.bukkit.entity.Player;

import java.io.Closeable;

public interface ResourcePackHandler extends Closeable {

    void apply(Player player);

    String getUrl(Player player);

}
