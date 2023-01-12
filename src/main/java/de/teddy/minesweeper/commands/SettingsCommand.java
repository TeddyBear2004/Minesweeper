package de.teddy.minesweeper.commands;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.Board;
import de.teddy.minesweeper.game.GameManager;
import de.teddy.minesweeper.game.modifier.PersonalModifier;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddy.minesweeper.game.texture.pack.ResourcePackHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
                    player.sendMessage(ChatColor.GREEN + "Your current resource pack url is: " + modifier.get(PersonalModifier.ModifierType.RESOURCE_PACK_URL).orElse("default"));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.RESOURCE_PACK_URL, null);
                    return true;
                }
                String url = args[1];
                if (!(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))) {
                    player.sendMessage(ChatColor.DARK_RED + "Please make sure that the url starts with http:// or https://");
                    return true;
                }
                modifier.set(PersonalModifier.ModifierType.RESOURCE_PACK_URL, url);
                player.sendMessage(ChatColor.GREEN + "The specified resource pack url was applied.");
                packHandler.apply(player);
            }
            case QUICK_REVEAL_DURATION -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Your current quick reveal duration (time between double clicks) is: " + (modifier.get(PersonalModifier.ModifierType.DOUBLE_CLICK_DURATION).isPresent() ? modifier.<Integer>get(PersonalModifier.ModifierType.DOUBLE_CLICK_DURATION).orElse(0) : "default"));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.DOUBLE_CLICK_DURATION, null);
                    return true;
                }
                try{
                    int number = Integer.parseInt(args[1]);

                    modifier.set(PersonalModifier.ModifierType.DOUBLE_CLICK_DURATION, number);
                    player.sendMessage(ChatColor.GREEN + "The specified quick reveal duration was applied.");
                }catch(NumberFormatException e){
                    player.sendMessage(ChatColor.DARK_RED + "Please make sure that you provide a valid number.");
                }
            }
            case BOARD_STYLE -> {
                if (args.length == 1) {
                    Optional<String> painterClass = modifier.get(PersonalModifier.ModifierType.PAINTER_CLASS);
                    if (painterClass.isPresent()) {
                        try{
                            Painter painter = Painter.PAINTER_MAP.get(Class.forName(painterClass.get()));

                            player.sendMessage(ChatColor.GREEN + "Your current board style is: " + painter.getName());
                        }catch(ClassNotFoundException e){
                            player.sendMessage(ChatColor.DARK_RED + "An unknown error has occurred.");
                        }
                    } else
                        player.sendMessage(ChatColor.GREEN + "Your current board style is: " + painterClass.orElse("default"));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    GameManager gameManager = Minesweeper.getPlugin(Minesweeper.class).getGameManager();
                    Board board = gameManager.getBoard(player);
                    if (board == null) {
                        board = gameManager.getBoardWatched(player);
                    }

                    if (board != null) {
                        Painter painter = Painter.PAINTER_MAP.get(Painter.loadPainterClass(player));
                        painter.drawBlancField(board, Collections.singletonList(player));
                    }


                    modifier.set(PersonalModifier.ModifierType.PAINTER_CLASS, null);
                    Painter.storePainterClass(player.getPersistentDataContainer(), Painter.DEFAULT_PAINTER);


                    if (board != null)
                        Painter.getPainter(player).drawField(board, Collections.singletonList(player));

                    return true;
                }
                for (Painter painter : Painter.PAINTER_MAP.values()) {
                    if (painter.getName().equalsIgnoreCase(args[1])) {
                        player.sendMessage(ChatColor.GREEN + "The specified board style was applied.");

                        GameManager gameManager = Minesweeper.getPlugin(Minesweeper.class).getGameManager();
                        Board board = gameManager.getBoard(player);
                        if (board == null) {
                            board = gameManager.getBoardWatched(player);
                        }

                        if (board != null) {
                            Painter painter2 = Painter.PAINTER_MAP.get(Painter.loadPainterClass(player));
                            painter2.drawBlancField(board, Collections.singletonList(player));
                        }


                        modifier.set(PersonalModifier.ModifierType.PAINTER_CLASS, painter.getClass().getName());
                        Painter.storePainterClass(player.getPersistentDataContainer(), painter.getClass());


                        if (board != null)
                            painter.drawField(board, Collections.singletonList(player));

                        return true;
                    }
                }
                player.sendMessage(ChatColor.DARK_RED + "Please make sure the provided board style is valid.");
            }
            case ENABLE_QUESTION_MARK -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently question marks are " + (modifier.get(PersonalModifier.ModifierType.ENABLE_QUESTION_MARK).isPresent() ? modifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_QUESTION_MARK).orElse(false) ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_QUESTION_MARK, null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_QUESTION_MARK, true);
                    player.sendMessage(ChatColor.GREEN + "Enabled question mark.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_QUESTION_MARK, false);
                    player.sendMessage(ChatColor.GREEN + "Disabled question mark.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case ENABLE_FLAG -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently flags are " + (modifier.get(PersonalModifier.ModifierType.ENABLE_MARKS).isPresent() ? modifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_MARKS).orElse(true) ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_MARKS, null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_MARKS, true);
                    player.sendMessage(ChatColor.GREEN + "Enabled marks.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_MARKS, false);
                    player.sendMessage(ChatColor.GREEN + "Disabled marks.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case QUICK_REVEAL -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently quick reveal is " + (modifier.get(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK).isPresent() ? modifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK).orElse(false) ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK, null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK, true);
                    player.sendMessage(ChatColor.GREEN + "Enabled double click.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.set(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK, false);
                    player.sendMessage(ChatColor.GREEN + "Disabled double click.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case HIDE_PLAYER -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently hide player is " + (modifier.get(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK).isPresent() ? modifier.<Boolean>get(PersonalModifier.ModifierType.ENABLE_DOUBLE_CLICK).orElse(false) ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.HIDE_PLAYER, null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.set(PersonalModifier.ModifierType.HIDE_PLAYER, true);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: hide player.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.set(PersonalModifier.ModifierType.HIDE_PLAYER, false);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: show player.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case HIDE_PLAYER_DISTANCE -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Your current hide player distance " + (modifier.get(PersonalModifier.ModifierType.HIDE_PLAYER_DISTANCE).isPresent() ? modifier.get(PersonalModifier.ModifierType.HIDE_PLAYER_DISTANCE).get() : "default"));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.HIDE_PLAYER_DISTANCE, null);
                    return true;
                }
                try{
                    double number = Double.parseDouble(args[1]);

                    modifier.set(PersonalModifier.ModifierType.HIDE_PLAYER_DISTANCE, number);
                    player.sendMessage(ChatColor.GREEN + "The specified hide player distance was applied.");
                }catch(NumberFormatException e){
                    player.sendMessage(ChatColor.DARK_RED + "Please make sure that you provide a valid number.");
                }
            }
            case REVEAL_ON_DOUBLE_CLICK -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently reveal on double click " + (modifier.get(PersonalModifier.ModifierType.REVEAL_ON_DOUBLE_CLICK).isPresent() ? modifier.<Boolean>get(PersonalModifier.ModifierType.REVEAL_ON_DOUBLE_CLICK).orElse(false) ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.REVEAL_ON_DOUBLE_CLICK, null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.set(PersonalModifier.ModifierType.REVEAL_ON_DOUBLE_CLICK, true);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: reveal on double click.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.set(PersonalModifier.ModifierType.REVEAL_ON_DOUBLE_CLICK, false);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: don't reveal on double click.");
                    break;
                }
                player.sendMessage(ChatColor.DARK_RED + "No true or false could be found as an argument, so the command is ignored.");
            }
            case USE_MULTI_FLAG -> {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.GREEN + "Currently multi flag is " + (modifier.get(PersonalModifier.ModifierType.USE_MULTI_FLAG).isPresent() ? modifier.<Boolean>get(PersonalModifier.ModifierType.USE_MULTI_FLAG).orElse(false) ? "enabled." : "disabled." : "default setting."));
                    break;
                }
                if (args[1].equalsIgnoreCase("default")) {
                    modifier.set(PersonalModifier.ModifierType.USE_MULTI_FLAG, null);
                    return true;
                }
                if (args[1].equalsIgnoreCase("true")) {
                    modifier.set(PersonalModifier.ModifierType.USE_MULTI_FLAG, true);
                    player.sendMessage(ChatColor.GREEN + "Applied setting: enabled multi flag.");
                    break;
                }
                if (args[1].equalsIgnoreCase("false")) {
                    modifier.set(PersonalModifier.ModifierType.USE_MULTI_FLAG, false);
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
                    Painter.PAINTER_MAP.values().forEach(painter -> {
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
