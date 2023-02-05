package de.teddybear2004.retro.games.scheduler;

import de.teddybear2004.retro.games.util.PacketUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RemoveMarkerScheduler extends BukkitRunnable {

    private final Map<Player, Long> playerIntegerMap = new HashMap<>();

    public void add(Player player, int duration) {
        playerIntegerMap.put(player, System.currentTimeMillis() + duration);
    }

    @Override
    public void run() {
        Set<Player> playerSet = new HashSet<>();

        playerIntegerMap.forEach((player, aLong) -> {
            if (aLong < System.currentTimeMillis()) {
                playerSet.add(player);
                PacketUtil.removeBlockHighlights(player);
            }
        });

        playerSet.forEach(playerIntegerMap::remove);
    }

}
