package de.teddy.minesweeper.util;

import com.moandjiezana.toml.Toml;
import org.bukkit.ChatColor;

public class Language {
    private final Toml config;

    public Language(Toml config) {
        this.config = config;
    }

    public String getString(String key) {
        String string = config.getString(key);
        return string == null ? key : string;
    }

    public String getString(String key, String... args) {
        String string = config.getString(key);
        for (int i = 0; i < args.length; i++)
            string = string.replace("{" + i + "}", args[i]);
        return ChatColor.translateAlternateColorCodes('§', string);
    }
}
