package de.teddybear2004.minesweeper.game.expansions;

import de.teddybear2004.minesweeper.game.statistic.GameStatistic;
import de.teddybear2004.minesweeper.util.ConnectionBuilder;
import de.teddybear2004.minesweeper.util.Time;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class StatsExpansion extends PlaceholderExpansion {

    private final ConnectionBuilder connectionBuilder;

    public StatsExpansion(ConnectionBuilder connectionBuilder) {
        this.connectionBuilder = connectionBuilder;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "minestats";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TeddyBear_2004";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("\\.");

        if (args.length != 2) return null;

        String[] map = args[0].split("@");

        if (map.length == 2) {
            try{
                GameStatistic gameStatistic = GameStatistic.retrieveNthPerMap(connectionBuilder,
                                                                              map[0],
                                                                              Integer.parseInt(map[1]),
                                                                              Integer.parseInt(args[1]));

                if (gameStatistic == null)
                    return null;

                return args[1] + ". " + gameStatistic.getName() + ": " + Time.parse(false, gameStatistic.getDuration());


            }catch(NumberFormatException e){
                return null;
            }
        } else if (map.length == 3) {
            if (map[0].toLowerCase().startsWith("avg")) {
                try{
                    GameStatistic gameStatistic = GameStatistic.retrieveNthAveragePerMap(connectionBuilder,
                                                                                         map[1],
                                                                                         Integer.parseInt(map[2]),
                                                                                         Integer.parseInt(args[1]),
                                                                                         Integer.parseInt(map[0].substring(3)));

                    if (gameStatistic == null)
                        return null;

                    return args[1] + ". " + gameStatistic.getName() + ": " + Time.parse(false, gameStatistic.getDuration());
                }catch(NumberFormatException e){
                    return null;
                }
            }
        }

        return null;
    }

}
