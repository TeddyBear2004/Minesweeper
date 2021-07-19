package de.teddy.minesweeper.commands;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.teddy.minesweeper.util.PacketUtil;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScoreTest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(sender instanceof Player){
            PacketUtil.sendSoundEffect(((Player)sender), Sound.BLOCK_STONE_PLACE, EnumWrappers.SoundCategory.BLOCKS, new BlockPosition(((Player)sender).getLocation().toVector()), 0.5F, 0);
        }

        return true;
    }
}
