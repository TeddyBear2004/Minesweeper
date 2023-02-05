package de.teddybear2004.retro.games.commands;

import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.inventory.generator.game.manager.GameManagerGenerator;
import de.teddybear2004.retro.games.util.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameManagerCommand implements CommandExecutor {

    private final GameManager gameManager;
    private final InventoryManager inventoryManager;
    private final Language language;

    public GameManagerCommand(GameManager gameManager, InventoryManager inventoryManager, Language language) {
        this.gameManager = gameManager;
        this.inventoryManager = inventoryManager;
        this.language = language;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            player.openInventory(inventoryManager.getInventory(GameManagerGenerator.class, player));
        }
        return true;
    }

}
