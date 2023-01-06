package de.teddy.minesweeper.scheduler;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
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

            boolean hidePlayer = modifier.isHidePlayer().orElse(false);
            double distance = modifier.getHidePlayerDistance().orElse((double) 3);
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
