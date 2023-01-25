package de.teddybear2004.minesweeper.events;

import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.BlockPainter;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddybear2004.minesweeper.game.Board;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.inventory.Inventories;
import de.teddybear2004.minesweeper.game.modifier.Modifier;
import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import de.teddybear2004.minesweeper.game.texture.pack.ResourcePackHandler;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GenericEvents implements Listener {

    private final Game customGame;
    private final GameManager gameManager;
    private final ResourcePackHandler resourcePackHandler;
    private final @Nullable Team noCollision;

    /**
     * @param resourcePackHandler The {@link ResourcePackHandler} this handler should use.
     * @param customGame          The custom game.
     * @param gameManager         The game manager to start games.
     */
    public GenericEvents(ResourcePackHandler resourcePackHandler, Game customGame, GameManager gameManager) {
        this.resourcePackHandler = resourcePackHandler;
        this.customGame = customGame;
        this.gameManager = gameManager;
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager != null) {
            Scoreboard newScoreboard = scoreboardManager.getNewScoreboard();
            this.noCollision = newScoreboard.registerNewTeam("no_collision");
            this.noCollision.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        } else {
            noCollision = null;
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(@NotNull PlayerJoinEvent event) {
        event.getPlayer().getInventory().setContents(Inventories.VIEWER_INVENTORY);

        if (Modifier.getInstance().allowFly()
                || !Modifier.getInstance().isTemporaryFlightEnabled()
                || Modifier.getInstance().isInside(event.getPlayer().getLocation()))
            event.getPlayer().setAllowFlight(true);

        event.getPlayer().setCollidable(false);
        if (noCollision != null) {
            noCollision.addEntry(event.getPlayer().getName());
        }

        PersonalModifier modifier = PersonalModifier.getPersonalModifier(event.getPlayer());

        Optional<String> o = modifier.get(PersonalModifier.ModifierType.PAINTER_CLASS);
        if (o.isPresent()) {
            try{
                Class<? extends Painter> aClass = Class.forName(o.get()).asSubclass(Painter.class);

                if (aClass != BlockPainter.class) {
                    Painter.storePainterClass(event.getPlayer().getPersistentDataContainer(), aClass);
                    handleWatching(event.getPlayer());
                    return;
                }
            }catch(ClassNotFoundException | ClassCastException ignored){
            }
        }
        handleWatching(event.getPlayer());
        resourcePackHandler.apply(event.getPlayer());
    }

    private void handleWatching(@NotNull Player player) {
        if (Modifier.getInstance().allowDefaultWatch()) {
            boolean watching = false;
            for (Game map : gameManager.getGames()) {
                Board runningGame = map.getRunningGame();
                if (runningGame != null) {
                    map.startViewing(player, runningGame);
                    watching = true;
                    break;
                }
            }
            if (!watching) {
                if (gameManager.getGames().size() != 0)
                    gameManager.getGames().get(0).startViewing(player, null);
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(@NotNull PlayerQuitEvent event) {
        Game game = gameManager.getGame(event.getPlayer());
        if (game != null) {
            gameManager.finishGame(event.getPlayer(), false);
            Board board = gameManager.getBoard(event.getPlayer());
            if (board != null)
                board.breakGame();
        }
    }


    @EventHandler
    public void onResourcePack(@NotNull PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PersonalModifier personalModifier = PersonalModifier.getPersonalModifier(player);

        if (personalModifier.get(PersonalModifier.ModifierType.PAINTER_CLASS).isEmpty()) {
            switch(event.getStatus()){
                case DECLINED, FAILED_DOWNLOAD ->
                        Painter.storePainterClass(player.getPersistentDataContainer(), ArmorStandPainter.class);
                case SUCCESSFULLY_LOADED ->
                        Painter.storePainterClass(player.getPersistentDataContainer(), BlockPainter.class);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
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
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();

        if (block == null || gameManager.getBoard(event.getPlayer()) != null)
            return;

        if (gameManager.getBoardWatched(event.getPlayer()) != null && gameManager.getBoardWatched(event.getPlayer()).isGenerated())
            return;

        for (Game game : gameManager.getGames()) {
            if (game.equals(customGame) || game.isOutside(block.getLocation())) continue;

            game.getStarter()
                    .setShouldTeleport(false)
                    .build(event.getPlayer());
            break;
        }
    }
}
