package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.Inventories;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
            if(args.length >= 1){
                if(args[0].equals("12a34b56c78d90")){
                    Game.MAP_SPECIAL.startGame((Player)sender);
                    return true;
                }
            }

            ((Player)sender).openInventory(Inventories.startCommandInventory);
        }

        return true;
    }
}
