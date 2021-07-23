package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScoreTest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
    PacketUtil.sendParticleEffect(((Player)sender).getPlayer(), ((Player)sender).getLocation());
        }

        return true;
    }
}
