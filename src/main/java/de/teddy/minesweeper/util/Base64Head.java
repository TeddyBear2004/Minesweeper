package de.teddy.minesweeper.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class Base64Head {

    public static ItemStack getBase64Head(String url) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        assert meta != null;

        PlayerProfile playerProfile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());

        try{
            playerProfile.getTextures().setSkin(new URL(url));
        }catch(MalformedURLException e){
            throw new RuntimeException(e);
        }

        meta.setOwnerProfile(playerProfile);

        itemStack.setItemMeta(meta);

        return itemStack;
    }

}