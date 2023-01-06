package de.teddy.minesweeper.game;

import de.teddy.minesweeper.util.ConnectionBuilder;
import de.teddy.minesweeper.util.Language;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class TutorialGame extends Game {

    private final Board.Field[][] board;

    public TutorialGame(Plugin plugin, GameManager gameManager, List<Game> games, Language language, ConnectionBuilder connectionBuilder, Location corner, Location spawn, Board.Field[][] board, String name, Material material, int inventoryPosition) {
        super(plugin, gameManager, games, language, connectionBuilder, corner, spawn, -1, -1, name, material, inventoryPosition);
        this.board = board;
    }

    @Override
    public Board startGame(Player p, boolean shouldTeleport, int bombCount, int width, int height, long seed, boolean setSeed, boolean saveStats) {
        return super.startGame(p, shouldTeleport, bombCount, width, height, seed, setSeed, saveStats);


    }

}
