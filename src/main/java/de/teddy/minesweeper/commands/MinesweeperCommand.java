package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public record MinesweeperCommand(List<Game> games) implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (args.length == 0) {
            player.openInventory(Inventories.CHOOSE_GAME.getInventory());
            return true;
        }

        for (Game game : games) {
            if (game.getDifficulty().replaceAll(" ", "_").equals(args[0])) {
                if (args.length == 2) {
                    try{
                        game.startGame(player, true, Integer.parseInt(args[1]));
                    }catch(NumberFormatException e){
                        player.sendMessage("Please write a whole number as second argument.");
                    }catch(IllegalArgumentException e){
                        player.sendMessage("The provided bomb count is too large.");
                    }
                } else if (args.length > 2) {
                    try{
                        int bombCount = Integer.parseInt(args[1]);
                        if(bombCount <= 0){
                            player.sendMessage(ChatColor.DARK_RED + "The number of bombs must be greater than 0.");
                            return true;
                        }
                        game.startGame(player, true, bombCount, Long.parseLong(args[2]));
                    }catch(NumberFormatException ignored){
                        player.sendMessage("Please write a whole number as second and third argument.");
                    }catch(IllegalArgumentException e){
                        player.sendMessage("The provided bomb count is too large.");
                    }
                } else
                    game.startGame(player, true);
                break;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> strings = new ArrayList<>();
        if (args.length == 1)
            games.forEach(game -> {
                String s = game.getDifficulty().replaceAll(" ", "_");
                if (s.toLowerCase().startsWith(args[0].toLowerCase()))
                    strings.add(s);
            });
        return strings;
    }

}
