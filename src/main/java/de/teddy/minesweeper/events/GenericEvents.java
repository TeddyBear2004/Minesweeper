package de.teddy.minesweeper.events;

import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.modifier.Modifier;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.BlockPainter;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddy.minesweeper.game.texture.pack.ResourcePackHandler;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;

import java.util.List;

public class GenericEvents implements Listener {

    private final List<Game> games;
    private final ResourcePackHandler resourcePackHandler;

    public GenericEvents(List<Game> games, ResourcePackHandler resourcePackHandler) {
        this.games = games;
        this.resourcePackHandler = resourcePackHandler;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.getPlayer().getInventory().setContents(Inventories.VIEWER_INVENTORY);

        if (Modifier.getInstance().allowFly()
                || !Modifier.getInstance().isTemporaryFlightEnabled()
                || Modifier.getInstance().isInside(event.getPlayer().getLocation()))
            event.getPlayer().setAllowFlight(true);

        PersonalModifier modifier = PersonalModifier.getPersonalModifier(event.getPlayer().getPersistentDataContainer());

        if (modifier.getPainterClass().isPresent()) {
            try{
                Class<? extends Painter> aClass = Class.forName(modifier.getPainterClass().get()).asSubclass(Painter.class);

                if (aClass != BlockPainter.class) {
                    Painter.storePainterClass(event.getPlayer().getPersistentDataContainer(), aClass);
                    return;
                }
            }catch(ClassNotFoundException | ClassCastException ignored){
            }
        }
        resourcePackHandler.apply(event.getPlayer());

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
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player.getPersistentDataContainer());

        if (personalModifier.getPainterClass().isEmpty()) {
            switch(event.getStatus()){
                case DECLINED, FAILED_DOWNLOAD ->
                        Painter.storePainterClass(player.getPersistentDataContainer(), ArmorStandPainter.class);
                case SUCCESSFULLY_LOADED ->
                        Painter.storePainterClass(player.getPersistentDataContainer(), BlockPainter.class);
            }
        }

        if (Modifier.getInstance().allowDefaultWatch()) {
            boolean watching = false;
            for (Game map : games) {
                Board runningGame = map.getRunningGame();
                if (runningGame != null) {
                    map.startViewing(event.getPlayer(), runningGame);
                    watching = true;
                    break;
                }
            }
            if (!watching) {
                if (games.size() != 0)
                    games.get(0).startViewing(event.getPlayer(), null);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (Modifier.getInstance().fromInsideToOutside(event)) {
            if (!Modifier.getInstance().allowFly() && Modifier.getInstance().isTemporaryFlightEnabled()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }

            return;
        }

        if (Modifier.getInstance().fromOutsideToInside(event)) {
            if (!Modifier.getInstance().allowFly() && Modifier.getInstance().isTemporaryFlightEnabled()) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();

        if (Game.getBoard(event.getPlayer()) != null || Game.getBoardWatched(event.getPlayer()) != null)
            return;

        for (Game game : games) {
            if (game.isBlockOutsideGame(block)) continue;

            game.startGame(event.getPlayer(), false);
            break;
        }
    }

}
