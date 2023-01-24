package de.teddybear2004.minesweeper.scheduler;

import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

            boolean hidePlayer = modifier.<Boolean>get(PersonalModifier.ModifierType.HIDE_PLAYER).orElse(false)
                    && (modifier.<Boolean>get(PersonalModifier.ModifierType.JUST_HIDE_WHILE_IN_GAME).orElse(false)
                    && gameManager.getGame(player) != null);
            double distance = Math.pow(modifier.<Double>get(PersonalModifier.ModifierType.HIDE_PLAYER_DISTANCE).orElse(3.0), 2);
            Location location = player.getLocation();

            player.getWorld().getPlayers().forEach(other -> {
                if (hidePlayer && location.distanceSquared(other.getLocation()) < distance) {
                    player.hidePlayer(plugin, other);
                } else {
                    player.showPlayer(plugin, other);
                }
            });
        });
    }

}
