package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record MinesweeperCommand(List<Game> games, Game customGame) implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (args.length == 0) {
            player.openInventory(Inventories.CHOOSE_GAME.getInventory());
            return true;
        }

        if (args[0].equalsIgnoreCase("custom")) {
            if (customGame == null) {
                player.sendMessage(ChatColor.DARK_RED + "Custom game is currently unavailable.");
                return true;
            }

            if (args.length < 3) {
                player.sendMessage(ChatColor.DARK_RED + "/" + label + " custom [Bomb Count] [WidthXHeight] <Seed>");
                return true;
            }

            if (!player.hasPermission("minesweeper.minesweeper.custom")) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have the permission to execute this command.");
                return true;
            }
            int w, h, bombCount;

            try{
                bombCount = Integer.parseInt(args[1]);
            }catch(NumberFormatException e){
                player.sendMessage(ChatColor.DARK_RED + "Please use a whole numbers as bomb count.");
                return true;

            }
            int i = args[2].toLowerCase().indexOf("x");
            String s1 = args[2].substring(0, i);
            String s2 = args[2].substring(i + 1);

            try{
                w = Integer.parseInt(s1);
                h = Integer.parseInt(s2);
            }catch(NumberFormatException e){
                player.sendMessage(ChatColor.DARK_RED + "Please use a whole numbers as width and height.");
                return true;
            }
            try{
                if (args.length == 3) {
                    customGame.getStarter()
                            .setBombCount(bombCount)
                            .setWidth(w)
                            .setHeight(h)
                            .build(player);
                } else {
                    customGame.getStarter()
                            .setBombCount(bombCount)
                            .setWidth(w)
                            .setHeight(h)
                            .setSeed(Long.parseLong(args[3]))
                            .setSetSeed(true)
                            .build(player);
                }
            }catch(IllegalArgumentException e){
                player.sendMessage(ChatColor.DARK_RED + "The bomb size is not valid for the given map size.");
            }
            return true;
        }

        for (Game game : games) {
            if (game.getDifficulty().replaceAll(" ", "_").equalsIgnoreCase(args[0])) {
                if (args.length == 2) {
                    try{
                        game.getStarter()
                                .setBombCount(Integer.parseInt(args[1]))
                                .build(player);
                    }catch(NumberFormatException e){
                        player.sendMessage("Please write a whole number as second argument.");
                    }catch(IllegalArgumentException e){
                        player.sendMessage("The provided bomb count is too large.");
                    }
                } else if (args.length > 2) {
                    try{
                        int bombCount = Integer.parseInt(args[1]);
                        if (bombCount <= 0) {
                            player.sendMessage(ChatColor.DARK_RED + "The number of bombs must be greater than 0.");
                            return true;
                        }
                        game.getStarter()
                                .setBombCount(bombCount)
                                .setSeed(Long.parseLong(args[2]))
                                .setSetSeed(true)
                                .build(player);
                    }catch(NumberFormatException ignored){
                        player.sendMessage("Please write a whole number as second and third argument.");
                    }catch(IllegalArgumentException e){
                        player.sendMessage("The provided bomb count is too large.");
                    }
                } else
                    game.getStarter().build(player);
                break;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> strings = new ArrayList<>();
        if (args.length == 1) {
            games.forEach(game -> {
                String s = game.getDifficulty().replaceAll(" ", "_");
                if (s.toLowerCase().startsWith(args[0].toLowerCase()))
                    strings.add(s);
            });

            if (customGame != null)
                strings.add("custom");
        } else if (args[0].equalsIgnoreCase("custom")) {
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
