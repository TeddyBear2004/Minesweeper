package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.CustomGame;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.statistic.GameStatistic;
import de.teddy.minesweeper.game.statistic.MapStatistic;
import de.teddy.minesweeper.game.statistic.PlayerStatistic;
import de.teddy.minesweeper.util.ConnectionBuilder;
import de.teddy.minesweeper.util.UUIDConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MineStatsCommand implements TabExecutor {

    private final List<Game> games;
    private final ConnectionBuilder connectionBuilder;

    public MineStatsCommand(List<Game> games, ConnectionBuilder connectionBuilder) {
        this.games = games;
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player))
            return true;


        if (args.length == 0 || args[0].equalsIgnoreCase("player") || !args[0].equalsIgnoreCase("game")) {
            String playerName = null;
            if (args.length >= 2)
                playerName = args[1];

            UUID uuid;

            if (playerName == null || playerName.isBlank()) {
                uuid = player.getUniqueId();
            } else {
                Player player1 = Bukkit.getPlayer(playerName);
                if (player1 == null)
                    uuid = UUIDConverter.getPlayerUUID(playerName);
                else
                    uuid = player1.getUniqueId();
            }

            if (uuid == null) {
                player.sendMessage(ChatColor.DARK_RED + "Could not determine the player by its name. Please try again later.");
                return true;
            }

            PlayerStatistic playerStatistic = new PlayerStatistic(GameStatistic.retrieve(connectionBuilder, uuid), games);
            player.openInventory(playerStatistic.generateInventory());
        } else {
            if (args.length < 2){
                sender.sendMessage(ChatColor.DARK_RED + "Please specify a map.");
                return true;
            }

            for (Game game : games) {
                if (!(game instanceof CustomGame) && game.getDifficulty().equalsIgnoreCase(args[1])) {
                    MapStatistic mapStatistic = new MapStatistic(GameStatistic.retrieveTopPerMap(connectionBuilder, game.getMap(), game.getBombCount(), 45));

                    player.openInventory(mapStatistic.getInventory());
                    break;
                }
            }
        }


        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> strings = new ArrayList<>();

        if (args.length == 1) {
            List.of("player", "game").forEach(s -> {
                if (s.startsWith(args[0].toLowerCase()))
                    strings.add(s);
            });

            if (strings.size() == 0) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    String name = player.getName();

                    if (name.toLowerCase().startsWith(args[0].toLowerCase()))
                        strings.add(name);
                });
            }
        } else if (args.length == 2) {
            String f = args[0].toLowerCase();
            String s = args[1].toLowerCase();

            if (f.equalsIgnoreCase("player")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    String name = player.getName();

                    if (name.toLowerCase().startsWith(s))
                        strings.add(name);
                });
            } else if (f.equalsIgnoreCase("game")) {
                games.forEach(game -> {
                    if(game instanceof CustomGame)
                        return;

                    String id = game.getDifficulty();

                    if (id.startsWith(s)) {
                        strings.add(id);
                    }
                });
            }
        }

        return strings;
    }

}
