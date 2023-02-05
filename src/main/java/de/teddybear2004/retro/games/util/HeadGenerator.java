package de.teddybear2004.retro.games.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeadGenerator {

    private static final Map<UUID, PlayerProfile> UUID_PLAYER_PROFILE_MAP = new HashMap<>();

    public static @NotNull ItemStack getHeadFromUrl(@NotNull String url) {
        PlayerProfile playerProfile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());

        try{
            playerProfile.getTextures().setSkin(new URL(url));
        }catch(MalformedURLException e){
            throw new RuntimeException(e);
        }

        return getHeadFromPlayerProfile(playerProfile);
    }

    public static @NotNull ItemStack getHeadFromPlayerProfile(PlayerProfile playerProfile) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        if (meta != null) {
            meta.setOwnerProfile(playerProfile);
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public static PlayerProfile getPlayerProfile(@NotNull UUID uuid) throws Exception {
        if (UUID_PLAYER_PROFILE_MAP.containsKey(uuid))
            return UUID_PLAYER_PROFILE_MAP.get(uuid);

        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", "");

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to retrieve player profile: HTTP " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response.toString());
        JsonObject object = element.getAsJsonObject();

        String name = object.get("name").getAsString();
        String id = object.get("id").getAsString();

        Property property = null;
        JsonArray propertyArray = object.get("properties").getAsJsonArray();
        for (JsonElement propertyElement : propertyArray) {
            JsonObject propertyObject = propertyElement.getAsJsonObject();
            String propertyName = propertyObject.get("name").getAsString();
            String propertyValue = propertyObject.get("value").getAsString();
            String signature = null;
            if (propertyObject.has("signature")) {
                signature = propertyObject.get("signature").getAsString();
            }

            property = new Property(propertyName, propertyValue, signature);
        }

        UUID uuid1 = UUID.fromString(id.substring(0, 8) + "-" +
                                             id.substring(8, 12) + "-" +
                                             id.substring(12, 16) + "-" +
                                             id.substring(16, 20) + "-" +
                                             id.substring(20));

        GameProfile gameProfile = new GameProfile(uuid1, name);
        gameProfile.getProperties().put("textures", property);

        PlayerProfile playerProfile = Bukkit.createPlayerProfile(uuid1, name);

        UUID_PLAYER_PROFILE_MAP.put(uuid, playerProfile);

        return playerProfile;

    }

}