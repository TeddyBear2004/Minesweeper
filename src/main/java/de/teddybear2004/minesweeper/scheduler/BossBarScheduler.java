package de.teddybear2004.minesweeper.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.function.Supplier;

public class BossBarScheduler extends BukkitRunnable implements Listener {

    private static final int MAX_LENGTH = 30;

    private final List<Supplier<String>> stringSuppliers;
    private final BossBar bossBar;
    private int index = 0;

    public BossBarScheduler(List<Supplier<String>> stringSuppliers) {
        this.stringSuppliers = stringSuppliers;
        this.bossBar = Bukkit.createBossBar(generateString(), BarColor.PURPLE, BarStyle.SOLID);
        this.bossBar.setProgress(1);
    }

    private String generateString() {
        StringBuilder builder = new StringBuilder();
        for (Supplier<String> supplier : stringSuppliers) {
            builder.append(supplier.get()).append(" | ");
        }

        builder.delete(builder.length() - 3, builder.length());
        return builder.toString();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.bossBar.addPlayer(event.getPlayer());
    }

    @Override
    public void run() {
        String title = generateString();

        String newTitle = title.substring(index, Math.min(index + MAX_LENGTH, generateString().length()));
        index++;

        if (title.length() != 0 && newTitle.length() == 0) {
            newTitle = title.substring(0, Math.min(MAX_LENGTH, generateString().length()));
            index = 0;
        }

        this.bossBar.setTitle(ChatColor.AQUA + newTitle);
    }

}
