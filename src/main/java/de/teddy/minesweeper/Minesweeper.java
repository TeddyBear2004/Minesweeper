package de.teddy.minesweeper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.moandjiezana.toml.Toml;
import de.teddy.minesweeper.commands.BypassEventCommand;
import de.teddy.minesweeper.commands.MineStatsCommand;
import de.teddy.minesweeper.commands.MinesweeperCommand;
import de.teddy.minesweeper.commands.SettingsCommand;
import de.teddy.minesweeper.events.CancelableEvents;
import de.teddy.minesweeper.events.GenericEvents;
import de.teddy.minesweeper.events.GenericRightClickEvent;
import de.teddy.minesweeper.events.InventoryClickEvents;
import de.teddy.minesweeper.events.packets.LeftClickEvent;
import de.teddy.minesweeper.events.packets.RightClickEvent;
import de.teddy.minesweeper.game.CustomGame;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.GameManager;
import de.teddy.minesweeper.game.TutorialGame;
import de.teddy.minesweeper.game.click.ClickHandler;
import de.teddy.minesweeper.game.expansions.StatsExpansion;
import de.teddy.minesweeper.game.inventory.Inventories;
import de.teddy.minesweeper.game.modifier.Modifier;
import de.teddy.minesweeper.game.modifier.ModifierArea;
import de.teddy.minesweeper.game.painter.ArmorStandPainter;
import de.teddy.minesweeper.game.painter.BlockPainter;
import de.teddy.minesweeper.game.painter.Painter;
import de.teddy.minesweeper.game.texture.pack.DisableResourceHandler;
import de.teddy.minesweeper.game.texture.pack.ExternalWebServerHandler;
import de.teddy.minesweeper.game.texture.pack.InternalWebServerHandler;
import de.teddy.minesweeper.game.texture.pack.ResourcePackHandler;
import de.teddy.minesweeper.scheduler.HidePlayerScheduler;
import de.teddy.minesweeper.util.ConnectionBuilder;
import de.teddy.minesweeper.util.JarWalker;
import de.teddy.minesweeper.util.Language;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Minesweeper extends JavaPlugin {

    private final List<BukkitTask> tasks = new ArrayList<>();
    private String langPath;
    private ResourcePackHandler resourcePackHandler;
    private GameManager gameManager;

    public GameManager getGameManager() {
        return gameManager;
    }

    public @NotNull List<Game> getGames() {
        return gameManager.getGames();
    }

    @Override
    public void onDisable() {
        tasks.forEach(BukkitTask::cancel);
        try{
            if (resourcePackHandler != null)
                resourcePackHandler.close();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {
        langPath = "lang/" + getConfig().getString("language") + ".toml";
        try{
            JarWalker.walkResources(this.getClass(), "/lang", 1, path -> {
                String localeFileName = path.getFileName().toString();
                if (!localeFileName.toLowerCase().endsWith(".toml")) return;

                if (!Files.exists(getDataFolder().toPath().resolve("locales").resolve(localeFileName))) {
                    saveResource("lang/" + localeFileName, false);
                }
            });
        }catch(URISyntaxException | IOException e){
            throw new RuntimeException(e);
        }

        saveDefaultConfig();
        reloadConfig();
        List<Game> games = new ArrayList<>();

        this.gameManager = new GameManager(games);

        ClickHandler clickHandler = new ClickHandler();

        Painter.PAINTER_MAP.put(BlockPainter.class, new BlockPainter(Minesweeper.getPlugin(Minesweeper.class), clickHandler, gameManager));
        Painter.PAINTER_MAP.put(ArmorStandPainter.class, new ArmorStandPainter(Minesweeper.getPlugin(Minesweeper.class), clickHandler, gameManager));


        Language language = loadLanguage();
        List<ModifierArea> modifierAreas = loadAreas();
        loadWorld();
        loadModifier(modifierAreas);
        ConnectionBuilder connectionBuilder = loadConnectionBuilder(getConfig().getConfigurationSection("database"));

        loadGames(games, language, connectionBuilder, gameManager);
        Game customGame = loadCustomGame(getConfig().getConfigurationSection("custom_game"), connectionBuilder, gameManager, language);

        if (customGame != null)
            games.add(customGame);

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
        Inventories.initialise(getConfig().getInt("available_games_inventory_lines"), language, gameManager);


        Objects.requireNonNull(this.getCommand("bypassEventCancellation")).setExecutor(new BypassEventCommand());
        Objects.requireNonNull(this.getCommand("minesweeper")).setExecutor(new MinesweeperCommand(gameManager, customGame, language));
        Objects.requireNonNull(this.getCommand("settings")).setExecutor(new SettingsCommand(resourcePackHandler, language));
        Objects.requireNonNull(this.getCommand("minestats")).setExecutor(new MineStatsCommand(gameManager, connectionBuilder, language));

        getServer().getPluginManager().registerEvents(new CancelableEvents(getConfig().getConfigurationSection("events"), modifierAreas), this);
        getServer().getPluginManager().registerEvents(new GenericEvents(resourcePackHandler, customGame, gameManager), this);
        getServer().getPluginManager().registerEvents(new GenericRightClickEvent(gameManager), this);
        getServer().getPluginManager().registerEvents(new InventoryClickEvents(gameManager), this);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new RightClickEvent(this, gameManager));
        protocolManager.addPacketListener(new LeftClickEvent(this, gameManager));

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().setContents(Inventories.VIEWER_INVENTORY);
            if (games.size() != 0)
                games.get(0).startViewing(player, null);
        });

        tasks.add(new HidePlayerScheduler(this).runTaskTimer(this, 20, 5));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new StatsExpansion(connectionBuilder).register();
        }
    }

    private @NotNull Language loadLanguage() {
        File file = new File(getDataFolder(), langPath);
        if (!file.exists()) {
            getLogger().warning("Language file not found! Using default language file!");
            saveResource("lang/en_US", false);
            file = new File(getDataFolder(), "lang/en_US");
        }

        return new Language(new Toml().read(file));
    }

    private @NotNull List<ModifierArea> loadAreas() {
        ConfigurationSection locationBased = getConfig().getConfigurationSection("location_based");
        List<ModifierArea> list = new ArrayList<>();

        if (locationBased == null || !locationBased.getBoolean("enable", false))
            return list;

        List<Map<?, ?>> areas1 = locationBased.getMapList("areas");

        areas1.forEach(map -> list.add(new ModifierArea(map)));

        return list;
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
                    if (parent != null) {
                        if (!parent.exists() && !parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                    }

                    if (!newFile.getName().equals(".")) {
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

            }
        }catch(IOException e){
            e.printStackTrace();
        }

        WorldCreator.name("MineSweeper").type(WorldType.FLAT).createWorld();
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

    private ConnectionBuilder loadConnectionBuilder(@Nullable ConfigurationSection database) {
        if (database == null)
            return null;
        try{
            return new ConnectionBuilder(database.getString("host"),
                                         database.getString("port"),
                                         database.getString("user"),
                                         database.getString("password"),
                                         database.getString("database"));
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    private void loadGames(@NotNull List<Game> games, @NotNull Language language, ConnectionBuilder connectionBuilder, GameManager gameManager) {
        ConfigurationSection gameSection = getConfig().getConfigurationSection("games");
        if (gameSection == null)
            return;

        gameSection.getKeys(false).forEach(key -> {
            ConfigurationSection cornerSection = gameSection.getConfigurationSection(key + ".corner");
            assert cornerSection != null;
            Location corner = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(cornerSection.getString("world")))), cornerSection.getInt("x"), cornerSection.getInt("y"), cornerSection.getInt("z"));

            ConfigurationSection spawnSection = gameSection.getConfigurationSection(key + ".spawn");
            assert spawnSection != null;
            Location spawn = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(spawnSection.getString("world")))), spawnSection.getInt("x"), spawnSection.getInt("y"), spawnSection.getInt("z"), spawnSection.getInt("yaw"), spawnSection.getInt("pitch"));

            int borderSize = gameSection.getInt(key + ".border_size");
            int bombCount = gameSection.getInt(key + ".bomb_count");
            String difficultyLangPath = gameSection.getString(key + ".difficulty_lang_path");

            Material material = Material.valueOf(gameSection.getString(key + ".inventory_material"));
            int inventoryPosition = gameSection.getInt(key + ".inventory_position");

            Game game = new Game(this,
                                 gameManager,
                                 language,
                                 connectionBuilder,
                                 corner,
                                 spawn,
                                 borderSize,
                                 borderSize,
                                 bombCount,
                                 language.getString(difficultyLangPath),
                                 material,
                                 inventoryPosition);
            games.add(game);
        });
    }

    private Game loadCustomGame(@Nullable ConfigurationSection section, ConnectionBuilder connectionBuilder, GameManager gameManager, @NotNull Language language) {
        if (section == null || !section.getBoolean("enable", false))
            return null;

        ConfigurationSection cornerSection = section.getConfigurationSection("corner");
        assert cornerSection != null;
        Location corner = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(cornerSection.getString("world")))), cornerSection.getInt("x"), cornerSection.getInt("y"), cornerSection.getInt("z"));

        ConfigurationSection spawnSection = section.getConfigurationSection("spawn");
        assert spawnSection != null;
        Location spawn = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(spawnSection.getString("world")))), spawnSection.getInt("x"), spawnSection.getInt("y"), spawnSection.getInt("z"), spawnSection.getInt("yaw"), spawnSection.getInt("pitch"));

        int minWidth = section.getInt("min-size.width");
        int minHeight = section.getInt("min-size.height");
        int maxWidth = section.getInt("max-size.width");
        int maxHeight = section.getInt("max-size.height");

        return new CustomGame(this, gameManager, language, connectionBuilder, corner, spawn, minWidth, minHeight, maxWidth, maxHeight, "custom");
    }

    private @NotNull ResourcePackHandler loadTexturePackHandler(@Nullable ConfigurationSection section) throws IOException {
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
            String link = externalWebServer.getString("link");
            if (link != null)
                return new ExternalWebServerHandler(link);
        }

        return new DisableResourceHandler();
    }

    private @Nullable List<TutorialGame> loadTutorialGames(ConfigurationSection section, GameManager gameManager, List<Game> games, @NotNull Language language) {
        new TutorialGame(this, gameManager, language, null, null, null, null, Material.AIR, 0, null);
        return null;
    }

}
