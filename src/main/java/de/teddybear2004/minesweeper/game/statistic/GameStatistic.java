package de.teddybear2004.minesweeper.game.statistic;

import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.util.ConnectionBuilder;
import de.teddybear2004.minesweeper.util.HeadGenerator;
import de.teddybear2004.minesweeper.util.Time;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameStatistic {

    public static final NamespacedKey MAP_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "map");
    public static final NamespacedKey SEED_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "seed");
    public static final NamespacedKey X_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "x");
    public static final NamespacedKey Y_KEY = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "y");
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

    public static @NotNull List<GameStatistic> retrieve(@Nullable ConnectionBuilder connectionBuilder, @NotNull UUID uuid) {
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

    public static GameStatistic retrieveNthPerMap(@Nullable ConnectionBuilder connectionBuilder, String map, int bombCount, int n) {
        if (connectionBuilder == null)
            return null;

        try(Connection connection = connectionBuilder.getConnection()){
            PreparedStatement preparedStatement
                    = connection.prepareStatement("""
                                                          SELECT *
                                                          FROM minesweeper_stats s
                                                                   INNER JOIN (
                                                              SELECT uuid, MIN(CAST(duration AS INTEGER)) AS duration
                                                              FROM minesweeper_stats
                                                              WHERE map = ? AND bomb_count = ? AND won = 1
                                                              GROUP BY uuid
                                                          ) min_durations
                                                                              ON s.uuid = min_durations.uuid AND s.duration = min_durations.duration
                                                          WHERE s.map = ? AND s.bomb_count = ? AND s.won = 1
                                                          ORDER BY CAST(s.duration AS INTEGER)
                                                          LIMIT 1
                                                          OFFSET ?
                                                          """);

            preparedStatement.setString(1, map);
            preparedStatement.setObject(2, bombCount);
            preparedStatement.setString(3, map);
            preparedStatement.setObject(4, bombCount);
            preparedStatement.setObject(5, n - 1);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return new GameStatistic(
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
                );
            return null;
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    public static @NotNull List<GameStatistic> retrieveTopPerMap(@Nullable ConnectionBuilder connectionBuilder, String map, int bombCount, int number) {
        if (connectionBuilder == null)
            return new ArrayList<>();

        try(Connection connection = connectionBuilder.getConnection()){
            PreparedStatement preparedStatement
                    = connection.prepareStatement("""
                                                                                                        SELECT *
                                                                                                        FROM minesweeper_stats s
                                                                                                                 INNER JOIN (
                                                                                                            SELECT uuid, MIN(CAST(duration AS INTEGER)) AS duration
                                                              FROM minesweeper_stats
                                                              WHERE map = ? AND bomb_count = ? AND won = 1
                                                              GROUP BY uuid
                                                          ) min_durations
                                                                              ON s.uuid = min_durations.uuid AND s.duration = min_durations.duration
                                                          WHERE s.map = ? AND s.bomb_count = ? AND s.won = 1
                                                          ORDER BY CAST(s.duration AS INTEGER)
                                                          LIMIT ?
                                                          """);

            preparedStatement.setString(1, map);
            preparedStatement.setObject(2, bombCount);
            preparedStatement.setString(3, map);
            preparedStatement.setObject(4, bombCount);
            preparedStatement.setObject(5, number);

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

    public @Nullable String getName() {
        try{
            return HeadGenerator.getPlayerProfile(UUID.fromString(uuid)).getName();
        }catch(Exception e){
            return null;
        }
    }

    public long getDuration() {
        return duration;
    }

    public String getMap() {
        return map;
    }

    public int getBombCount() {
        return bombCount;
    }

    public boolean isSetSeed() {
        return setSeed;
    }

    public boolean isWon() {
        return won;
    }

    public void save(@Nullable ConnectionBuilder connectionBuilder) {
        if (connectionBuilder == null)
            return;

        if (duration == 0)
            return;


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

    public @NotNull ItemStack getItemStack(int i) throws Exception {
        PlayerProfile playerProfile = HeadGenerator.getPlayerProfile(UUID.fromString(uuid));

        ItemStack itemStack = HeadGenerator.getHeadFromPlayerProfile(playerProfile);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName((i + 1) + ". " + playerProfile.getName());

            if (start != 0) {
                PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
                persistentDataContainer.set(MAP_KEY, PersistentDataType.STRING, map);
                persistentDataContainer.set(SEED_KEY, PersistentDataType.LONG, seed);
                persistentDataContainer.set(X_KEY, PersistentDataType.INTEGER, x);
                persistentDataContainer.set(Y_KEY, PersistentDataType.INTEGER, y);

                itemMeta.setLore(List.of(
                        ChatColor.GRAY + "Duration: "
                                + ChatColor.YELLOW + Time.parse(true, duration),
                        ChatColor.GRAY + "Map Size: "
                                + ChatColor.YELLOW + map,
                        ChatColor.GRAY + "Bomb count: "
                                + ChatColor.YELLOW + bombCount,
                        ChatColor.YELLOW + "Click here to play it for yourself!"
                ));
            }
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public @NotNull String toString() {
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
