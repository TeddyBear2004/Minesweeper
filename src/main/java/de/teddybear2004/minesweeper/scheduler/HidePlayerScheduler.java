package de.teddybear2004.minesweeper.scheduler;

import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HidePlayerScheduler extends BukkitRunnable {

    private final Plugin plugin;
    private final GameManager gameManager;

    public HidePlayerScheduler(Plugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PersonalModifier modifier = PersonalModifier.getPersonalModifier(player);

            boolean b1 = modifier.<Boolean>get(PersonalModifier.ModifierType.HIDE_PLAYER);
            boolean justHideInGame = modifier.<Boolean>get(PersonalModifier.ModifierType.JUST_HIDE_WHILE_IN_GAME);
            double distance = Math.pow(modifier.<Double>get(PersonalModifier.ModifierType.HIDE_PLAYER_DISTANCE), 2);

            player.getWorld().getPlayers().forEach(other -> {
                if (b1) {
                    if (justHideInGame) {
                        if (gameManager.getBoard(player) == null)
                            player.showPlayer(plugin, other);
                        else
                            hide(player, other, distance);
                    } else
                        hide(player, other, distance);
                } else
                    player.showPlayer(plugin, other);
            });
        });
    }

    private void hide(Player player, Player other, double distance) {
        boolean b3 = player.getLocation().distanceSquared(other.getLocation()) < distance;
        if (b3) {
            player.hidePlayer(plugin, other);
        } else {
            player.showPlayer(plugin, other);
        }
    }

}
