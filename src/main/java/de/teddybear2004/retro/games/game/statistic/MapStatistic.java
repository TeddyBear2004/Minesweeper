package de.teddybear2004.retro.games.game.statistic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapStatistic {

    private final List<GameStatistic> statistics;
    private long bestDuration;
    private long averageDuration;
    private long totalDuration;
    private double winRate;
    private int started;
    private int finished;

    public MapStatistic(List<GameStatistic> statistics) {
        this.statistics = statistics;

        this.calculate();
    }

    public double getWinRate() {
        return winRate;
    }

    public long getBestDuration() {
        return bestDuration;
    }

    public long getAverageDuration() {
        return averageDuration;
    }

    public int getStarted() {
        return started;
    }

    public int getFinished() {
        return finished;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    private void calculate() {
        long bestDuration = Integer.MAX_VALUE;
        long totalDuration = 0;
        long totalDurationWin = 0;
        int finished = 0;
        int started = 0;

        for (GameStatistic statistic : this.statistics) {
            if (statistic.isSetSeed())
                continue;

            totalDuration += statistic.getDuration();

            started++;
            if (statistic.isWon()) {
                finished++;
                totalDurationWin += statistic.getDuration();

                if (statistic.getDuration() < bestDuration)
                    bestDuration = statistic.getDuration();
            }

        }

        this.bestDuration = bestDuration == Integer.MAX_VALUE ? 0 : bestDuration;
        this.totalDuration = totalDuration;
        this.started = started;
        this.finished = finished;
        this.averageDuration = finished != 0 ? totalDurationWin / finished : 0;
        this.winRate = (double) finished / (double) started;
    }

    public @NotNull Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(null, 45, ChatColor.AQUA + "RetroGames Stats");

        for (int i = 0; i < statistics.size(); i++) {
            try{
                inventory.setItem(i, statistics.get(i).getItemStack(i));
            }catch(Exception e){
                inventory.setItem(i, new ItemStack(Material.BARRIER));
            }
        }

        return inventory;
    }

    @Override
    public @NotNull String toString() {
        return "MapStatistic{" +
                "  bestDuration=" + bestDuration +
                ", averageDuration=" + averageDuration +
                ", totalDuration=" + totalDuration +
                ", winRate=" + winRate +
                ", started=" + started +
                ", finished=" + finished +
                '}';
    }

}
