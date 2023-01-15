package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.game.texture.pack.ResourcePackHandler;
import de.teddy.minesweeper.util.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SettingsCommand implements TabExecutor {

    private final ResourcePackHandler packHandler;
    private final Language language;

    public SettingsCommand(ResourcePackHandler packHandler, Language language) {
        this.packHandler = packHandler;
        this.language = language;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player) || args.length == 0) {
            return true;
        }

        PersonalModifier modifier = PersonalModifier.getPersonalModifier(player);

        for (PersonalModifier.ModifierType value : PersonalModifier.ModifierType.values()) {
            if (language.getString(value.getLangReference()).equalsIgnoreCase(args[0])) {
                value.performAction(value, player, modifier, args[1], language, packHandler);
                break;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        List<String> strings = new ArrayList<>();

        if (args.length == 0)
            return strings;

        if (args.length == 1) {
            PersonalModifier.ModifierType.getLangReferences().forEach(s -> {
                String languageString = language.getString(s);

                if (languageString.startsWith(args[0]))
                    strings.add(languageString);
            });
        } else if (args.length == 2) {
            for (PersonalModifier.ModifierType value : PersonalModifier.ModifierType.values()) {
                if (language.getString(value.getLangReference()).equalsIgnoreCase(args[0])) {
                    value.fillList(args[1], strings, language);
                    break;
                }
            }
        }

        return strings;
    }

}
