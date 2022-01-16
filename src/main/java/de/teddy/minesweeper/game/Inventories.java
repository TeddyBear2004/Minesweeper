package de.teddy.minesweeper.game;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.teddy.minesweeper.Minesweeper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

public class Inventories {
    public static Inventory startCommandInventory;

    public static ItemStack[] gameInventory;
    public static ItemStack[] viewerInventory;
    public static ItemStack compass;
    public static ItemStack book;
    public static ItemStack hourGlass;
    public static ItemStack barrier;
    public static ItemStack reload;
    public static ItemStack easyMode;
    public static ItemStack mediumMode;
    public static ItemStack hardMode;
    private static Method metaSetProfileMethod;

    public static void loadInventories(){
        gameInventory = new ItemStack[27];
        viewerInventory = new ItemStack[27];
        startCommandInventory = Bukkit.createInventory(null, 9, ChatColor.AQUA + "Minesweeper");
        //GameInventory
        {
            barrier = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = barrier.getItemMeta();
            assert barrierMeta != null;
            barrierMeta.setDisplayName(ChatColor.DARK_RED + Minesweeper.language.getString("leave"));
            barrier.setItemMeta(barrierMeta);

            reload = getPlayerHead("http://textures.minecraft.net/texture/e887cc388c8dcfcf1ba8aa5c3c102dce9cf7b1b63e786b34d4f1c3796d3e9d61");

            SkullMeta headMeta = (SkullMeta)reload.getItemMeta();
            assert headMeta != null;
            headMeta.setDisplayName(ChatColor.YELLOW + Minesweeper.language.getString("restart_pl"));
            reload.setItemMeta(headMeta);

            gameInventory[2] = reload;
            gameInventory[6] = barrier;
        }
        //ViewerInventory
        {
            compass = new ItemStack(Material.COMPASS);
            ItemMeta compassMeta = compass.getItemMeta();
            assert compassMeta != null;
            compassMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.STRIKETHROUGH + Minesweeper.language.getString("watch_others"));
            compass.setItemMeta(compassMeta);

            String[] tutorialPages = new String[]{
                    Minesweeper.language.getString("book_page_1"),
                    Minesweeper.language.getString("book_page_2"),
                    Minesweeper.language.getString("book_page_3")};

            book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta)book.getItemMeta();
            assert bookMeta != null;
            bookMeta.setDisplayName(ChatColor.YELLOW + Minesweeper.language.getString("book_tutorial"));
            bookMeta.setAuthor(ChatColor.AQUA + "TeddyBear_2004");
            bookMeta.setTitle(Minesweeper.language.getString("book_title"));
            bookMeta.setPages(tutorialPages);
            book.setItemMeta(bookMeta);

            hourGlass = getPlayerHead("http://textures.minecraft.net/texture/b522cac48d1151b0a6eebb72fae26626c394fdc62d5b2064a69266e796a20268");

            SkullMeta timeMeta = (SkullMeta)hourGlass.getItemMeta();
            assert timeMeta != null;
            timeMeta.setDisplayName(ChatColor.AQUA + Minesweeper.language.getString("hour_glass_display_name"));
            hourGlass.setItemMeta(timeMeta);

            viewerInventory[1] = compass;
            viewerInventory[4] = hourGlass;
            viewerInventory[7] = book;
        }

        {
            easyMode = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta easyMeta = easyMode.getItemMeta();
            assert easyMeta != null;
            easyMeta.setDisplayName(ChatColor.GREEN + Minesweeper.language.getString("difficulty_easy"));
            easyMeta.setLore(Collections.singletonList(Minesweeper.language.getString("field_desc", "10", "10", "10")));
            easyMode.setItemMeta(easyMeta);

            mediumMode = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta mediumMeta = mediumMode.getItemMeta();
            assert mediumMeta != null;
            mediumMeta.setDisplayName(ChatColor.YELLOW + Minesweeper.language.getString("difficulty_normal"));
            mediumMeta.setLore(Collections.singletonList(Minesweeper.language.getString("field_desc", "18", "18", "24")));
            mediumMode.setItemMeta(mediumMeta);

            hardMode = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta hardMeta = hardMode.getItemMeta();
            assert hardMeta != null;
            hardMeta.setDisplayName(ChatColor.RED + Minesweeper.language.getString("difficulty_hard"));
            hardMeta.setLore(Collections.singletonList(Minesweeper.language.getString("field_desc", "24", "24", "99")));
            hardMode.setItemMeta(hardMeta);

            startCommandInventory.setItem(2, easyMode);
            startCommandInventory.setItem(4, mediumMode);
            startCommandInventory.setItem(6, hardMode);
        }
    }

    private static GameProfile makeProfile(String b64){
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );
        GameProfile profile = new GameProfile(id, "Player");
        profile.getProperties().put("textures", new Property("textures", b64));
        return profile;
    }

    private static String urlToBase64(String url){

        URI actualUrl;
        try{
            actualUrl = new URI(url);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
        String toEncode = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
        return Base64.getEncoder().encodeToString(toEncode.getBytes());
    }

    public static ItemStack getPlayerHead(String url){
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta)head.getItemMeta();
        assert itemMeta != null;

        try{
            if(metaSetProfileMethod == null){
                metaSetProfileMethod = itemMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                metaSetProfileMethod.setAccessible(true);
            }

            metaSetProfileMethod.invoke(itemMeta, makeProfile(urlToBase64(url)));
        }catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
            e.printStackTrace();
        }

        head.setItemMeta(itemMeta);
        return head;
    }
}
