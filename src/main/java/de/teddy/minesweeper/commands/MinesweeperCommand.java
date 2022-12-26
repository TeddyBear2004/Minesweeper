package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
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
                game.startGame(player, true);
                break;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> strings = new ArrayList<>();
        games.forEach(game -> strings.add(game.getDifficulty().replaceAll(" ", "_")));
        return strings;
    }

}
