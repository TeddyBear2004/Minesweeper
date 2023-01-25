package de.teddybear2004.minesweeper.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionBuilder {

    private final String host;
    private final String port;
    private final String user;
    private final String password;
    private final String database;

    public ConnectionBuilder(String host, String port, String user, String password, String database) throws SQLException {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;


        try(Connection connection = getConnection()){
            connection.prepareStatement("create table if not exists minesweeper_stats " +
                                                "( " +
                                                "    id         int auto_increment, " +
                                                "    start      long        null, " +
                                                "    duration   long        null, " +
                                                "    uuid       varchar(36) null, " +
                                                "    bomb_count int         null, " +
                                                "    map        tinytext    null, " +
                                                "    set_seed   varchar(1)  null, " +
                                                "    seed       long        null, " +
                                                "    x          int         null, " +
                                                "    y          int         null," +
                                                "    won        varchar(1)  null,  " +
                                                "    constraint table_name_pk " +
                                                "        primary key (id) " +
                                                ");")
                    .execute();
        }

    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", host, port, database, user, password));
    }

}
