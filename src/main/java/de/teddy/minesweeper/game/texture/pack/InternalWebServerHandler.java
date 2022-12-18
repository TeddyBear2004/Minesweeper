package de.teddy.minesweeper.game.texture.pack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.InetSocketAddress;

public class InternalWebServerHandler implements ResourcePackHandler {

    private final HttpServer server;
    private final String host;
    private final int port;
    private final File file;

    public InternalWebServerHandler(String host, int port, File file) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException();

        this.host = host;
        this.port = port;
        this.file = file;
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.server.createContext("/", new FileHandler());
        this.server.setExecutor(null);
    }

    private static byte[] readFile(File file) throws IOException {
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
    public void apply(Player player) {
        player.setResourcePack("http://" + this.host + ":" + this.port + "/");
    }


    @Override
    public void close() {
        server.stop(0);
    }

    private class FileHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] response = readFile(file);
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }

    }

}