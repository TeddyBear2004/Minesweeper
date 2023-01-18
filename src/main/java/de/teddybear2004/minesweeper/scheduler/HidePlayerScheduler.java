package de.teddybear2004.minesweeper.scheduler;

import de.teddybear2004.minesweeper.game.modifier.PersonalModifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HidePlayerScheduler extends BukkitRunnable {

    private final Plugin plugin;

    public HidePlayerScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PersonalModifier modifier = PersonalModifier.getPersonalModifier(player);

            boolean hidePlayer = modifier.<Boolean>get(PersonalModifier.ModifierType.HIDE_PLAYER).orElse(false);
            double distance = modifier.<Double>get(PersonalModifier.ModifierType.HIDE_PLAYER_DISTANCE).orElse(3.0);
            Location location = player.getLocation();

            player.getWorld().getPlayers().forEach(other -> {
                if (hidePlayer && location.distance(other.getLocation()) < distance) {
                    player.hidePlayer(plugin, other);
                } else {
                    player.showPlayer(plugin, other);
                }
            });
        });
    }

}
