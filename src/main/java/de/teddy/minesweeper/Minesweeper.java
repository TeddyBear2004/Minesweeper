package de.teddy.minesweeper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.moandjiezana.toml.Toml;
import de.teddy.minesweeper.commands.BypassEventCommand;
import de.teddy.minesweeper.events.CancelableEvents;
import de.teddy.minesweeper.events.GenericEvents;
import de.teddy.minesweeper.events.GenericRightClickEvent;
import de.teddy.minesweeper.events.InventoryClickEvents;
import de.teddy.minesweeper.events.packets.LeftClickEvent;
import de.teddy.minesweeper.events.packets.RightClickEvent;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.temporary.Area;
import de.teddy.minesweeper.game.temporary.AreaSettings;
import de.teddy.minesweeper.game.texture.pack.DisableResourceHandler;
import de.teddy.minesweeper.game.texture.pack.ExternalWebServerHandler;
import de.teddy.minesweeper.game.texture.pack.InternalWebServerHandler;
import de.teddy.minesweeper.game.texture.pack.ResourcePackHandler;
import de.teddy.minesweeper.util.Language;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Minesweeper extends JavaPlugin {

    private static List<Game> games = new ArrayList<>();
    private static List<Area> areas = new ArrayList<>();
    private static AreaSettings areaSettings;
    private static JavaPlugin plugin;
    private static Language language;
    private static ResourcePackHandler resourcePackHandler;
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

    public static List<Area> getAreas() {
        return areas;
    }

    public static AreaSettings getAreaSettings() {
        return areaSettings;
    }

    public static ResourcePackHandler getTexturePackHandler() {
        return resourcePackHandler;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource(langPath, false);


        Minesweeper.plugin = this;
        Minesweeper.language = loadLanguage();
        loadWorld();
        Minesweeper.games = loadGames();
        Minesweeper.areas = loadAreas();
        Minesweeper.areaSettings = loadAreaSettings();

        try{
            Minesweeper.resourcePackHandler = loadTexturePackHandler(getConfig().getConfigurationSection("resource_pack"));
        }catch(FileNotFoundException e){
            getLogger().severe("Could not find the corresponding resource pack file. Please check the config.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }catch(IOException e){
            getLogger().severe("Could not start the internal web server. Please check the config.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Inventories.initialise();


        Objects.requireNonNull(this.getCommand("bypassEventCancellation")).setExecutor(new BypassEventCommand());

        getServer().getPluginManager().registerEvents(new CancelableEvents(getConfig().getConfigurationSection("events")), this);
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

    @Override
    public void onDisable() {
        try{
            resourcePackHandler.close();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
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


    private List<Area> loadAreas() {
        ConfigurationSection locationBased = getConfig().getConfigurationSection("location_based");
        List<Area> list = new ArrayList<>();

        if (locationBased == null || !locationBased.getBoolean("enable", false))
            return list;

        List<Map<?, ?>> areas1 = locationBased.getMapList("areas");

        areas1.forEach(map -> list.add(new Area(map)));

        return list;
    }

    private AreaSettings loadAreaSettings() {
        ConfigurationSection locationBased = getConfig().getConfigurationSection("location_based");

        if (locationBased == null || !locationBased.getBoolean("enable", false))
            return new AreaSettings();

        ConfigurationSection actions = locationBased.getConfigurationSection("actions");
        if (actions == null)
            return new AreaSettings();

        return new AreaSettings(actions);
    }

    private ResourcePackHandler loadTexturePackHandler(ConfigurationSection section) throws IOException {
        if (section == null)
            return new DisableResourceHandler();

        ConfigurationSection internalWebServer = section.getConfigurationSection("internal_web_server");
        if (internalWebServer != null && internalWebServer.getBoolean("enable", false)) {
            InternalWebServerHandler internalWebServerHandler = new InternalWebServerHandler(internalWebServer.getString("domain", "localhost"),
                                                                                             internalWebServer.getInt("port", 27565),
                                                                                             new File(getDataFolder(), internalWebServer.getString("name", "pack.zip")));

            internalWebServerHandler.start();

            return internalWebServerHandler;
        }

        ConfigurationSection externalWebServer = section.getConfigurationSection("external_web_server");
        if (externalWebServer != null && externalWebServer.getBoolean("enable", false)) {
            return new ExternalWebServerHandler(externalWebServer.getString("link"));
        }

        return new DisableResourceHandler();
    }

}
