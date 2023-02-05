package de.teddybear2004.retro.games.commands;

import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.inventory.generator.ViewGamesGenerator;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
import de.teddybear2004.retro.games.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record MinesweeperCommand(GameManager manager, Game customGame, Language language,
                                 InventoryManager inventoryManager) implements TabExecutor {

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

        if (args.length == 0) {
            player.openInventory(inventoryManager.getInventory(ViewGamesGenerator.class, player));
            return true;
        }

        if (args[0].equalsIgnoreCase(language.getString("custom"))) {
            if (customGame == null) {
                player.sendMessage(ChatColor.DARK_RED + language.getString("error_custom_game_unavailable"));
                return true;
            }

            if (args.length < 3) {
                player.sendMessage(ChatColor.DARK_RED + language.getString("minesweeper_command_custom_completion", label));
                return true;
            }

            if (!player.hasPermission("minesweeper.minesweeper.custom")) {
                player.sendMessage(ChatColor.DARK_RED + language.getString("error_no_permission"));
                return true;
            }
            int w, h, bombCount;

            int i = args[2].toLowerCase().indexOf("x");
            String s1 = args[2].substring(0, i);
            String s2 = args[2].substring(i + 1);


            try{
                bombCount = Integer.parseInt(args[1]);
                w = Integer.parseInt(s1);
                h = Integer.parseInt(s2);
            }catch(NumberFormatException e){
                player.sendMessage(ChatColor.DARK_RED + language.getString("error_no_valid_number"));
                return true;

            }

            try{
                Game.Builder builder = customGame
                        .getStarter()
                        .setBombCount(bombCount)
                        .setWidth(w)
                        .setHeight(h);

                if (args.length != 3)
                    builder
                            .setSeed(Long.parseLong(args[3]))
                            .setSetSeed(true);

                builder.build(player, MinesweeperBoard.class);
            }catch(IllegalArgumentException e){
                player.sendMessage(ChatColor.DARK_RED + language.getString("error_bomb_size_not_valid"));
            }
            return true;
        }

        for (Game game : manager.getGames()) {
            if (game.getDifficulty().replaceAll(" ", "_").equalsIgnoreCase(args[0])) {
                if (args.length == 2) {
                    try{
                        game.getStarter()
                                .setBombCount(Integer.parseInt(args[1]))
                                .build(player, MinesweeperBoard.class);
                    }catch(NumberFormatException e){
                        player.sendMessage(language.getString("error_no_valid_number"));
                    }catch(IllegalArgumentException e){
                        player.sendMessage(language.getString("error_bomb_size_too_big"));
                    }
                } else if (args.length > 2) {
                    try{
                        int bombCount = Integer.parseInt(args[1]);
                        if (bombCount <= 0) {
                            player.sendMessage(ChatColor.DARK_RED + language.getString("error_bomb_size_too_small"));
                            return true;
                        }
                        game.getStarter()
                                .setBombCount(bombCount)
                                .setSeed(Long.parseLong(args[2]))
                                .setSetSeed(true)
                                .setSaveStats(false)
                                .build(player, MinesweeperBoard.class);
                    }catch(NumberFormatException ignored){
                        player.sendMessage(language.getString("error_no_valid_number"));
                    }catch(IllegalArgumentException e){
                        player.sendMessage(language.getString("error_bomb_size_too_big"));
                    }
                } else
                    game.getStarter().build(player, MinesweeperBoard.class);
                break;
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
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        List<String> strings = new ArrayList<>();
        if (args.length == 1) {
            manager.getGames().forEach(game -> {
                String s = game.getDifficulty().replaceAll(" ", "_");
                if (s.toLowerCase().startsWith(args[0].toLowerCase()))
                    strings.add(s);
            });

            if (customGame != null)
                strings.add(language.getString("custom"));
        } else if (args[0].equalsIgnoreCase(language.getString("custom"))) {
            if (args.length == 2) {
                List.of("10", "20", "30", "40", "80", "100", "150", "200", "250", "350", "500").forEach(s -> {
                    if (s.startsWith(args[1]))
                        strings.add(s);
                });
            } else if (args.length == 3) {
                List.of("4x4", "10x10", "24x24", "30x30", "40x40", "45x45").forEach(s -> {
                    if (s.startsWith(args[2]))
                        strings.add(s);
                });
            }
        }
        return strings;
    }

}
