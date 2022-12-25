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
import de.teddy.minesweeper.game.modifier.Modifier;
import de.teddy.minesweeper.game.modifier.ModifierArea;
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

    private final String langPath = "lang/" + getConfig().getString("language") + ".toml";
    private ResourcePackHandler resourcePackHandler;
    private List<Game> games = new ArrayList<>();


    public List<Game> getGames() {
        return games;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource(langPath, false);


        Language language = loadLanguage();
        List<ModifierArea> modifierAreas = loadAreas();
        loadWorld();
        loadModifier(modifierAreas);
        this.games = loadGames(language);

        try{
            this.resourcePackHandler = loadTexturePackHandler(getConfig().getConfigurationSection("resource_pack"));
        }catch(FileNotFoundException e){
            getLogger().severe("Could not find the corresponding resource pack file. Please check the config.");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }catch(IOException e){
            getLogger().severe("Could not start the internal web server. Please check the config.");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Inventories.initialise(getConfig(), language);


        Objects.requireNonNull(this.getCommand("bypassEventCancellation")).setExecutor(new BypassEventCommand());

        getServer().getPluginManager().registerEvents(new CancelableEvents(getConfig().getConfigurationSection("events"), modifierAreas), this);
        getServer().getPluginManager().registerEvents(new GenericEvents(games, resourcePackHandler), this);
        getServer().getPluginManager().registerEvents(new GenericRightClickEvent(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickEvents(), this);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new RightClickEvent(this));
        protocolManager.addPacketListener(new LeftClickEvent(this));

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().setContents(Inventories.VIEWER_INVENTORY);
            if (games.size() != 0)
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

    private List<Game> loadGames(Language language) {
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

            Game game = new Game(this, gameList, language, corner, spawn, borderSize, bombCount, language.getString(difficultyLangPath), material, inventoryPosition);
            gameList.add(game);
        });
        return gameList;
    }


    private List<ModifierArea> loadAreas() {
        ConfigurationSection locationBased = getConfig().getConfigurationSection("location_based");
        List<ModifierArea> list = new ArrayList<>();

        if (locationBased == null || !locationBased.getBoolean("enable", false))
            return list;

        List<Map<?, ?>> areas1 = locationBased.getMapList("areas");

        areas1.forEach(map -> list.add(new ModifierArea(map)));

        return list;
    }

    private void loadModifier(List<ModifierArea> areas) {
        ConfigurationSection locationBased = getConfig().getConfigurationSection("location_based");

        if (locationBased == null || !locationBased.getBoolean("enable", false)) {
            Modifier.initialise(getConfig(), areas);
            return;
        }

        ConfigurationSection actions = locationBased.getConfigurationSection("actions");
        if (actions == null)
            Modifier.initialise(getConfig(), areas);
        else
            Modifier.initialise(getConfig(), actions, areas);
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
