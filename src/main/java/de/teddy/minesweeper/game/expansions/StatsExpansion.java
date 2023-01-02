package de.teddy.minesweeper.game.expansions;

import de.teddy.minesweeper.game.statistic.GameStatistic;
import de.teddy.minesweeper.util.ConnectionBuilder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StatsExpansion extends PlaceholderExpansion {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss:SSS");

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

        if (map.length != 2)
            return null;

        try{
            GameStatistic gameStatistic = GameStatistic.retrieveNthPerMap(connectionBuilder,
                                                                          map[0],
                                                                          Integer.parseInt(map[1]),
                                                                          Integer.parseInt(args[1]));

            return args[1] + ". " + gameStatistic.getName() + ": " + SIMPLE_DATE_FORMAT.format(new Date(gameStatistic.getDuration()));


        }catch(NumberFormatException e){
            return null;
        }
    }

}
