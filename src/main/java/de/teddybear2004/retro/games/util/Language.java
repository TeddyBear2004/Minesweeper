package de.teddybear2004.retro.games.util;

import com.moandjiezana.toml.Toml;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class Language {

    private final Toml config;

    public Language(Toml config) {
        this.config = config;
    }

    public String getString(String key) {
        String string = config.getString(key);
        return string == null ? key : string;
    }

    public @NotNull String getString(String key, String @NotNull ... args) {
        String string = config.getString(key);
        for (int i = 0; i < args.length; i++)
            string = string.replace("{" + i + "}", args[i]);
        return ChatColor.translateAlternateColorCodes('§', string);
    }

}
