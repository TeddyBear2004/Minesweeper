package de.teddy.minesweeper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.moandjiezana.toml.Toml;
import de.teddy.minesweeper.commands.ResetResourcePack;
import de.teddy.minesweeper.commands.StartCommand;
import de.teddy.minesweeper.events.GenericEvents;
import de.teddy.minesweeper.events.GenericRightClickEvent;
import de.teddy.minesweeper.events.OnInventory;
import de.teddy.minesweeper.events.packets.LeftClickEvent;
import de.teddy.minesweeper.events.packets.OnResourcePackStatus;
import de.teddy.minesweeper.events.packets.RightClickEvent;
import de.teddy.minesweeper.game.Game;
import de.teddy.minesweeper.game.Inventories;
import de.teddy.minesweeper.util.Language;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class Minesweeper extends JavaPlugin {
    public static World WORLD;
    public static JavaPlugin INSTANCE;
    public static Language language;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        saveResource("lang/" + getConfig().getString("language") + ".toml", false);
        File file = new File(getDataFolder(), "lang/" + getConfig().getString("language") + ".toml");
        if (!file.exists()) {
            getLogger().warning("Language file not found! Using default language file!");
            saveResource("lang/en_US", false);
            file = new File(getDataFolder(), "lang/en_US");
        }
        language = new Language(new Toml().read(file));
        INSTANCE = this;

        loadGames(getConfig());

        Inventories.loadInventories(getConfig().getInt("available_games_inventory_lines"), Game.games);

        if (getConfig().getBoolean("use_default_map"))
            extractWorld();
        WORLD = new WorldCreator("MineSweeper").createWorld();
        Objects.requireNonNull(this.getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(this.getCommand("resetResourcePack")).setExecutor(new ResetResourcePack());

        getServer().getPluginManager().registerEvents(new GenericEvents(), this);
        getServer().getPluginManager().registerEvents(new OnInventory(), this);
        getServer().getPluginManager().registerEvents(new GenericRightClickEvent(), this);

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().setContents(Inventories.viewerInventory);
            Game.games.get(0).startViewing(player, null);
        });

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new RightClickEvent());
        protocolManager.addPacketListener(new LeftClickEvent());
        protocolManager.addPacketListener(new OnResourcePackStatus());

    }

    private void extractWorld() {
        File minesweeper = new File("MineSweeper");
        if (minesweeper.exists())
            return;

        saveResource("MineSweeper.zip", true);
        File zippedFile = new File(getDataFolder(), "MineSweeper.zip");
        try{
            ZipFile zipFile = new ZipFile(zippedFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int i = 0;
            int entriesCount = zipFile.size();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                i++;
                getLogger().info("Extracting world file: " + entry.getName() + " (" + i + "/" + entriesCount + ")");
                if (entry.isDirectory())
                    new File(entry.getName()).mkdirs();
                else{
                    InputStream inputStream = zipFile.getInputStream(entry);
                    File file = new File(entry.getName());
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0)
                        fileOutputStream.write(buffer, 0, length);
                    fileOutputStream.close();
                }
            }
            zipFile.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        zippedFile.delete();

        if (zippedFile.getParentFile().listFiles().length == 0)
            zippedFile.getParentFile().delete();
    }

    private static void loadGames(FileConfiguration config) {
        ConfigurationSection games = config.getConfigurationSection("games");
        if (games == null)
            return;
        games.getKeys(false).forEach(key -> {
            ConfigurationSection cornerSection = games.getConfigurationSection(key + ".corner");
            assert cornerSection != null;
            Location corner = new Location(Bukkit.getWorld(Objects.requireNonNull(cornerSection.getString("world"))), cornerSection.getInt("x"), cornerSection.getInt("y"), cornerSection.getInt("z"));

            ConfigurationSection spawnSection = games.getConfigurationSection(key + ".spawn");
            assert spawnSection != null;
            Location spawn = new Location(Bukkit.getWorld(Objects.requireNonNull(spawnSection.getString("world"))), spawnSection.getInt("x"), spawnSection.getInt("y"), spawnSection.getInt("z"), spawnSection.getInt("yaw"), spawnSection.getInt("pitch"));

            int borderSize = games.getInt(key + ".border_size");
            int bombCount = games.getInt(key + ".bomb_count");
            String difficultyLangPath = games.getString(key + ".difficulty_lang_path");

            Material material = Material.valueOf(games.getString(key + ".inventory_material"));
            int inventoryPosition = games.getInt(key + ".inventory_position");

            Game game = new Game(corner, spawn, borderSize, bombCount, Minesweeper.language.getString(difficultyLangPath), material, inventoryPosition);
            Game.games.add(game);
        });
    }
}
