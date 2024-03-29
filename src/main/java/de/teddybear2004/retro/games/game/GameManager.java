package de.teddybear2004.retro.games.game;

import de.teddybear2004.retro.games.RetroGames;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.inventory.generator.game.manager.AddGameGenerator;
import de.teddybear2004.retro.games.game.modifier.Modifier;
import de.teddybear2004.retro.games.scheduler.RemoveMarkerScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final Map<Player, Board<?>> gameWatched = new HashMap<>();
    private final Map<Player, Board<?>> runningGames = new HashMap<>();
    private final Map<Player, Game> playerLocation = new HashMap<>();
    private final List<Game> games;
    private final RemoveMarkerScheduler removeMarkerScheduler;

    public GameManager(List<Game> games, RemoveMarkerScheduler removeMarkerScheduler) {
        this.games = games;
        this.removeMarkerScheduler = removeMarkerScheduler;
    }

    public void startWatching(@NotNull Player p, @NotNull Board<?> b) {
        stopWatching(p);
        Game cur = getPlayerLocation().get(p);
        if (cur != b.getGame()) {
            switchToMap(p, b.getGame());
        }
        getGameWatched().put(p, b);
        b.addViewer(p);
    }

    private void stopWatching(Player p) {
        Board<?> b = getGameWatched().remove(p);
        if (b != null) {
            b.removeViewer(p);
        }
    }

    public @NotNull Map<Player, Game> getPlayerLocation() {
        return playerLocation;
    }

    public void switchToMap(@NotNull Player p, @NotNull Game g) {
        stopWatching(p);
        Board<?> b = getRunningGames().get(p);
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

    public @NotNull Map<Player, Board<?>> getGameWatched() {
        return gameWatched;
    }

    public @NotNull Map<Player, Board<?>> getRunningGames() {
        return runningGames;
    }

    public void finishGame(Player p) {
        finishGame(p, true);
    }

    public void finishGame(Player p, boolean saveStats) {
        getGame(p).finish(p, saveStats);
    }

    public Game getGame(Player player) {
        return getPlayerLocation().get(player);
    }

    public void stopGames(Player p, boolean saveStats) {
        Board<?> b = getRunningGames().get(p);
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

    public Board<?> getBoard(Player Player) {
        return getRunningGames().get(Player);
    }

    public <F extends Board<?>> F getBoard(Player Player, Class<F> boardClass) {
        Board<?> board = getRunningGames().get(Player);
        if (boardClass.isInstance(board))
            return boardClass.cast(board);

        return null;
    }

    public Board<?> getBoardWatched(Player player) {
        return getGameWatched().get(player);
    }

    public <F extends Board<?>> F getBoardWatched(Player player, Class<F> boardClass) {
        Board<?> board = getGameWatched().get(player);
        if (boardClass.isInstance(board))
            return boardClass.cast(board);

        return null;
    }

    public List<Game> getGames() {
        return games;
    }

    public boolean isInside(Location location) {
        for (Game game : this.games)
            if (!game.isOutside(location))
                return true;

        return false;
    }

    public RemoveMarkerScheduler getRemoveMarkerScheduler() {
        return removeMarkerScheduler;
    }

    public static class Creator {

        private static final Map<Player, Creator> PLAYER_CREATOR_MAP = new HashMap<>();
        private final Player player;
        private final InventoryManager manager;
        private Type currentActive = null;
        private Location corner;
        private Location spawn;
        private Location otherCorner;
        private int bombCount;
        private String langPath;

        private Creator(Player player, InventoryManager manager) {
            this.player = player;
            this.manager = manager;

            EventListener.createInstance(RetroGames.getPlugin(RetroGames.class));
        }

        public static Creator getInstance(Player player, InventoryManager manager) {
            return PLAYER_CREATOR_MAP.computeIfAbsent(player, player1 -> new Creator(player1, manager));
        }

        public void setActive(Type type) {
            currentActive = type;
        }

        public enum Type {
            CORNER,
            SPAWN,
            OTHER_CORNER,
            BOMB_COUNT,
            LANG_PATH
        }

    }

    public static class EventListener implements Listener {

        private static EventListener handler = null;

        private EventListener(Plugin plugin) {
            if (handler != null)
                throw new RuntimeException();

            Bukkit.getPluginManager().registerEvents(this, plugin);
            handler = this;
        }

        public static void createInstance(Plugin plugin) {
            if (handler == null) {
                new EventListener(plugin);
            }
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            Creator creator = Creator.PLAYER_CREATOR_MAP.get(event.getPlayer());
            if (creator != null) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null && (creator.currentActive == Creator.Type.CORNER || creator.currentActive == Creator.Type.OTHER_CORNER || creator.currentActive == Creator.Type.SPAWN) && event.getAction() == Action.LEFT_CLICK_BLOCK) {

                    switch(creator.currentActive){
                        case CORNER -> creator.corner = clickedBlock.getLocation();
                        case OTHER_CORNER -> creator.otherCorner = clickedBlock.getLocation();
                        case SPAWN -> creator.spawn = clickedBlock.getLocation();
                    }

                    event.getPlayer().sendMessage("Set corner!");
                    creator.currentActive = null;
                    event.getPlayer().openInventory(creator.manager.getInventory(AddGameGenerator.class, event.getPlayer()));
                }
            }
        }

    }

}
