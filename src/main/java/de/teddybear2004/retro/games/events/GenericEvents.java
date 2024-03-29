package de.teddybear2004.retro.games.events;

import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.modifier.Modifier;
import de.teddybear2004.retro.games.game.painter.ArmorStandPainter;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.game.painter.BlockPainter;
import de.teddybear2004.retro.games.game.painter.Painter;
import de.teddybear2004.retro.games.game.texture.pack.ResourcePackHandler;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
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

public class GenericEvents implements Listener {

    private final Game customGame;
    private final GameManager gameManager;
    private final Atelier atelier;
    private final ResourcePackHandler resourcePackHandler;
    private final @Nullable Team noCollision;

    /**
     * @param resourcePackHandler The {@link ResourcePackHandler} this handler should use.
     * @param customGame          The custom game.
     * @param gameManager         The game manager to start games.
     */
    public GenericEvents(ResourcePackHandler resourcePackHandler, Game customGame, GameManager gameManager, Atelier atelier) {
        this.resourcePackHandler = resourcePackHandler;
        this.customGame = customGame;
        this.gameManager = gameManager;
        this.atelier = atelier;
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
        Player player = event.getPlayer();
        InventoryManager.PlayerInventory.VIEWER.apply(player);

        if (Modifier.getInstance().allowFly()
                || !Modifier.getInstance().isTemporaryFlightEnabled()
                || Modifier.getInstance().isInside(player.getLocation()))
            player.setAllowFlight(true);

        player.setCollidable(false);
        if (noCollision != null) {
            noCollision.addEntry(player.getName());
        }

        @SuppressWarnings("rawtypes")
        Class<? extends Painter> painterClass = atelier.getPainterClass(player);
        if (!BlockPainter.class.isAssignableFrom(painterClass)) {
            atelier.save(player, Atelier.getDefault());
            handleWatching(player);
            return;
        }

        handleWatching(player);
        resourcePackHandler.apply(player);
    }

    private void handleWatching(@NotNull Player player) {
        if (Modifier.getInstance().allowDefaultWatch()) {
            boolean watching = false;
            for (Game map : gameManager.getGames()) {
                Board<?> runningGame = map.getRunningGame();
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
        Board<?> board = gameManager.getBoard(event.getPlayer());
        if (game != null && board != null && board.getPlayer().equals(event.getPlayer())) {
            gameManager.finishGame(event.getPlayer(), false);
            board.breakGame();
        }
    }


    @EventHandler
    public void onResourcePack(@NotNull PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();

        switch(event.getStatus()){
            case DECLINED, FAILED_DOWNLOAD -> atelier.save(player, ArmorStandPainter.class);
            case SUCCESSFULLY_LOADED -> atelier.save(player, BlockPainter.class);
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
                    .build(event.getPlayer(), MinesweeperBoard.class);
            break;
        }
    }

}
