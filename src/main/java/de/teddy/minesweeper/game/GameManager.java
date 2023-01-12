package de.teddy.minesweeper.game;

import de.teddy.minesweeper.game.modifier.Modifier;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameManager {

    private final Map<Player, Board> gameWatched = new HashMap<>();
    private final Map<Player, Board> runningGames = new HashMap<>();
    private final Map<Player, Game> playerLocation = new HashMap<>();

    public void switchToMap(Player p, Game g) {
        stopWatching(p);
        Board b = getRunningGames().get(p);
        if (b != null) {
            finishGame(p);
        }
        getPlayerLocation().put(p, g);
        if (Modifier.getInstance().allowFly() || Modifier.getInstance().isInside(g.getViewingSpawn())) {
            p.setAllowFlight(true);
            p.setFlying(true);
        }
        p.teleport(g.getViewingSpawn());
    }

    public void startWatching(Player p, Board b) {
        stopWatching(p);
        Game cur = getPlayerLocation().get(p);
        if (cur != b.map) {
            switchToMap(p, b.map);
        }
        b.draw(Collections.singletonList(p));
        getGameWatched().put(p, b);
        b.addViewer(p);
    }

    private void stopWatching(Player p) {
        Board b = getGameWatched().remove(p);
        if (b != null) {
            b.removeViewer(p);
        }
    }

    public void stopGames(Player p, boolean saveStats) {
        Board b = getRunningGames().get(p);
        if (b != null) {
            b.drawBlancField();
            b.finish(false, saveStats);
            b.getViewers().forEach(getGameWatched()::remove);
            b.clearViewers();
        } else {
            stopWatching(p);
        }
        getRunningGames().remove(p);
    }

    public Game getGame(Player player) {
        return getPlayerLocation().get(player);
    }

    public Board getBoard(Player Player) {
        return getRunningGames().get(Player);
    }

    public Board getBoardWatched(Player player) {
        return getGameWatched().get(player);
    }

    public void finishGame(Player p) {
        finishGame(p, true);
    }

    public void finishGame(Player p, boolean saveStats) {
        getGame(p).finish(p, saveStats);
    }

    public Map<Player, Board> getRunningGames() {
        return runningGames;
    }

    public Map<Player, Board> getGameWatched() {
        return gameWatched;
    }

    public Map<Player, Game> getPlayerLocation() {
        return playerLocation;
    }

}
