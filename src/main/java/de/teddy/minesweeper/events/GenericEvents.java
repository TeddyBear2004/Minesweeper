package de.teddy.minesweeper.events;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.BlockPainter;
import de.teddy.minesweeper.game.temporary.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class GenericEvents implements Listener {

    private static Area fromOutsideToInside(PlayerMoveEvent event) {
        for (Area area : Minesweeper.getAreas())
            if (!area.isInArea(event.getFrom()) && event.getTo() != null && area.isInArea(event.getTo()))
                return area;

        return null;
    }

    private static Area fromInsideToOutside(PlayerMoveEvent event) {
        for (Area area : Minesweeper.getAreas())
            if (event.getTo() != null && !area.isInArea(event.getTo()) && area.isInArea(event.getFrom()))
                return area;

        return null;
    }

    private static Area isInside(Location location) {
        for (Area area : Minesweeper.getAreas())
            if (area.isInArea(location))
                return area;

        return null;

    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Area inside = isInside(event.getPlayer().getLocation());
        if (inside == null || Minesweeper.getAreaSettings().isTemporaryInventoryEnabled())
            event.getPlayer().getInventory().setContents(Inventories.VIEWER_INVENTORY);
        if (inside == null || Minesweeper.getAreaSettings().isTemporaryFlightEnabled())
            event.getPlayer().setAllowFlight(true);
        Minesweeper.getTexturePackHandler().apply(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Game game = Game.getGame(event.getPlayer());
        if (game != null) {
            Game.finishGame(event.getPlayer());
            Board board = Game.getBoard(event.getPlayer());
            if (board != null)
                board.breakGame();
        }
    }


    @EventHandler
    public void onResourcePack(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();

        switch(event.getStatus()){
            case DECLINED, FAILED_DOWNLOAD -> Game.PLAYER_PAINTER_MAP.put(player, ArmorStandPainter.class);
            case SUCCESSFULLY_LOADED -> Game.PLAYER_PAINTER_MAP.put(player, BlockPainter.class);
        }

        boolean watching = false;
        for (Game map : Game.values()) {
            Board runningGame = map.getRunningGame();
            if (runningGame != null) {
                map.startViewing(event.getPlayer(), runningGame);
                watching = true;
                break;
            }
        }
        if (!watching) {
            if (Minesweeper.getGames().size() != 0)
                Minesweeper.getGames().get(0).startViewing(event.getPlayer(), null);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Area area = fromInsideToOutside(event);
        if (area != null) {
            if (Minesweeper.getAreaSettings().isTemporaryFlightEnabled()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
            if (Minesweeper.getAreaSettings().isTemporaryInventoryEnabled()) {
                player.getInventory().setContents(player.getInventory().getContents());
            }
            return;
        }

        area = fromOutsideToInside(event);
        if (area != null) {
            if (Minesweeper.getAreaSettings().isTemporaryFlightEnabled()) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }
    }

}
