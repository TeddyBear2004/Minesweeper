package de.teddybear2004.minesweeper.game;

import de.teddybear2004.minesweeper.game.event.BoardFinishEvent;
import de.teddybear2004.minesweeper.game.event.BoardLoseEvent;
import de.teddybear2004.minesweeper.game.event.BoardWinEvent;
import de.teddybear2004.minesweeper.game.event.DuelLeaveEvent;
import de.teddybear2004.minesweeper.util.Time;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class DuelGame implements Listener {

    private final Game game;
    private final Map<Player, Board> playerBoardMap;

    public DuelGame(Plugin plugin, Game game, Set<Player> players) {
        this.game = game;
        this.playerBoardMap = new HashMap<>();
        players.forEach(player -> this.playerBoardMap.put(player, null));

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean startGame() {
        for (Player player : this.playerBoardMap.keySet()) {
            Board board = game.getGameManager().getBoard(player);
            if (!(board == null || !board.isGenerated())) {
                broadcastMessage("Der Spieler " + player.getName() + " hat ein Spiel gestartet!");
                return false;
            }
        }

        Game.Builder builder = this.game.getStarter()
                .setSaveStats(true)
                .setShouldTeleport(true);

        this.playerBoardMap.keySet().forEach(player -> this.playerBoardMap.put(player, builder.build(player)));
        return true;
    }

    @EventHandler
    public void onBoardWin(BoardWinEvent event) {
        this.onBoardFinish(event);
    }

    private void broadcastMessage(String message) {
        this.playerBoardMap.keySet().forEach(player -> player.sendMessage(message));
    }

    private void onBoardFinish(BoardFinishEvent event) {
        if (!this.playerBoardMap.containsValue(event.getBoard()))
            return;

        int completed = getCompleted();

        String message = event.getPlayer().getName() + " hat das Spiel beendet! (" + completed + " / " + playerBoardMap.size() + ")";
        broadcastMessage(message);

        checkDone(completed);
    }

    private int getCompleted() {
        int completed = 0;
        for (Board value : this.playerBoardMap.values())
            if (value.isFinished())
                completed++;
        return completed;
    }

    private void checkDone(int completed) {
        if (completed >= playerBoardMap.size()) {
            String message = "Alle Spieler haben ihr beendet.";
            broadcastMessage(message);
            List<Board> boards = new ArrayList<>(playerBoardMap.values());

            boards.sort(Board::compareTo);

            for (int i = 0; i < boards.size(); i++) {
                Board board = boards.get(i);
                if (board.getDuration() != null) {
                    message = ChatColor.GOLD.toString() + (i + 1) + ". Platz: " + board.getPlayer().getName() + " Zeit: " + Time.parse(false, board.getDuration()) + " Flaggen Score: " + (board.isWin() ? board.getBombCount() : board.calculateFlagScore());
                    broadcastMessage(message);
                }
            }

            HandlerList.unregisterAll(this);
        }
    }

    @EventHandler
    public void onDuelLeave(DuelLeaveEvent event) {
        if (playerBoardMap.remove(event.getPlayer()) == null)
            return;

        int completed = getCompleted();

        String message = event.getPlayer().getName() + " hat das Spiel verlassen! (" + completed + " / " + playerBoardMap.size() + ")";
        broadcastMessage(message);

        checkDone(completed);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!this.playerBoardMap.containsKey(event.getPlayer()))
            return;

        onBoardFinish(new BoardLoseEvent(this.playerBoardMap.get(event.getPlayer()), event.getPlayer(), 0, 0));
    }

    @EventHandler
    public void onBoardLose(BoardLoseEvent event) {
        this.onBoardFinish(event);
    }

    public static class Builder {

        private final Player owner;
        private final Plugin plugin;
        private final Set<Player> invited = new HashSet<>();
        private final Set<Player> accepted = new HashSet<>();
        private Game game;

        public Builder(Player player, Plugin plugin) {
            this.owner = player;
            this.plugin = plugin;
        }

        public boolean invite(Player player) {
            return this.invited.add(player);
        }

        public boolean accept(Player player) {
            if (invited.remove(player))
                return accepted.add(player);
            return false;
        }

        public void setGame(Game game) {
            this.game = game;
        }

        public boolean kick(Player player) {
            return invited.remove(player) | accepted.remove(player);
        }

        public void broadcast(String message) {
            this.broadcast(message, true);
        }

        public void broadcast(String message, boolean all) {
            owner.sendMessage(message);
            (all ? getAll() : getAccepted()).forEach(player -> player.sendMessage(message));
        }

        public Set<Player> getAll() {
            Set<Player> players = new HashSet<>();
            players.addAll(getInvited());
            players.addAll(getAccepted());
            return players;
        }

        public Set<Player> getAccepted() {
            return accepted;
        }

        public Set<Player> getInvited() {
            return invited;
        }

        public DuelGame build(Game defaultGame) {
            Set<Player> players = new HashSet<>(accepted);
            players.add(owner);
            return new DuelGame(plugin, game == null ? defaultGame : game, players);
        }

    }

}
