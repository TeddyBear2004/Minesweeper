package de.teddy.minesweeper.game.statistic;

import de.teddy.minesweeper.util.ConnectionBuilder;
import de.teddy.minesweeper.util.HeadGenerator;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.profile.PlayerProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GameStatistic {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("mm:ss:SSS");

    private final String uuid;
    private final long start;
    private final long duration;
    private final int bombCount;
    private final String map;
    private final boolean setSeed;
    private final long seed;
    private final int x;
    private final int y;
    private final boolean won;

    public GameStatistic(String uuid, long start, long duration, int bombCount, String map, boolean setSeed, long seed, int x, int y, boolean won) {
        this.uuid = uuid;
        this.start = start;
        this.duration = duration;
        this.bombCount = bombCount;
        this.map = map;
        this.setSeed = setSeed;
        this.seed = seed;
        this.x = x;
        this.y = y;
        this.won = won;
    }

    public static List<GameStatistic> retrieve(ConnectionBuilder connectionBuilder, UUID uuid) {
        if (connectionBuilder == null)
            return new ArrayList<>();

        try(Connection connection = connectionBuilder.getConnection()){
            PreparedStatement preparedStatement
                    = connection.prepareStatement("SELECT * FROM minesweeper_stats WHERE uuid = ?");

            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();
            List<GameStatistic> list = new ArrayList<>();

            while (resultSet.next()) {
                list.add(new GameStatistic(
                        resultSet.getString("uuid"),
                        resultSet.getLong("start"),
                        resultSet.getLong("duration"),
                        resultSet.getInt("bomb_count"),
                        resultSet.getString("map"),
                        resultSet.getBoolean("set_seed"),
                        resultSet.getLong("seed"),
                        resultSet.getInt("x"),
                        resultSet.getInt("y"),
                        resultSet.getBoolean("won")
                ));
            }
            return list;
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static List<GameStatistic> retrieveTopPerMap(ConnectionBuilder connectionBuilder, String map, int bombCount, int number) {
        if (connectionBuilder == null)
            return new ArrayList<>();

        try(Connection connection = connectionBuilder.getConnection()){
            PreparedStatement preparedStatement
                    = connection.prepareStatement("SELECT * FROM minesweeper_stats WHERE map = ? and bomb_count = ? and won = 1 ORDER BY length(duration), duration LIMIT ?");

            preparedStatement.setString(1, map);
            preparedStatement.setObject(2, bombCount);
            preparedStatement.setObject(3, number);

            ResultSet resultSet = preparedStatement.executeQuery();
            List<GameStatistic> list = new ArrayList<>();

            while (resultSet.next()) {
                list.add(new GameStatistic(
                        resultSet.getString("uuid"),
                        resultSet.getLong("start"),
                        resultSet.getLong("duration"),
                        resultSet.getInt("bomb_count"),
                        resultSet.getString("map"),
                        resultSet.getBoolean("set_seed"),
                        resultSet.getLong("seed"),
                        resultSet.getInt("x"),
                        resultSet.getInt("y"),
                        resultSet.getBoolean("won")
                ));
            }
            return list;
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public long getDuration() {
        return duration;
    }

    public String getMap() {
        return map;
    }

    public boolean isSetSeed() {
        return setSeed;
    }

    public boolean isWon() {
        return won;
    }

    public void save(ConnectionBuilder connectionBuilder) {
        if (connectionBuilder == null)
            return;

        if (duration == 0)
            throw new RuntimeException("Duration cannot be null.");

        try(Connection connection = connectionBuilder.getConnection()){
            PreparedStatement preparedStatement
                    = connection.prepareStatement("INSERT INTO minesweeper_stats (start, duration, uuid, bomb_count, map, set_seed, seed, x, y, won) VALUE (?, ?, ?,  ?, ?, ?, ?, ?, ?, ?)");

            preparedStatement.setObject(1, start);
            preparedStatement.setObject(2, duration);
            preparedStatement.setString(3, uuid);
            preparedStatement.setObject(4, bombCount);
            preparedStatement.setString(5, map);
            preparedStatement.setObject(6, setSeed);
            preparedStatement.setObject(7, seed);
            preparedStatement.setObject(8, x);
            preparedStatement.setObject(9, y);
            preparedStatement.setObject(10, won);

            preparedStatement.execute();

        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public ItemStack getItemStack(int i) throws Exception {
        PlayerProfile playerProfile = HeadGenerator.getPlayerProfile(UUID.fromString(uuid));

        ItemStack itemStack = HeadGenerator.getHeadFromPlayerProfile(playerProfile);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName((i + 1) + ". " + playerProfile.getName());

            if (start != 0)
                itemMeta.setLore(List.of(
                        ChatColor.GRAY + "Duration: "
                                + ChatColor.YELLOW + SIMPLE_DATE_FORMAT.format(new Date(duration)),
                        ChatColor.GRAY + "Map Size: "
                                + ChatColor.YELLOW + map,
                        ChatColor.GRAY + "Bomb count: "
                                + ChatColor.YELLOW + bombCount
                ));
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public String toString() {
        return "GameStatistic{" +
                "uuid='" + uuid + '\'' +
                ", start=" + start +
                ", duration=" + duration +
                ", bombCount=" + bombCount +
                ", map='" + map + '\'' +
                ", setSeed=" + setSeed +
                ", seed=" + seed +
                ", x=" + x +
                ", y=" + y +
                ", won=" + won +
                '}';
    }

}
