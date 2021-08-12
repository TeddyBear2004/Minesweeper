package de.teddy.minesweeper.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetResourcePack implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
            ((Player)sender).setResourcePack("https://cdn.discordapp.com/attachments/676083915382849576/875367487334649876/Default.zip");
        }
        return true;
    }
}
