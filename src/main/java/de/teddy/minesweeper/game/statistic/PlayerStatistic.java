package de.teddy.minesweeper.game.statistic;

import de.teddy.minesweeper.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerStatistic {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss:SSS");
    private static final DecimalFormat df = new DecimalFormat("#.000");
    private final List<GameStatistic> stats;
    private final List<Game> games;
    private final Map<Game, MapStatistic> statsPerMap;

    public PlayerStatistic(List<GameStatistic> stats, List<Game> games) {
        this.stats = stats;
        this.games = games;
        this.statsPerMap = new HashMap<>();

        this.calculate();
    }

    private void calculate() {
        Map<Game, List<GameStatistic>> gameStatisticMap = new HashMap<>();

        this.stats.forEach(gameStatistic -> games.forEach(game -> {
            if (!gameStatistic.getMap().equals(game.getMap()))
                return;

            gameStatisticMap.putIfAbsent(game, new ArrayList<>());
            gameStatisticMap.get(game).add(gameStatistic);
        }));

        gameStatisticMap.forEach((game, gameStatistic) -> this.statsPerMap.put(game, new MapStatistic(gameStatistic)));
    }

    public Inventory generateInventory() {
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.AQUA + "Minesweeper Stats");

        games.forEach(game -> {
            if (game.getInventoryPosition() == -1)
                return;

            MapStatistic mapStatistic = statsPerMap.get(game);

            if (mapStatistic == null)
                mapStatistic = new MapStatistic(Collections.emptyList());

            ItemStack itemStack = new ItemStack(game.getItemStack().getType());
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                itemMeta.setDisplayName(game.getDifficulty());

                if (mapStatistic.getStarted() != 0)
                    itemMeta.setLore(List.of(
                            ChatColor.GRAY + "Personal best time: "
                                    + ChatColor.YELLOW + SIMPLE_DATE_FORMAT.format(new Date(mapStatistic.getBestDuration())),
                            ChatColor.GRAY + "Average time: "
                                    + ChatColor.YELLOW + SIMPLE_DATE_FORMAT.format(new Date(mapStatistic.getAverageDuration())),
                            ChatColor.GRAY + "Total time played: "
                                    + ChatColor.YELLOW + SIMPLE_DATE_FORMAT.format(new Date(mapStatistic.getTotalDuration())),
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
