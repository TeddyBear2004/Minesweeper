package de.teddy.minesweeper.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class HeadGenerator {

    public static ItemStack getHeadFromUrl(String url) {
        PlayerProfile playerProfile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());

        try{
            playerProfile.getTextures().setSkin(new URL(url));
        }catch(MalformedURLException e){
            throw new RuntimeException(e);
        }

        return getHeadFromPlayerProfile(playerProfile);
    }

    public static ItemStack getHeadFromPlayerProfile(PlayerProfile playerProfile) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        if (meta != null) {
            meta.setOwnerProfile(playerProfile);
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }


}