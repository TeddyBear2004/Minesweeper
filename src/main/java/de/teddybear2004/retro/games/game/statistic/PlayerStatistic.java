package de.teddybear2004.retro.games.game.statistic;

import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.util.Language;
import de.teddybear2004.retro.games.util.Time;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;

public class PlayerStatistic {

    private static final DecimalFormat df = new DecimalFormat("0.000");
    private final List<GameStatistic> stats;
    private final List<Game> games;
    private final @NotNull Map<Game, MapStatistic> statsPerMap;

    public PlayerStatistic(List<GameStatistic> stats, List<Game> games) {
        this.stats = stats;
        this.games = games;
        this.statsPerMap = new HashMap<>();

        this.calculate();
    }

    private void calculate() {
        Map<Game, List<GameStatistic>> gameStatisticMap = new HashMap<>();

        this.stats.forEach(gameStatistic -> games.forEach(game -> {
            if (!gameStatistic.getMap().equals(game.getMap()) || gameStatistic.getBombCount() != game.getBombCount())
                return;

            gameStatisticMap.putIfAbsent(game, new ArrayList<>());
            gameStatisticMap.get(game).add(gameStatistic);
        }));

        gameStatisticMap.forEach((game, gameStatistic) -> this.statsPerMap.put(game, new MapStatistic(gameStatistic)));
    }

    public @NotNull Inventory generateInventory(Language language) {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.AQUA + "RetroGames Stats");

        games.forEach(game -> {
            if (game.getInventoryPosition() == -1)
                return;

            MapStatistic mapStatistic = statsPerMap.get(game);

            if (mapStatistic == null)
                mapStatistic = new MapStatistic(Collections.emptyList());

            ItemStack itemStack = new ItemStack(game.getItemStack().getType());
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                itemMeta.setDisplayName(language.getString(game.getDifficulty()));

                if (mapStatistic.getStarted() != 0)
                    itemMeta.setLore(List.of(
                            ChatColor.GRAY + "Personal best time: "
                                    + ChatColor.YELLOW + Time.parse(true, mapStatistic.getBestDuration()),
                            ChatColor.GRAY + "Average time: "
                                    + ChatColor.YELLOW + Time.parse(true, mapStatistic.getAverageDuration()),
                            ChatColor.GRAY + "Total time played: "
                                    + ChatColor.YELLOW + Time.parse(true, mapStatistic.getTotalDuration()),
                            ChatColor.GRAY + "Win rate: "
                                    + ChatColor.YELLOW + df.format(mapStatistic.getWinRate() * 100) + "%",
                            ChatColor.GRAY + "Times won: "
                                    + ChatColor.YELLOW + mapStatistic.getFinished(),
                            ChatColor.GRAY + "Times started: "
                                    + ChatColor.YELLOW + mapStatistic.getStarted()
                    ));
                else itemMeta.setLore(Collections.singletonList(ChatColor.RED + "Never played."));
            }

            itemStack.setItemMeta(itemMeta);

            inventory.setItem(game.getInventoryPosition(), itemStack);
        });

        return inventory;
    }

}
