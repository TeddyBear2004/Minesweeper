package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.events.CancelableEvents;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class BypassEventCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            PersistentDataContainer persistentDataContainer = player.getPersistentDataContainer();

            //the following will flip the bit so that 0->1 and 1->0
            byte b = persistentDataContainer.getOrDefault(CancelableEvents.BYPASS_EVENTS, PersistentDataType.BYTE, (byte) 0);
            b = (byte) ~b;
            b &= 0xff >> 7;

            persistentDataContainer.set(CancelableEvents.BYPASS_EVENTS, PersistentDataType.BYTE, b);
        }
        return true;
    }

}
