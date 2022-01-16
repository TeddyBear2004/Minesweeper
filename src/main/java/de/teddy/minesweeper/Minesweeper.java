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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
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
    public void onEnable(){
        saveDefaultConfig();

        saveResource("lang/" + getConfig().getString("language") + ".toml", false);
        File file = new File(getDataFolder(), "lang/" + getConfig().getString("language") + ".toml");
        if(!file.exists()){
            getLogger().warning("Language file not found! Using default language file!");
            saveResource("lang/en_US", false);
            file = new File(getDataFolder(), "lang/en_US");
        }
        language = new Language(new Toml().read(file));
        INSTANCE = this;

        Inventories.loadInventories();

        extractWorld();
        WORLD = new WorldCreator("MineSweeper").createWorld();
        Objects.requireNonNull(this.getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(this.getCommand("resetResourcePack")).setExecutor(new ResetResourcePack());

        getServer().getPluginManager().registerEvents(new GenericEvents(), this);
        getServer().getPluginManager().registerEvents(new OnInventory(), this);
        getServer().getPluginManager().registerEvents(new GenericRightClickEvent(), this);

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().setContents(Inventories.viewerInventory);
            Game.MAP10X10.startViewing(player, null);
        });

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new RightClickEvent());
        protocolManager.addPacketListener(new LeftClickEvent());
        protocolManager.addPacketListener(new OnResourcePackStatus());

    }

    private void extractWorld(){
        File minesweeper = new File("MineSweeper");
        if(minesweeper.exists())
            return;

        saveResource("MineSweeper.zip", true);
        File zippedFile = new File(getDataFolder(), "MineSweeper.zip");
        try{
            ZipFile zipFile = new ZipFile(zippedFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int i = 0;
            int entriesCount = zipFile.size();
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                i++;
                getLogger().info("Extracting world file: " + entry.getName() + " (" + i + "/" + entriesCount + ")");
                if(entry.isDirectory())
                    new File(entry.getName()).mkdirs();
                else{
                    InputStream inputStream = zipFile.getInputStream(entry);
                    File file = new File(entry.getName());
                    if(!file.exists()){
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int length;
                    while((length = inputStream.read(buffer)) > 0)
                        fileOutputStream.write(buffer, 0, length);
                    fileOutputStream.close();
                }
            }
            zipFile.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        zippedFile.delete();

        if(zippedFile.getParentFile().listFiles().length == 0)
            zippedFile.getParentFile().delete();
    }
}
