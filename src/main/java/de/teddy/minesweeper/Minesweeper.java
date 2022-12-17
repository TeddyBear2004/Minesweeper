package de.teddy.minesweeper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.moandjiezana.toml.Toml;
import de.teddy.minesweeper.commands.ResetResourcePack;
import de.teddy.minesweeper.commands.StartCommand;
import de.teddy.minesweeper.events.GenericEvents;
import de.teddy.minesweeper.events.GenericRightClickEvent;
import de.teddy.minesweeper.events.InventoryClickEvents;
import de.teddy.minesweeper.events.packets.LeftClickEvent;
import de.teddy.minesweeper.events.packets.RightClickEvent;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.util.Language;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Minesweeper extends JavaPlugin {

    private static List<Game> games = new ArrayList<>();
    private static JavaPlugin plugin;
    private static Language language;
    private final String langPath = "lang/" + getConfig().getString("language") + ".toml";

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static List<Game> getGames() {
        return games;
    }

    public static Language getLanguage() {
        return language;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource(langPath, false);


        Minesweeper.plugin = this;
        Minesweeper.language = loadLanguage();
        loadWorld();
        Minesweeper.games = loadGames();
        Inventories.initialise();


        Objects.requireNonNull(this.getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(this.getCommand("resetResourcePack")).setExecutor(new ResetResourcePack());

        getServer().getPluginManager().registerEvents(new GenericEvents(), this);
        getServer().getPluginManager().registerEvents(new GenericRightClickEvent(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickEvents(), this);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new RightClickEvent());
        protocolManager.addPacketListener(new LeftClickEvent());

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().setContents(Inventories.VIEWER_INVENTORY);
            if (Minesweeper.getGames().size() != 0)
                games.get(0).startViewing(player, null);
        });
    }

    private Language loadLanguage() {
        File file = new File(getDataFolder(), langPath);
        if (!file.exists()) {
            getLogger().warning("Language file not found! Using default language file!");
            saveResource("lang/en_US", false);
            file = new File(getDataFolder(), "lang/en_US");
        }

        return new Language(new Toml().read(file));
    }

    private void loadWorld() {
        if (!getConfig().getBoolean("use_default_map"))
            return;

        File minesweeper = new File("MineSweeper");
        File[] files = minesweeper.listFiles();
        if (minesweeper.exists()
                && minesweeper.isDirectory()
                && files != null
                && files.length != 0) {
            WorldCreator.name("MineSweeper").type(WorldType.FLAT).createWorld();
            return;
        }

        try(ZipInputStream zis = new ZipInputStream(Objects.requireNonNull(getResource("MineSweeper.zip")))){
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(".", zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    if (!newFile.exists() && !newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.exists() && !parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    if (!newFile.isDirectory() && !newFile.exists())
                        if (!newFile.createNewFile())
                            throw new IOException("Failed to create file " + newFile);
                    try(FileOutputStream fos = new FileOutputStream(newFile)){
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

            }
        }catch(IOException e){
            e.printStackTrace();
        }

        WorldCreator.name("MineSweeper").type(WorldType.FLAT).createWorld();
    }

    private List<Game> loadGames() {
        List<Game> gameList = new ArrayList<>();

        ConfigurationSection games = getConfig().getConfigurationSection("games");
        if (games == null)
            return gameList;

        games.getKeys(false).forEach(key -> {
            ConfigurationSection cornerSection = games.getConfigurationSection(key + ".corner");
            assert cornerSection != null;
            Location corner = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(cornerSection.getString("world")))), cornerSection.getInt("x"), cornerSection.getInt("y"), cornerSection.getInt("z"));

            ConfigurationSection spawnSection = games.getConfigurationSection(key + ".spawn");
            assert spawnSection != null;
            Location spawn = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(spawnSection.getString("world")))), spawnSection.getInt("x"), spawnSection.getInt("y"), spawnSection.getInt("z"), spawnSection.getInt("yaw"), spawnSection.getInt("pitch"));

            int borderSize = games.getInt(key + ".border_size");
            int bombCount = games.getInt(key + ".bomb_count");
            String difficultyLangPath = games.getString(key + ".difficulty_lang_path");

            Material material = Material.valueOf(games.getString(key + ".inventory_material"));
            int inventoryPosition = games.getInt(key + ".inventory_position");

            Game game = new Game(corner, spawn, borderSize, bombCount, Minesweeper.language.getString(difficultyLangPath), material, inventoryPosition);
            gameList.add(game);
        });
        return gameList;
    }


}
