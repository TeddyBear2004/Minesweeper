package de.teddy.minesweeper.game.inventory;

import de.teddy.minesweeper.Minesweeper;
import de.teddy.minesweeper.game.GameManager;
import de.teddy.minesweeper.game.inventory.content.ContentFiller;
import de.teddy.minesweeper.game.inventory.content.MainMenuFiller;
import de.teddy.minesweeper.game.inventory.content.ViewGamesFiller;
import de.teddy.minesweeper.util.HeadGenerator;
import de.teddy.minesweeper.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum Inventories {
    CHOOSE_GAME,
    VIEW_GAMES;

    public static final ItemStack[] GAME_INVENTORY = new ItemStack[27];
    public static final ItemStack[] VIEWER_INVENTORY = new ItemStack[27];
    private static final Map<Inventories, Supplier<Inventory>> INVENTORIES = new HashMap<>();
    private static final Map<Inventories, String> INVENTORY_NAME_MAP = new HashMap<>();
    private static final Map<ItemStack, Function<Player, Inventory>> ITEM_INVENTORY_MAP = new HashMap<>();
    private static final Map<ItemStack, Function<Player, Inventories>> ITEM_INVENTORY_TYPE_MAP = new HashMap<>();
    private static final Map<ItemStack, Function<Player, Consumer<Inventory>>> ITEM_CONSUMER_MAP = new HashMap<>();
    public static ItemStack compass;
    public static ItemStack book;
    public static ItemStack hourGlass;
    public static ItemStack barrier;
    public static ItemStack reload;
    private static GameManager gameManager;

    public static void initialise(int availableGamesInventoryLines, Language language, GameManager gameManager) {
        Inventories.gameManager = gameManager;
        loadGameInventory(language);
        loadViewerInventory(language);

        INVENTORY_NAME_MAP.put(CHOOSE_GAME, ChatColor.AQUA + language.getString("minesweeper"));
        INVENTORIES.put(CHOOSE_GAME, createSupplier(null,
                                                    availableGamesInventoryLines * 9,
                                                    INVENTORY_NAME_MAP.get(CHOOSE_GAME), CHOOSE_GAME));

        INVENTORY_NAME_MAP.put(VIEW_GAMES, ChatColor.AQUA + "Watch other games!");
        INVENTORIES.put(VIEW_GAMES, createSupplier(null,
                                                   54,
                                                   INVENTORY_NAME_MAP.get(VIEW_GAMES), VIEW_GAMES));
    }

    public static Consumer<Inventory> getConsumer(ItemStack itemStack, Player whoClicked) {
        Function<Player, Consumer<Inventory>> playerConsumerFunction = ITEM_CONSUMER_MAP.get(itemStack);
        return playerConsumerFunction != null ? playerConsumerFunction.apply(whoClicked) : null;
    }

    public static boolean isValidInventory(InventoryView view) {
        return view != null && INVENTORY_NAME_MAP.keySet().stream().anyMatch(type -> view.getTitle().equals(INVENTORY_NAME_MAP.get(type)));
    }

    private static List<ContentFiller> getIContentFillers() {
        return List.of(new MainMenuFiller(Minesweeper.getPlugin(Minesweeper.class).getGames()), new ViewGamesFiller(gameManager));
    }

    private static void loadGameInventory(Language language) {
        barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null)
            barrierMeta.setDisplayName(ChatColor.DARK_RED + language.getString("leave"));
        barrier.setItemMeta(barrierMeta);

        reload = HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/e887cc388c8dcfcf1ba8aa5c3c102dce9cf7b1b63e786b34d4f1c3796d3e9d61");

        SkullMeta headMeta = (SkullMeta) reload.getItemMeta();
        if (headMeta != null)
            headMeta.setDisplayName(ChatColor.YELLOW + language.getString("restart_pl"));
        reload.setItemMeta(headMeta);

        GAME_INVENTORY[2] = reload;
        GAME_INVENTORY[6] = barrier;
    }

    private static void loadViewerInventory(Language language) {
        compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        if (compassMeta != null)
            compassMeta.setDisplayName(ChatColor.GREEN + language.getString("watch_others"));
        compass.setItemMeta(compassMeta);

        String[] tutorialPages = new String[]{
                language.getString("book_page_1"),
                language.getString("book_page_2"),
                language.getString("book_page_3")};

        book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setDisplayName(ChatColor.YELLOW + language.getString("book_tutorial"));
            bookMeta.setAuthor(ChatColor.AQUA + "TeddyBear_2004");
            bookMeta.setTitle(language.getString("book_title"));
            bookMeta.setPages(tutorialPages);
        }
        book.setItemMeta(bookMeta);

        hourGlass = HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/b522cac48d1151b0a6eebb72fae26626c394fdc62d5b2064a69266e796a20268");

        SkullMeta timeMeta = (SkullMeta) hourGlass.getItemMeta();
        if (timeMeta != null)
            timeMeta.setDisplayName(ChatColor.AQUA + language.getString("hour_glass_display_name"));

        hourGlass.setItemMeta(timeMeta);

        VIEWER_INVENTORY[1] = compass;
        VIEWER_INVENTORY[4] = hourGlass;
        VIEWER_INVENTORY[7] = book;
    }

    private static Supplier<Inventory> createSupplier(InventoryHolder holder, int size, String name, Inventories inventories) {
        Inventory inventory = Bukkit.createInventory(holder, size, name);
        return () -> {
            getIContentFillers().forEach(iContentFiller -> {
                if (iContentFiller.getEInventory() == inventories) {
                    ITEM_INVENTORY_MAP.putAll(iContentFiller.insertInventoryItems(inventory));
                    ITEM_INVENTORY_TYPE_MAP.putAll(iContentFiller.insertEInventoryItems(inventory));
                    ITEM_CONSUMER_MAP.putAll(iContentFiller.insertConsumerItems(inventory));
                }
            });

            return inventory;
        };
    }

    public Inventory getInventory() {
        return INVENTORIES.get(this).get();
    }
}
