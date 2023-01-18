package de.teddybear2004.minesweeper.commands;

import de.teddybear2004.minesweeper.game.CustomGame;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.statistic.GameStatistic;
import de.teddybear2004.minesweeper.game.statistic.MapStatistic;
import de.teddybear2004.minesweeper.game.statistic.PlayerStatistic;
import de.teddybear2004.minesweeper.util.ConnectionBuilder;
import de.teddybear2004.minesweeper.util.Language;
import de.teddybear2004.minesweeper.util.UUIDConverter;
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

    private final GameManager gameManager;
    private final ConnectionBuilder connectionBuilder;
    private final Language language;

    /**
     * @param gameManager       The game manager this stats should build from.
     * @param connectionBuilder A connection builder to execute queries from.
     * @param language          A language class to load strings from.
     */
    public MineStatsCommand(GameManager gameManager, ConnectionBuilder connectionBuilder, Language language) {
        this.gameManager = gameManager;
        this.connectionBuilder = connectionBuilder;
        this.language = language;
    }

    /**
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player))
            return true;


        if (args.length == 0 || args[0].equalsIgnoreCase(language.getString("player")) || !args[0].equalsIgnoreCase(language.getString("game"))) {
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
                player.sendMessage(ChatColor.DARK_RED + language.getString("error_not_determine_player_name"));
                return true;
            }

            PlayerStatistic playerStatistic = new PlayerStatistic(GameStatistic.retrieve(connectionBuilder, uuid), gameManager.getGames());
            player.openInventory(playerStatistic.generateInventory());
        } else {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.DARK_RED + language.getString("error_specify_map"));
                return true;
            }

            for (Game game : gameManager.getGames()) {
                if (!(game instanceof CustomGame) && game.getDifficulty().equalsIgnoreCase(args[1])) {
                    MapStatistic mapStatistic = new MapStatistic(GameStatistic.retrieveTopPerMap(connectionBuilder, game.getMap(), game.getBombCount(), 45));

                    player.openInventory(mapStatistic.getInventory());
                    break;
                }
            }
        }


        return true;
    }

    /**
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        List<String> strings = new ArrayList<>();

        if (args.length == 1) {
            List.of(language.getString("player"), language.getString("game")).forEach(s -> {
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

            if (f.equalsIgnoreCase(language.getString("player"))) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    String name = player.getName();

                    if (name.toLowerCase().startsWith(s))
                        strings.add(name);
                });
            } else if (f.equalsIgnoreCase(language.getString("game"))) {
                gameManager.getGames().forEach(game -> {
                    if (game instanceof CustomGame)
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
