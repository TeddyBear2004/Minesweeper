package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.inventory.Inventories;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
            ((Player)sender).openInventory(Inventories.CHOOSE_GAME.getInventory());
        }

        return true;
    }
}
