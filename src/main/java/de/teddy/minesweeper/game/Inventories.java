package de.teddy.minesweeper.game;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public class Inventories {
    public static final Inventory startCommandInventory;

    public static final ItemStack[] gameInventory;
    public static final ItemStack[] viewerInventory;
    public static final ItemStack compass;
    public static final ItemStack book;
    public static final ItemStack hourGlass;
    public static final ItemStack redBanner;
    public static final ItemStack barrier;
    public static final ItemStack reload;
    public static final ItemStack easyMode;
    public static final ItemStack mediumMode;
    public static final ItemStack hardMode;
    private static Method metaSetProfileMethod;

    private static final String[] tutorialPages;

    static{
        gameInventory = new ItemStack[27];
        viewerInventory = new ItemStack[27];
        startCommandInventory = Bukkit.createInventory(null, 9, ChatColor.AQUA + "Minesweeper");
        //GameInventory
        {
            redBanner = new ItemStack(Material.RED_BANNER);
            ItemMeta bannerMeta = redBanner.getItemMeta();
            assert bannerMeta != null;
            bannerMeta.setDisplayName(ChatColor.RED + "Flagge platzieren");
            redBanner.setItemMeta(bannerMeta);

            barrier = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = barrier.getItemMeta();
            assert barrierMeta != null;
            barrierMeta.setDisplayName(ChatColor.DARK_RED + "Verlassen");
            barrier.setItemMeta(barrierMeta);

            reload = getPlayerHead("http://textures.minecraft.net/texture/e887cc388c8dcfcf1ba8aa5c3c102dce9cf7b1b63e786b34d4f1c3796d3e9d61");

            SkullMeta headMeta = (SkullMeta)reload.getItemMeta();
            assert headMeta != null;
            headMeta.setDisplayName(ChatColor.YELLOW + "Neustarten");
            reload.setItemMeta(headMeta);

            //gameInventory[1] = redBanner;
            gameInventory[2] = reload;
            gameInventory[6] = barrier;
        }
        //ViewerInventory
        {
            compass = new ItemStack(Material.COMPASS);
            ItemMeta compassMeta = compass.getItemMeta();
            assert compassMeta != null;
            compassMeta.setDisplayName(ChatColor.YELLOW + "Sieh anderen Spielern zu");
            compass.setItemMeta(compassMeta);

            tutorialPages = new String[]{
                    "\u00A72\u00A7nMinesweeper\n" +
                            "\n" +
                            "\u00A7rDas Ziel ist es, alle Felder zu enthüllen. Dabei darfst du keine Bombe aktivieren.\n" +
                            "\n" +
                            "Die Zahlen zeigen dir, wie viele Bomben in einem 3x3 Feld liegen.",
                    "\u00A72\u00A7nBenutze:\u00A7r\n" +
                            "\n" +
                            "\u00A79\u00A7oLinksklick\u00A7r, um ein Feld zu enthüllen.\n" +
                            "\n" +
                            "\u00A7r\u00A79\u00A7oRechtsklick\u00A7r, um eine Flagge zu platzieren.\n" +
                            "\u00A78->Damit markierst du\n" +
                            "   die Bomben und\n" +
                            "   kannst sie nicht\n" +
                            "   mehr aktivieren.\n" +
                            "->Entfernen mit\n" +
                            "   erneutem Rechtklick",
                    "\u00A79\u00A7oDoppel Linksklick\u00A7r, um\n" +
                            "alle umliegenden Felder aufzudecken.\n" +
                            "\u00A78->Nur möglich, wenn im\n" +
                            "   Umfeld schon\n" +
                            "   die benötigten\n" +
                            "   Bomben markiert\n" +
                            "   wurden."
            };

            book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta bookMeta = (BookMeta)book.getItemMeta();
            assert bookMeta != null;
            bookMeta.setDisplayName(ChatColor.YELLOW + "Tutorial");
            bookMeta.setAuthor(ChatColor.AQUA + "TeddyBear_2004");
            bookMeta.setTitle("Minesweeper - Tutorial");
            bookMeta.setPages(tutorialPages);
            book.setItemMeta(bookMeta);

            hourGlass = getPlayerHead("http://textures.minecraft.net/texture/b522cac48d1151b0a6eebb72fae26626c394fdc62d5b2064a69266e796a20268");

            SkullMeta timeMeta = (SkullMeta)hourGlass.getItemMeta();
            assert timeMeta != null;
            timeMeta.setDisplayName(ChatColor.AQUA + "Starte ein Spiel");
            hourGlass.setItemMeta(timeMeta);

            //viewerInventory[1] = compass;
            viewerInventory[4] = hourGlass;
            viewerInventory[7] = book;
        }

        {
            easyMode = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta easyMeta = easyMode.getItemMeta();
            assert easyMeta != null;
            easyMeta.setDisplayName(ChatColor.GREEN + "Einfach");
            easyMeta.setLore(Arrays.asList("Größe: 10x10", "Bomben: 10"));
            easyMode.setItemMeta(easyMeta);

            mediumMode = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta mediumMeta = mediumMode.getItemMeta();
            assert mediumMeta != null;
            mediumMeta.setDisplayName(ChatColor.YELLOW + "Mittel");
            mediumMeta.setLore(Arrays.asList("Größe: 18x18", "Bomben: 40"));
            mediumMode.setItemMeta(mediumMeta);

            hardMode = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta hardMeta = hardMode.getItemMeta();
            assert hardMeta != null;
            hardMeta.setDisplayName(ChatColor.RED + "Schwer");
            hardMeta.setLore(Arrays.asList("Größe: 24x24", "Bomben: 99"));
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
