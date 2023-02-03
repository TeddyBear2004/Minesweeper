package de.teddybear2004.minesweeper.game.inventory;

import com.mojang.datafixers.util.Pair;
import de.teddybear2004.minesweeper.Minesweeper;
import de.teddybear2004.minesweeper.game.GameManager;
import de.teddybear2004.minesweeper.game.inventory.generator.InventoryGenerator;
import de.teddybear2004.minesweeper.util.HeadGenerator;
import de.teddybear2004.minesweeper.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class InventoryManager {

    public static final NamespacedKey ITEM_ID = new NamespacedKey(Minesweeper.getPlugin(Minesweeper.class), "item_id");
    private final GameManager manager;
    private final Map<Integer, BiConsumer<Inventory, ClickType>> itemStackFunctionMap = new HashMap<>();
    private final Map<Class<? extends InventoryGenerator>, InventoryGenerator> inventoryGeneratorMap = new HashMap<>();
    private final AtomicInteger integer;

    public InventoryManager(GameManager manager) {
        this.manager = manager;
        this.integer = new AtomicInteger(0);
    }

    public Inventory getInventory(Class<? extends InventoryGenerator> inventoryGenerator, Player player) {
        try{

            InventoryGenerator inventoryGenerator1;

            if (inventoryGeneratorMap.containsKey(inventoryGenerator)) {
                inventoryGenerator1 = inventoryGeneratorMap.get(inventoryGenerator);
            } else {
                inventoryGenerator1 = inventoryGenerator.getConstructor(GameManager.class).newInstance(manager);
                inventoryGeneratorMap.put(inventoryGenerator, inventoryGenerator1);
            }

            Inventory inventory = Bukkit.createInventory(null, inventoryGenerator1.getSize(), inventoryGenerator1.getName());
            inventoryGenerator1.insertConsumerItems(inventory, this)
                    .forEach((integer1, playerConsumerFunction) -> itemStackFunctionMap.put(integer1, playerConsumerFunction.apply(player)));

            return inventory;
        }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
            throw new RuntimeException(e);
        }
    }

    public boolean isValidInventory(@NotNull InventoryView view) {
        Inventory topInventory = view.getTopInventory();

        for (ItemStack itemStack : topInventory) {
            if (itemStack == null)
                continue;

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null)
                return false;

            if (!itemMeta.getPersistentDataContainer().has(ITEM_ID, PersistentDataType.INTEGER))
                return false;
        }

        return true;
    }

    public BiConsumer<Inventory, ClickType> getConsumer(ItemStack itemStack) {
        return itemStackFunctionMap.get(getItemId(itemStack));
    }

    public Integer getItemId(ItemStack itemStack) {
        if (itemStack == null)
            return null;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null)
            return itemMeta.getPersistentDataContainer().get(ITEM_ID, PersistentDataType.INTEGER);

        return null;
    }

    public void onClose(Inventory inventory) {
        for (ItemStack itemStack : inventory)
            itemStackFunctionMap.remove(getItemId(itemStack));
    }

    public Pair<Integer, ItemStack> insertItemId(ItemStack itemStack) {
        return insertItemId(itemStack, getNextId());
    }

    public Pair<Integer, ItemStack> insertItemId(ItemStack itemStack, int i) {

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null)
            itemMeta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.INTEGER, i);

        itemStack.setItemMeta(itemMeta);
        return Pair.of(i, itemStack);

    }

    public int getNextId() {
        return integer.incrementAndGet();
    }

    public enum PlayerInventory {
        GAME(
                Pair.of(2, Items.RELOAD.getItemStack()),
                Pair.of(6, Items.LEAVE.getItemStack()),
                Pair.of(40, Items.SLIME_BALL.getItemStack())
        ),
        VIEWER(
                Pair.of(0, Items.WATCH_OTHER.getItemStack()),
                Pair.of(2, Items.START.getItemStack()),
                Pair.of(6, Items.TUTORIAL.getItemStack()),
                Pair.of(8, Items.SETTINGS.getItemStack())
        );

        private final ItemStack[] inventory;

        @SafeVarargs
        PlayerInventory(Pair<Integer, ItemStack>... items) {
            int highest = 0;

            for (Pair<Integer, ItemStack> item : items)
                if (highest < item.getFirst())
                    highest = item.getFirst();

            this.inventory = new ItemStack[highest + 1];
            for (Pair<Integer, ItemStack> item : items)
                this.inventory[item.getFirst()] = item.getSecond();
        }

        public void apply(Player player) {
            player.getInventory().setContents(this.inventory);
        }

        public enum Items {
            RELOAD(getReload()),
            LEAVE(getLeave()),
            SLIME_BALL(new ItemStack(Material.SLIME_BALL)),
            WATCH_OTHER(getWatchOthers()),
            START(getStartItem()),
            TUTORIAL(getTutorialBook()),
            SETTINGS(getSettings());


            private final ItemStack itemStack;

            Items(ItemStack itemStack) {
                this.itemStack = itemStack;
            }

            private static ItemStack getReload() {
                ItemStack reload = HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/e887cc388c8dcfcf1ba8aa5c3c102dce9cf7b1b63e786b34d4f1c3796d3e9d61");

                SkullMeta headMeta = (SkullMeta) reload.getItemMeta();
                if (headMeta != null)
                    headMeta.setDisplayName(ChatColor.YELLOW + Minesweeper.getPlugin(Minesweeper.class).getLanguage().getString("restart_pl"));
                reload.setItemMeta(headMeta);

                return reload;
            }

            private static ItemStack getLeave() {
                ItemStack barrier = new ItemStack(Material.BARRIER);

                ItemMeta barrierMeta = barrier.getItemMeta();
                if (barrierMeta != null)
                    barrierMeta.setDisplayName(ChatColor.DARK_RED + Minesweeper.getPlugin(Minesweeper.class).getLanguage().getString("leave"));
                barrier.setItemMeta(barrierMeta);

                return barrier;
            }

            private static ItemStack getWatchOthers() {
                ItemStack compass = new ItemStack(Material.COMPASS);

                ItemMeta compassMeta = compass.getItemMeta();
                if (compassMeta != null)
                    compassMeta.setDisplayName(ChatColor.GREEN + Minesweeper.getPlugin(Minesweeper.class).getLanguage().getString("watch_others"));
                compass.setItemMeta(compassMeta);

                return compass;
            }

            private static ItemStack getTutorialBook() {
                ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                Language language = Minesweeper.getPlugin(Minesweeper.class).getLanguage();

                String[] tutorialPages = new String[]{
                        language.getString("book_page_1"),
                        language.getString("book_page_2"),
                        language.getString("book_page_3")};

                BookMeta bookMeta = (BookMeta) book.getItemMeta();

                if (bookMeta != null) {
                    bookMeta.setDisplayName(ChatColor.YELLOW + language.getString("book_tutorial"));
                    bookMeta.setAuthor(ChatColor.AQUA + "TeddyBear_2004");
                    bookMeta.setTitle(language.getString("book_title"));
                    bookMeta.setPages(tutorialPages);
                }

                book.setItemMeta(bookMeta);

                return book;
            }

            private static ItemStack getStartItem() {
                ItemStack hourGlass = HeadGenerator.getHeadFromUrl("http://textures.minecraft.net/texture/b522cac48d1151b0a6eebb72fae26626c394fdc62d5b2064a69266e796a20268");

                SkullMeta timeMeta = (SkullMeta) hourGlass.getItemMeta();
                if (timeMeta != null)
                    timeMeta.setDisplayName(ChatColor.AQUA + Minesweeper.getPlugin(Minesweeper.class).getLanguage().getString("hour_glass_display_name"));

                hourGlass.setItemMeta(timeMeta);
                return hourGlass;
            }

            private static ItemStack getSettings() {
                ItemStack settings = HeadGenerator.getHeadFromUrl("https://textures.minecraft.net/texture/e4d49bae95c790c3b1ff5b2f01052a714d6185481d5b1c85930b3f99d2321674");

                SkullMeta settingsMeta = (SkullMeta) settings.getItemMeta();
                if (settingsMeta != null)
                    settingsMeta.setDisplayName(ChatColor.AQUA + Minesweeper.getPlugin(Minesweeper.class).getLanguage().getString("settings"));

                settings.setItemMeta(settingsMeta);
                return settings;
            }


            public ItemStack getItemStack() {
                return itemStack;
            }
        }
    }

}
