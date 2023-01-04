package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddy.minesweeper.game.texture.pack.ResourcePackHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    private static final String HIDE_PLAYER = "hide_player";
    private static final String HIDE_PLAYER_DISTANCE = "hide_player_distance";
    private static final String REVEAL_ON_DOUBLE_CLICK = "reveal_on_double_click";
    private static final String USE_MULTI_FLAG = "use_multi_flag";
    private final ResourcePackHandler packHandler;

    public SettingsCommand(ResourcePackHandler packHandler) {
        this.packHandler = packHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player) || args.length == 0) {
            return true;
        }

        PersonalModifier modifier = PersonalModifier.getPersonalModifier(player);
        switch(args[0].toLowerCase()){
            case CUSTOM_RESOURCE_PACK_URL -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Your current resource pack url is: " + modifier.getResourcePackUrl().orElse("default"));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setResourcePackUrl(null);
                    return true;
                }
                String url = args[1];
                if (!(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))) {
                    player.sendMessage(ChatColor.DARK_RED + "Please make sure that the url starts with http:// or https://");
                    return true;
                }
                modifier.setResourcePackUrl(url);
                player.sendMessage(ChatColor.GREEN + "The specified resource pack url was applied.");
                packHandler.apply(player);
            }
            case QUICK_REVEAL_DURATION -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Your current quick reveal duration (time between double clicks) is: " + (modifier.getDoubleClickDuration().isPresent() ? modifier.getDoubleClickDuration().get() : "default"));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setDoubleClickDuration(null);
                    return true;
                }
                try{
                    int number = Integer.parseInt(args[1]);

                    modifier.setDoubleClickDuration(number);
                    player.sendMessage(ChatColor.GREEN + "The specified quick reveal duration was applied.");
                }catch(NumberFormatException e){
                    player.sendMessage(ChatColor.DARK_RED + "Please make sure that you provide a valid number.");
                }
            }
            case BOARD_STYLE -> {
                if (args.length == 1) {
                    if (modifier.getPainterClass().isPresent()) {
                        try{
                            Painter painter = Game.PAINTER_MAP.get(Class.forName(modifier.getPainterClass().get()));

                            player.sendMessage(ChatColor.GREEN + "Your current board style is: " + painter.getName());
                        }catch(ClassNotFoundException e){
                            player.sendMessage(ChatColor.DARK_RED + "An unknown error has occurred.");
                        }
                    } else
                        player.sendMessage(ChatColor.GREEN + "Your current board style is: " + modifier.getPainterClass().orElse("default"));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setPainterClass(null);
                    return true;
                }
                for (Painter painter : Game.PAINTER_MAP.values()) {
                    if (painter.getName().equalsIgnoreCase(args[1])) {
                        modifier.setPainterClass(painter.getClass().getName());
                        player.sendMessage(ChatColor.GREEN + "The specified board style was applied.");
                        return true;
                    }
                }
                player.sendMessage(ChatColor.DARK_RED + "Please make sure the provided board style is valid.");
            }
            case ENABLE_QUESTION_MARK -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently question marks are " + (modifier.isEnableQuestionMark().isPresent() ? modifier.isEnableQuestionMark().get() ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setEnableQuestionMark(null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.setEnableQuestionMark(true);
                    player.sendMessage(ChatColor.GREEN + "Enabled question mark.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.setEnableQuestionMark(false);
                    player.sendMessage(ChatColor.GREEN + "Disabled question mark.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case ENABLE_FLAG -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently flags are " + (modifier.isEnableMarks().isPresent() ? modifier.isEnableMarks().get() ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setEnableMarks(null);
                    return true;
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
                    player.sendMessage(ChatColor.GREEN + "Currently quick reveal is " + (modifier.isEnableDoubleClick().isPresent() ? modifier.isEnableDoubleClick().get() ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setEnableDoubleClick(null);
                    return true;
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
            case HIDE_PLAYER -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently hide player is " + (modifier.isEnableDoubleClick().isPresent() ? modifier.isEnableDoubleClick().get() ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setHidePlayer(null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.setHidePlayer(true);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: hide player.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.setHidePlayer(false);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: show player.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case HIDE_PLAYER_DISTANCE -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Your current hide player distance " + (modifier.getHidePlayerDistance().isPresent() ? modifier.getHidePlayerDistance().get() : "default"));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setDoubleClickDuration(null);
                    return true;
                }
                try{
                    double number = Double.parseDouble(args[1]);

                    modifier.setHidePlayerDistance(number);
                    player.sendMessage(ChatColor.GREEN + "The specified hide player distance was applied.");
                }catch(NumberFormatException e){
                    player.sendMessage(ChatColor.DARK_RED + "Please make sure that you provide a valid number.");
                }
            }
            case REVEAL_ON_DOUBLE_CLICK -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently reveal on double click " + (modifier.isRevealOnDoubleClick().isPresent() ? modifier.isRevealOnDoubleClick().get() ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setRevealOnDoubleClick(null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.setRevealOnDoubleClick(true);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: reveal on double click.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.setRevealOnDoubleClick(false);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: don't reveal on double click.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case USE_MULTI_FLAG -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently multi flag is " + (modifier.isUseMultiFlag().isPresent() ? modifier.isUseMultiFlag().get() ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.setUseMultiFlag(null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.setUseMultiFlag(true);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: enabled multi flag.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.setUseMultiFlag(false);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: disabled multi flag.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> strings = new ArrayList<>();

        if (!(sender instanceof Player player))
            return strings;

        if (args.length == 0)
            return strings;

        if (args.length == 1) {
            Arrays.asList(CUSTOM_RESOURCE_PACK_URL, QUICK_REVEAL_DURATION, BOARD_STYLE, ENABLE_QUESTION_MARK, ENABLE_FLAG, QUICK_REVEAL, HIDE_PLAYER, HIDE_PLAYER_DISTANCE, REVEAL_ON_DOUBLE_CLICK, USE_MULTI_FLAG).forEach(s -> {
                if (s.startsWith(args[0]))
                    strings.add(s);
            });
        } else if (args.length == 2) {
            switch(args[0]){
                case CUSTOM_RESOURCE_PACK_URL -> {
                    String url = packHandler.getUrl(player);
                    if (url != null)
                        strings.add(url);
                    if ("default".startsWith(args[1].toLowerCase()))
                        strings.add("default");
                }
                case BOARD_STYLE -> {
                    Game.PAINTER_MAP.values().forEach(painter -> {
                        if (painter.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                            strings.add(painter.getName());

                    });
                    if ("default".startsWith(args[1].toLowerCase()))
                        strings.add("default");
                }
                case QUICK_REVEAL_DURATION, HIDE_PLAYER_DISTANCE -> {
                    if ("default".startsWith(args[1].toLowerCase()))
                        strings.add("default");
                }
                case ENABLE_QUESTION_MARK, QUICK_REVEAL, ENABLE_FLAG, HIDE_PLAYER, REVEAL_ON_DOUBLE_CLICK, USE_MULTI_FLAG -> {
                    if ("true".startsWith(args[1].toLowerCase()))
                        strings.add("true");
                    if ("false".startsWith(args[1].toLowerCase()))
                        strings.add("false");
                    if ("default".startsWith(args[1].toLowerCase()))
                        strings.add("default");
                }
            }
        }

        return strings;
    }

}
