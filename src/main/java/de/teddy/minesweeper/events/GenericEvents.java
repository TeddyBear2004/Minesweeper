package de.teddy.minesweeper.events;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.modifier.ModifierArea;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.BlockPainter;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class GenericEvents implements Listener {

    private static boolean fromOutsideToInside(PlayerMoveEvent event) {
        for (ModifierArea modifierArea : Minesweeper.getAreas())
            if (!modifierArea.isInArea(event.getFrom()) && event.getTo() != null && modifierArea.isInArea(event.getTo()))
                return true;

        return false;
    }

    private static boolean fromInsideToOutside(PlayerMoveEvent event) {
        for (ModifierArea modifierArea : Minesweeper.getAreas())
            if (event.getTo() != null && !modifierArea.isInArea(event.getTo()) && modifierArea.isInArea(event.getFrom()))
                return true;

        return false;
    }

    public static boolean isInside(Location location) {
        for (ModifierArea modifierArea : Minesweeper.getAreas())
            if (modifierArea.isInArea(location))
                return true;

        return false;

    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.getPlayer().getInventory().setContents(Inventories.VIEWER_INVENTORY);

        if (Minesweeper.getAreaSettings().allowFly()
                || !Minesweeper.getAreaSettings().isTemporaryFlightEnabled()
                || isInside(event.getPlayer().getLocation()))
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
            case DECLINED, FAILED_DOWNLOAD ->
                    Painter.storePainterClass(player.getPersistentDataContainer(), ArmorStandPainter.class);
            case SUCCESSFULLY_LOADED ->
                    Painter.storePainterClass(player.getPersistentDataContainer(), BlockPainter.class);
        }

        if (Minesweeper.getAreaSettings().allowDefaultWatch()) {
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
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (fromInsideToOutside(event)) {
            if (!Minesweeper.getAreaSettings().allowFly() && Minesweeper.getAreaSettings().isTemporaryFlightEnabled()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }

            return;
        }

        if (fromOutsideToInside(event)) {
            if (!Minesweeper.getAreaSettings().allowFly() && Minesweeper.getAreaSettings().isTemporaryFlightEnabled()) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }
    }

}
