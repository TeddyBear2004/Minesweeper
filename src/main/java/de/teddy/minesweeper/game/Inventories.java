package de.teddy.minesweeper.game;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.util.HeadGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.List;

public class Inventories {

    public static Inventory startCommandInventory;
    public static ItemStack[] gameInventory;
    public static ItemStack[] viewerInventory;
    public static ItemStack compass;
    public static ItemStack book;
    public static ItemStack hourGlass;
    public static ItemStack barrier;
    public static ItemStack reload;
    private static Method metaSetProfileMethod;

    public static void loadInventories(int inventoryLines, List<Game> games) {
        gameInventory = new ItemStack[27];
        viewerInventory = new ItemStack[27];
        startCommandInventory = Bukkit.createInventory(null, inventoryLines * 9, ChatColor.AQUA + Minesweeper.getLanguage().getString("minesweeper"));

        loadGameInventory();
        loadViewerInventory();

        games.forEach(game -> startCommandInventory.setItem(game.getInventoryPosition(), game.getItemStack()));
    }

    private static void loadGameInventory() {
        barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null)
            barrierMeta.setDisplayName(ChatColor.DARK_RED + Minesweeper.getLanguage().getString("leave"));
        barrier.setItemMeta(barrierMeta);

        reload = HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/e887cc388c8dcfcf1ba8aa5c3c102dce9cf7b1b63e786b34d4f1c3796d3e9d61");

        SkullMeta headMeta = (SkullMeta) reload.getItemMeta();
        if (headMeta != null)
            headMeta.setDisplayName(ChatColor.YELLOW + Minesweeper.getLanguage().getString("restart_pl"));
        reload.setItemMeta(headMeta);

        gameInventory[2] = reload;
        gameInventory[6] = barrier;
    }

    private static void loadViewerInventory() {
        compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        if (compassMeta != null)
            compassMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.STRIKETHROUGH + Minesweeper.getLanguage().getString("watch_others"));
        compass.setItemMeta(compassMeta);

        String[] tutorialPages = new String[]{
                Minesweeper.getLanguage().getString("book_page_1"),
                Minesweeper.getLanguage().getString("book_page_2"),
                Minesweeper.getLanguage().getString("book_page_3")};

        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setDisplayName(ChatColor.YELLOW + Minesweeper.getLanguage().getString("book_tutorial"));
            bookMeta.setAuthor(ChatColor.AQUA + "TeddyBear_2004");
            bookMeta.setTitle(Minesweeper.getLanguage().getString("book_title"));
            bookMeta.setPages(tutorialPages);
        }
        book.setItemMeta(bookMeta);

        hourGlass = HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/b522cac48d1151b0a6eebb72fae26626c394fdc62d5b2064a69266e796a20268");

        SkullMeta timeMeta = (SkullMeta) hourGlass.getItemMeta();
        if (timeMeta != null)
            timeMeta.setDisplayName(ChatColor.AQUA + Minesweeper.getLanguage().getString("hour_glass_display_name"));
        hourGlass.setItemMeta(timeMeta);

        viewerInventory[1] = compass;
        viewerInventory[4] = hourGlass;
        viewerInventory[7] = book;
    }
}
