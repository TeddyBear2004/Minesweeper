package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.game.painter.Painter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsCommand implements TabExecutor {

    private static final String CUSTOM_RESOURCE_PACK_URL = "custom_resource_pack_url";
    private static final String QUICK_REVEAL_DURATION = "quick_reveal_duration";
    private static final String BOARD_STYLE = "board_style";
    private static final String ENABLE_QUESTION_MARK = "enable_question_mark";
    private static final String ENABLE_FLAG = "enable_flag";
    private static final String QUICK_REVEAL = "quick_reveal";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player) || args.length == 0) {
            return true;
        }

        PersonalModifier modifier = PersonalModifier.getPersonalModifier(player);
        switch(args[0].toLowerCase()){
            case CUSTOM_RESOURCE_PACK_URL -> {
                if (args.length == 1) {
                    modifier.setResourcePackUrl(null);
                    player.sendMessage(ChatColor.GREEN + "Disabled custom resource pack.");
                    break;
                }
                String url = args[1];
                if (!(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))) {
                    player.sendMessage(ChatColor.DARK_RED + "Please make sure that the url starts with http:// or https://");
                    return true;
                }
                modifier.setResourcePackUrl(url);
                player.sendMessage(ChatColor.GREEN + "The specified resource pack url was applied.");
            }
            case QUICK_REVEAL_DURATION -> {
                if (args.length == 1) {
                    modifier.setDoubleClickDuration(null);
                    player.sendMessage(ChatColor.GREEN + "Disabled custom double click duration.");
                    break;
                }
                try{
                    int number = Integer.parseInt(args[1]);

                    modifier.setDoubleClickDuration(number);
                    player.sendMessage(ChatColor.GREEN + "The specified double click duration was applied.");
                }catch(NumberFormatException e){
                    player.sendMessage(ChatColor.DARK_RED + "Please make sure that you provide a valid number.");
                }
            }
            case BOARD_STYLE -> {
                if (args.length == 1) {
                    modifier.setPainterClass(null);
                    player.sendMessage(ChatColor.GREEN + "Disabled custom painter.");
                    break;
                }
                for (Class<? extends Painter> aClass : Game.PAINTER_MAP.keySet()) {
                    if (aClass.getSimpleName().equalsIgnoreCase(args[1])) {
                        modifier.setPainterClass(aClass.getName());
                        player.sendMessage(ChatColor.GREEN + "The specified painter was applied.");
                        return true;
                    }
                }
                player.sendMessage(ChatColor.DARK_RED + "Please make sure the provided painter is valid.");
            }
            case ENABLE_QUESTION_MARK -> {
                if (args.length == 1) {
                    modifier.setEnableQuestionMark(null);
                    player.sendMessage(ChatColor.GREEN + "Disabled custom second mark configuration.");
                    break;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.setEnableQuestionMark(true);
                    player.sendMessage(ChatColor.GREEN + "Enabled second mark.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.setEnableQuestionMark(false);
                    player.sendMessage(ChatColor.GREEN + "Disabled second mark.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case ENABLE_FLAG -> {
                if (args.length == 1) {
                    modifier.setEnableMarks(null);
                    player.sendMessage(ChatColor.GREEN + "Disabled custom mark configuration.");
                    break;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.setEnableMarks(true);
                    player.sendMessage(ChatColor.GREEN + "Enabled marks.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.setEnableMarks(false);
                    player.sendMessage(ChatColor.GREEN + "Disabled marks.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case QUICK_REVEAL -> {
                if (args.length == 1) {
                    modifier.setEnableDoubleClick(null);
                    player.sendMessage(ChatColor.GREEN + "Disabled custom double click configuration.");
                    break;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.setEnableDoubleClick(true);
                    player.sendMessage(ChatColor.GREEN + "Enabled double click.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.setEnableDoubleClick(false);
                    player.sendMessage(ChatColor.GREEN + "Disabled double click.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> strings = new ArrayList<>();

        if (args.length == 0)
            return strings;

        if (args.length == 1) {
            Arrays.asList(CUSTOM_RESOURCE_PACK_URL, QUICK_REVEAL_DURATION, BOARD_STYLE, ENABLE_QUESTION_MARK, ENABLE_FLAG, QUICK_REVEAL).forEach(s -> {
                if (s.startsWith(args[0]))
                    strings.add(s);
            });
        } else {
            switch(args[0]){
                case CUSTOM_RESOURCE_PACK_URL:
                    break;
                case QUICK_REVEAL_DURATION:
                    strings.add("350");
                    break;
                case BOARD_STYLE:
                    Game.PAINTER_MAP.keySet().forEach(aClass -> {
                        if (aClass.getSimpleName().toLowerCase().startsWith(args[1].toLowerCase()))
                            strings.add(aClass.getSimpleName());
                    });
                    break;
                case ENABLE_QUESTION_MARK, QUICK_REVEAL, ENABLE_FLAG:
                    if ("true".startsWith(args[1].toLowerCase()))
                        strings.add("true");
                    if ("false".startsWith(args[1].toLowerCase()))
                        strings.add("false");
                    break;
            }
        }

        return strings;
    }

}
