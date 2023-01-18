package de.teddybear2004.minesweeper.commands;

import de.teddybear2004.minesweeper.game.DuelGame;
import de.teddybear2004.minesweeper.game.Game;
import de.teddybear2004.minesweeper.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DuelCommand implements TabExecutor, Listener {

    private final Map<Player, DuelGame.Builder> builderMap = new HashMap<>();
    private final Plugin plugin;
    private final GameManager gameManager;

    public DuelCommand(Plugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (args.length == 0)
            return false;

        switch(args[0].toLowerCase()){
            case "invite" -> {
                if (!builderMap.containsKey(player))
                    builderMap.put(player, new DuelGame.Builder(player, plugin));

                if (args.length > 1) {
                    Player player1 = Bukkit.getPlayer(args[1]);
                    if (player1 == null)
                        return true;

                    if (builderMap.get(player).invite(player1)) {
                        player1.sendMessage("Du wurdest von " + player.getName() + " zu einem Duell eingeladen. Gebe /duel accept " + player.getName() + " ein um diesem beizutreten.");
                        builderMap.get(player).broadcast(player1.getName() + " wurde eingeladen.");
                    }
                }
            }
            case "accept" -> {
                if (args.length > 1) {
                    Player player1 = Bukkit.getPlayer(args[1]);
                    if (player1 == null)
                        return true;

                    DuelGame.Builder builder = builderMap.get(player1);

                    if (builder != null) {
                        if (builder.accept(player)) {
                            builder.broadcast(player1.getName() + " ist dem Duell beigetreten.");
                        }
                    }
                }
            }
            case "leave" -> {
                if (args.length > 1) {
                    Player player1 = Bukkit.getPlayer(args[1]);

                    if (player1 == null)
                        return true;

                    DuelGame.Builder builder = builderMap.get(player1);

                    if (builder != null) {
                        if (builder.kick(player)) {
                            player1.sendMessage("Du hast das Duell von " + player.getName() + " verlassen.");
                            builder.broadcast(player1.getName() + " hat das Duell verlassen.");
                        }
                    }
                }
            }
            case "kick" -> {
                if (args.length > 1) {
                    Player player1 = Bukkit.getPlayer(args[1]);

                    if (player1 == null)
                        return true;

                    DuelGame.Builder builder = builderMap.get(player);

                    if (builder != null) {
                        if (builder.kick(Bukkit.getPlayer(args[1]))) {
                            player1.sendMessage("Du wurdest aus dem Duell von " + player.getName() + " gekickt.");
                            builder.broadcast(player1.getName() + " hat das Duell verlassen.");
                        }
                    }
                }
            }
            case "game" -> {
                if (!builderMap.containsKey(player))
                    builderMap.put(player, new DuelGame.Builder(player, plugin));

                for (Game game : gameManager.getGames()) {
                    if (args[1].equalsIgnoreCase(game.getMap())) {
                        DuelGame.Builder builder = builderMap.get(player);
                        builder.setGame(game);
                        builder.broadcast("Das Duell findet nun auf der Map " + game.getMap() + " statt.");
                        break;
                    }
                }
            }
            case "start" -> {
                if (builderMap.containsKey(player)) {
                    DuelGame.Builder builder = builderMap.get(player);
                    builder.build(gameManager.getGames().get(0)).startGame();
                    builder.broadcast("Das Duell hat gestartet. Viel Glück!");
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        builderMap.remove(player);

        builderMap.values().forEach(builder -> builder.kick(player));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> strings = new ArrayList<>();

        if (!(sender instanceof Player player))
            return strings;

        if (args.length == 1) {
            Arrays.asList("invite", "accept", "leave", "kick", "start", "game").forEach(s -> {
                if (s.toLowerCase().startsWith(args[0]))
                    strings.add(s);
            });
        } else if (args.length > 1) {
            switch(args[0].toLowerCase()){
                case "invite" -> {
                    DuelGame.Builder builder = builderMap.get(player);

                    if (builder == null)
                        Bukkit.getOnlinePlayers().forEach(player1 -> {
                            if (player1.getName().toLowerCase().startsWith(args[1].toLowerCase()) && player != player1) {
                                strings.add(player1.getName());
                            }
                        });

                    else Bukkit.getOnlinePlayers().forEach(player1 -> {
                        if (player1.getName().toLowerCase().startsWith(args[1].toLowerCase()) && player != player1 && !builder.getAll().contains(player1)) {
                            strings.add(player1.getName());
                        }
                    });
                }
                case "accept" -> {
                    for (Player player1 : builderMap.keySet()) {
                        DuelGame.Builder builder = builderMap.get(player1);

                        if (builder.getInvited().contains(player) && player1.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            strings.add(player1.getName());
                        }
                    }
                }
                case "leave" -> {
                    for (Player player1 : builderMap.keySet()) {
                        DuelGame.Builder builder = builderMap.get(player1);

                        if (builder.getAll().contains(player) && player1.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            strings.add(player1.getName());
                        }
                    }
                }
                case "kick" -> {
                    DuelGame.Builder builder = builderMap.get(player);

                    if (builder != null)
                        Bukkit.getOnlinePlayers().forEach(player1 -> {
                            if (player1.getName().toLowerCase().startsWith(args[1].toLowerCase()) && player != player1)
                                strings.add(player1.getName());
                        });
                }
                case "game" -> gameManager.getGames().forEach(game -> {
                    if (game.getMap().toLowerCase().startsWith(args[1].toLowerCase()))
                        strings.add(game.getMap());
                });
            }
        }

        return strings;
    }

}
