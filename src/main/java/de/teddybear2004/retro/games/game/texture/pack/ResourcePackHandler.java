package de.teddybear2004.retro.games.game.texture.pack;

import org.bukkit.entity.Player;

import java.io.Closeable;

public interface ResourcePackHandler extends Closeable {

    void apply(Player player);

}
