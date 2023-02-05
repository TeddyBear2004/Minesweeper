package de.teddybear2004.retro.games.commands;

import de.teddybear2004.retro.games.game.CustomGame;
import de.teddybear2004.retro.games.game.DuelGame;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.event.DuelLeaveEvent;
import de.teddybear2004.retro.games.util.Language;
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
    private final Language language;

    public DuelCommand(Plugin plugin, GameManager gameManager, Language language) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.language = language;

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (args.length == 0)
            return false;

        if (args[0].equalsIgnoreCase(language.getString("duel_invite"))) {
            if (!builderMap.containsKey(player))
                builderMap.put(player, new DuelGame.Builder(player, plugin));

            if (args.length > 1) {
                Player player1 = Bukkit.getPlayer(args[1]);
                if (player1 == null)
                    return true;

                if (builderMap.get(player).invite(player1)) {
                    player1.sendMessage(language.getString("send_invite_player", player.getName()));
                    builderMap.get(player).broadcast(language.getString("broadcast_invite", player1.getName()));
                }
            }
        } else if (args[0].equalsIgnoreCase(language.getString("duel_accept"))) {
            if (args.length > 1) {
                Player player1 = Bukkit.getPlayer(args[1]);
                if (player1 == null)
                    return true;

                DuelGame.Builder builder = builderMap.get(player1);

                if (builder != null) {
                    if (builder.accept(player)) {
                        builder.broadcast(language.getString("broadcast_accept", player.getName()));
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase(language.getString("duel_leave"))) {
            if (args.length > 1) {
                Player player1 = Bukkit.getPlayer(args[1]);

                if (player1 == null) {
                    builderMap.forEach((player2, builder) -> {
                        if (builder.kick(player)) {
                            player.sendMessage(language.getString("send_leave", player.getName()));
                            builder.broadcast(language.getString("broadcast_leave", player.getName()));
                        }
                    });
                    return true;
                }

                DuelGame.Builder builder = builderMap.get(player1);

                if (builder != null) {
                    if (builder.kick(player)) {
                        DuelLeaveEvent event = new DuelLeaveEvent(player);
                        Bukkit.getServer().getPluginManager().callEvent(event);
                        player1.sendMessage(language.getString("send_leave", player.getName()));
                        builder.broadcast(language.getString("broadcast_leave", player1.getName()));
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase(language.getString("duel_kick"))) {
            if (args.length > 1) {
                Player player1 = Bukkit.getPlayer(args[1]);

                if (player1 == null)
                    return true;

                DuelGame.Builder builder = builderMap.get(player);

                if (builder != null) {
                    if (builder.kick(Bukkit.getPlayer(args[1]))) {
                        DuelLeaveEvent event = new DuelLeaveEvent(player);
                        Bukkit.getServer().getPluginManager().callEvent(event);

                        player1.sendMessage(language.getString("send_kick", player.getName()));
                        builder.broadcast(language.getString("broadcast_kick", player1.getName()));
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase(language.getString("duel_game"))) {
            if (!builderMap.containsKey(player))
                builderMap.put(player, new DuelGame.Builder(player, plugin));

            for (Game game : gameManager.getGames()) {
                if (game instanceof CustomGame)
                    continue;

                if (args[1].equalsIgnoreCase(game.getMap())) {
                    DuelGame.Builder builder = builderMap.get(player);
                    builder.setGame(game);
                    builder.broadcast(language.getString("broadcast_game", game.getMap()));
                    break;
                }
            }
        } else if (args[0].equalsIgnoreCase(language.getString("duel_start"))) {
            if (builderMap.containsKey(player)) {
                DuelGame.Builder builder = builderMap.get(player);

                if (builder.build(gameManager.getGames().get(0)).startGame())
                    builder.broadcast(language.getString("broadcast_start"));
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
            Arrays.asList(language.getString("duel_invite"), language.getString("duel_accept"), language.getString("duel_leave"), language.getString("duel_kick"), language.getString("duel_game"), language.getString("duel_start")).forEach(s -> {
                if (s.toLowerCase().startsWith(args[0]))
                    strings.add(s);
            });
        } else if (args.length > 1) {
            if (args[0].equalsIgnoreCase(language.getString("duel_invite"))) {
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
            } else if (args[0].equalsIgnoreCase(language.getString("duel_accept"))) {
                for (Player player1 : builderMap.keySet()) {
                    DuelGame.Builder builder = builderMap.get(player1);

                    if (builder.getInvited().contains(player) && player1.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        strings.add(player1.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase(language.getString("duel_leave"))) {
                for (Player player1 : builderMap.keySet()) {
                    DuelGame.Builder builder = builderMap.get(player1);

                    if (builder.getAll().contains(player) && player1.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        strings.add(player1.getName());
                    }
                }
            } else if (args[0].equalsIgnoreCase(language.getString("duel_kick"))) {
                DuelGame.Builder builder = builderMap.get(player);

                if (builder != null)
                    Bukkit.getOnlinePlayers().forEach(player1 -> {
                        if (player1.getName().toLowerCase().startsWith(args[1].toLowerCase()) && player != player1)
                            strings.add(player1.getName());
                    });
            } else if (args[0].equalsIgnoreCase(language.getString("duel_game"))) {
                gameManager.getGames().forEach(game -> {
                    if (game.getMap().toLowerCase().startsWith(args[1].toLowerCase()))
                        strings.add(game.getMap());
                });
            }
        }

        return strings;
    }

}
