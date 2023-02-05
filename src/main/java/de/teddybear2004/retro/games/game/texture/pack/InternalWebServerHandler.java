package de.teddybear2004.retro.games.game.texture.pack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;

public class InternalWebServerHandler implements ResourcePackHandler {

    private final HttpServer server;
    private final @NotNull String host;
    private final int port;
    private final @NotNull File file;

    public InternalWebServerHandler(@NotNull String host, int port, @NotNull File file) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException();

        this.host = host;
        this.port = port;
        this.file = file;
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.server.createContext("/", new FileHandler());
        this.server.setExecutor(null);
    }

    private static byte[] readFile(@NotNull File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        int read = is.read(buffer, 0, buffer.length);
        is.close();

        return read == -1 ? new byte[0] : buffer;
    }

    public void start() {
        this.server.start();
    }

    @Override
    public void apply(@NotNull Player player) {
        player.setResourcePack(getUrl());
    }

    public @NotNull String getUrl() {
        return "http://" + this.host + ":" + this.port + "/";
    }


    @Override
    public void close() {
        server.stop(0);
    }

    private class FileHandler implements HttpHandler {

        @Override
        public void handle(@NotNull HttpExchange exchange) throws IOException {
            byte[] response = readFile(file);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

    }

}