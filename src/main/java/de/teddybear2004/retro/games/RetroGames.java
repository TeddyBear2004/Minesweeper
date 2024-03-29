package de.teddybear2004.retro.games;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.moandjiezana.toml.Toml;
import de.teddybear2004.retro.games.commands.*;
import de.teddybear2004.retro.games.events.*;
import de.teddybear2004.retro.games.events.packets.LeftClickEvent;
import de.teddybear2004.retro.games.events.packets.RightClickEvent;
import de.teddybear2004.retro.games.game.Board;
import de.teddybear2004.retro.games.game.CustomGame;
import de.teddybear2004.retro.games.game.Game;
import de.teddybear2004.retro.games.game.GameManager;
import de.teddybear2004.retro.games.game.click.ClickHandler;
import de.teddybear2004.retro.games.game.expansions.StatsExpansion;
import de.teddybear2004.retro.games.game.inventory.InventoryManager;
import de.teddybear2004.retro.games.game.modifier.Modifier;
import de.teddybear2004.retro.games.game.modifier.ModifierArea;
import de.teddybear2004.retro.games.game.painter.ArmorStandPainter;
import de.teddybear2004.retro.games.game.painter.Atelier;
import de.teddybear2004.retro.games.game.painter.BlockPainter;
import de.teddybear2004.retro.games.game.texture.pack.DisableResourceHandler;
import de.teddybear2004.retro.games.game.texture.pack.ExternalWebServerHandler;
import de.teddybear2004.retro.games.game.texture.pack.InternalWebServerHandler;
import de.teddybear2004.retro.games.game.texture.pack.ResourcePackHandler;
import de.teddybear2004.retro.games.minesweeper.MinesweeperBoard;
import de.teddybear2004.retro.games.minesweeper.MinesweeperField;
import de.teddybear2004.retro.games.minesweeper.click.MinesweeperClickHandler;
import de.teddybear2004.retro.games.minesweeper.painter.MinesweeperArmorStandPainter;
import de.teddybear2004.retro.games.minesweeper.painter.MinesweeperBlockPainter;
import de.teddybear2004.retro.games.scheduler.HidePlayerScheduler;
import de.teddybear2004.retro.games.scheduler.RemoveMarkerScheduler;
import de.teddybear2004.retro.games.sudoku.SudokuBoard;
import de.teddybear2004.retro.games.sudoku.SudokuField;
import de.teddybear2004.retro.games.sudoku.SudokuGame;
import de.teddybear2004.retro.games.sudoku.click.SudokuClickHandler;
import de.teddybear2004.retro.games.sudoku.painter.SudokuArmorStandPainter;
import de.teddybear2004.retro.games.sudoku.painter.SudokuBlockPainter;
import de.teddybear2004.retro.games.util.ConnectionBuilder;
import de.teddybear2004.retro.games.util.JarWalker;
import de.teddybear2004.retro.games.util.Language;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class RetroGames extends JavaPlugin {

    private final List<BukkitTask> tasks = new ArrayList<>();
    private String langPath;
    private ResourcePackHandler resourcePackHandler;
    private Language language;
    private GameManager gameManager;
    private Atelier atelier;
    private int lines;

    public Language getLanguage() {
        return language;
    }

    public int getChooseGameLines() {
        return lines;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public Atelier getAtelier() {
        return atelier;
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

        File gameFile = new File(getDataFolder(), "games.yml");
        if (!gameFile.exists()) {
            saveResource("games.yml", false);
        }

        this.lines = getConfig().getInt("available_games_inventory_lines");

        RemoveMarkerScheduler removeMarkerScheduler = new RemoveMarkerScheduler();
        removeMarkerScheduler.runTaskTimer(this, 0, 5);

        gameManager = new GameManager(new ArrayList<>(), removeMarkerScheduler);

        ClickHandler<MinesweeperField, Board<MinesweeperField>> minesweeperClickHandler = new MinesweeperClickHandler();
        ClickHandler<SudokuField, Board<SudokuField>> sudokuClickHandler = new SudokuClickHandler();

        atelier = new Atelier();
        atelier.register(MinesweeperBoard.class, BlockPainter.class,
                         new MinesweeperBlockPainter(this, minesweeperClickHandler, gameManager));
        atelier.register(MinesweeperBoard.class, ArmorStandPainter.class,
                         new MinesweeperArmorStandPainter(this, minesweeperClickHandler, gameManager));
        atelier.register(SudokuBoard.class, BlockPainter.class,
                         new SudokuBlockPainter(this, sudokuClickHandler, gameManager));
        atelier.register(SudokuBoard.class, ArmorStandPainter.class,
                         new SudokuArmorStandPainter(this, sudokuClickHandler, gameManager));

        language = loadLanguage();
        List<ModifierArea> modifierAreas = loadAreas();
        loadWorld(getConfig().getString("map_url"));
        loadModifier(modifierAreas);
        ConnectionBuilder connectionBuilder = loadConnectionBuilder(getConfig().getConfigurationSection("database"));

        YamlConfiguration gameConfig = YamlConfiguration.loadConfiguration(gameFile);
        loadGames(language, connectionBuilder, gameManager, gameConfig);
        Game customGame = loadCustomGame(gameConfig.getConfigurationSection("custom_game"), connectionBuilder, gameManager, language);

        if (customGame != null)
            gameManager.getGames().add(customGame);

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
        InventoryManager inventoryManager = new InventoryManager(gameManager);


        Objects.requireNonNull(this.getCommand("bypassEventCancellation")).setExecutor(new BypassEventCommand());
        Objects.requireNonNull(this.getCommand("minesweeper")).setExecutor(new MinesweeperCommand(gameManager, customGame, language, inventoryManager));
        Objects.requireNonNull(this.getCommand("settings")).setExecutor(new SettingsCommand(inventoryManager));
        Objects.requireNonNull(this.getCommand("minestats")).setExecutor(new MineStatsCommand(gameManager, connectionBuilder, language));
        Objects.requireNonNull(this.getCommand("mineduel")).setExecutor(new DuelCommand(this, gameManager, language));
        Objects.requireNonNull(this.getCommand("gameManager")).setExecutor(new GameManagerCommand(gameManager, inventoryManager, language));

        getServer().getPluginManager().registerEvents(new CancelableEvents(getConfig().getConfigurationSection("events"), modifierAreas, gameManager), this);
        getServer().getPluginManager().registerEvents(new GenericEvents(resourcePackHandler, customGame, gameManager, atelier), this);
        getServer().getPluginManager().registerEvents(new GenericRightClickEvent(gameManager, inventoryManager), this);
        getServer().getPluginManager().registerEvents(new InventoryClickEvents(gameManager, inventoryManager), this);
        getServer().getPluginManager().registerEvents(new GenericLongClickEvent(gameManager, minesweeperClickHandler), this);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new RightClickEvent(this, gameManager, atelier));
        protocolManager.addPacketListener(new LeftClickEvent(this, gameManager, atelier));

        Bukkit.getOnlinePlayers().forEach(player -> {
            InventoryManager.PlayerInventory.VIEWER.apply(player);
            if (gameManager.getGames().size() != 0)
                gameManager.getGames().get(0).startViewing(player, null);
        });

        tasks.add(new HidePlayerScheduler(this, gameManager).runTaskTimer(this, 20, 5));

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

    private void loadWorld(String url) {
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

        getLogger().info("Downloading world...");
        try(ZipInputStream zis = new ZipInputStream(new URL(url).openStream())){
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
            getLogger().info("Successfully loaded world.");
        }catch(FileNotFoundException e){
            throw new RuntimeException(e);
        }catch(IOException e){
            getLogger().severe("Failed to download/load world:");
            e.printStackTrace();
        }

        WorldCreator.name("MineSweeper").type(WorldType.FLAT).createWorld();
    }

    private void loadModifier(List<? extends ModifierArea> areas) {
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

    private void loadGames(@NotNull Language language, ConnectionBuilder connectionBuilder, GameManager gameManager, YamlConfiguration configuration) {
        ConfigurationSection gameSection = configuration.getConfigurationSection("games");
        if (gameSection != null)
            gameSection.getKeys(false).forEach(key -> {
                ConfigurationSection cornerSection = gameSection.getConfigurationSection(key + ".corner");
                assert cornerSection != null;
                Location corner = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(cornerSection.getString("world")))), cornerSection.getInt("x"), cornerSection.getInt("y"), cornerSection.getInt("z"));

                ConfigurationSection spawnSection = gameSection.getConfigurationSection(key + ".spawn");
                assert spawnSection != null;
                Location spawn = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(spawnSection.getString("world")))), spawnSection.getInt("x"), spawnSection.getInt("y"), spawnSection.getInt("z"), spawnSection.getInt("yaw"), spawnSection.getInt("pitch"));

                int width = gameSection.getInt(key + ".width");
                int height = gameSection.getInt(key + ".height");
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
                                     width,
                                     height,
                                     bombCount,
                                     difficultyLangPath,
                                     material,
                                     inventoryPosition,
                                     atelier);
                gameManager.getGames().add(game);
            });
        ConfigurationSection sudokusSection = configuration.getConfigurationSection("sudokus");
        if (sudokusSection != null)
            sudokusSection.getKeys(false).forEach(key -> {
                ConfigurationSection cornerSection = sudokusSection.getConfigurationSection(key + ".corner");
                assert cornerSection != null;
                Location corner = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(cornerSection.getString("world")))), cornerSection.getInt("x"), cornerSection.getInt("y"), cornerSection.getInt("z"));

                ConfigurationSection spawnSection = sudokusSection.getConfigurationSection(key + ".spawn");
                assert spawnSection != null;
                Location spawn = new Location(Bukkit.createWorld(WorldCreator.name(Objects.requireNonNull(spawnSection.getString("world")))), spawnSection.getInt("x"), spawnSection.getInt("y"), spawnSection.getInt("z"), spawnSection.getInt("yaw"), spawnSection.getInt("pitch"));

                String difficultyLangPath = sudokusSection.getString(key + ".difficulty_lang_path");

                Material material = Material.valueOf(sudokusSection.getString(key + ".inventory_material"));
                int inventoryPosition = sudokusSection.getInt(key + ".inventory_position");

                gameManager.getGames().add(new SudokuGame(this,
                                                          gameManager,
                                                          language,
                                                          connectionBuilder,
                                                          corner,
                                                          spawn,
                                                          difficultyLangPath,
                                                          material,
                                                          inventoryPosition,
                                                          atelier));

            });

    }

    private Game loadCustomGame(@Nullable ConfigurationSection section, ConnectionBuilder
            connectionBuilder, GameManager gameManager, @NotNull Language language) {
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

        return new CustomGame(this, gameManager, language, connectionBuilder, corner, spawn, minWidth, minHeight, maxWidth, maxHeight, "custom", atelier);
    }

    private @NotNull ResourcePackHandler loadTexturePackHandler(@Nullable ConfigurationSection section) throws
            IOException {
        if (section == null)
            return new DisableResourceHandler(atelier);

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

        return new DisableResourceHandler(atelier);
    }

}
