package de.teddy.minesweeper.game;

import org.bukkit.*;
import org.bukkit.BanList.Type;
import org.bukkit.Warning.WarningState;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.*;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.*;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.CachedServerIcon;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

class BoardTest {

    private static final Player[] dummies = new Player[]{
            new DummyPlayer(1),
            new DummyPlayer(2),
            new DummyPlayer(3)
    };

    @BeforeAll
    static void setUpBefore() {
        Board.notTest = false;
        Bukkit.setServer(new DummyServer());
    }

    @Test
    void test() {
        /*Game.MAP10X10.startGame(dummies[0]);
        Game.MAP10X10.startGame(dummies[1]);
        Game.MAP10X10.startGame(dummies[2]);
        assertNotNull(Game.getBoard(dummies[0]));
        assertNotNull(Game.getBoard(dummies[1]));
        assertNotNull(Game.getBoard(dummies[2]));

        Game.finishGame(dummies[0]);
        assertNull(Game.getBoard(dummies[0]));
        assertNotNull(Game.getGameWatched(dummies[0]));
        assertTrue(Game.getGame(dummies[0]).viewers.contains(dummies[0]));

        Game.finishGame(dummies[1]);
        Game.finishGame(dummies[2]);
        assertNull(Game.getBoard(dummies[0]));
        assertNull(Game.getBoard(dummies[1]));
        assertNull(Game.getBoard(dummies[2]));
        assertNull(Game.getGameWatched(dummies[0]));
        assertNull(Game.getGameWatched(dummies[1]));
        assertNull(Game.getGameWatched(dummies[2]));

        Game.MAP10X10.startGame(dummies[0]);
        assertNotNull(Game.getGameWatched(dummies[0]));
        assertNotNull(Game.getGameWatched(dummies[1]));
        assertNotNull(Game.getGameWatched(dummies[2]));
        assertTrue(Game.getGameWatched(dummies[0]).viewers.contains(dummies[0]));
        assertTrue(Game.getGameWatched(dummies[1]).viewers.contains(dummies[1]));
        assertTrue(Game.getGameWatched(dummies[2]).viewers.contains(dummies[2]));*/
    }

    private static class DummyPlayer implements Player {

        private final int id;

        public DummyPlayer(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PlayerInventory getInventory() {
            return new PlayerInventory() {

                @Override
                public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void remove(ItemStack item) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void remove(Material material) throws IllegalArgumentException {
                    // TODO Auto-generated method stub

                }

                @Override
                public ListIterator<ItemStack> iterator(int index) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ListIterator<ItemStack> iterator() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public boolean isEmpty() {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public List<HumanEntity> getViewers() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public InventoryType getType() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ItemStack[] getStorageContents() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setStorageContents(ItemStack[] items) throws IllegalArgumentException {
                    // TODO Auto-generated method stub

                }

                @Override
                public int getSize() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public int getMaxStackSize() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public void setMaxStackSize(int size) {
                    // TODO Auto-generated method stub

                }

                @Override
                public Location getLocation() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ItemStack getItem(int index) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ItemStack[] getContents() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setContents(ItemStack[] items) throws IllegalArgumentException {
                    // TODO Auto-generated method stub

                }

                @Override
                public int firstEmpty() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public int first(ItemStack item) {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public int first(Material material) throws IllegalArgumentException {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public boolean containsAtLeast(ItemStack item, int amount) {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public boolean contains(ItemStack item, int amount) {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public boolean contains(Material material, int amount) throws IllegalArgumentException {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public boolean contains(ItemStack item) {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public boolean contains(Material material) throws IllegalArgumentException {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public void clear(int index) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void clear() {
                    // TODO Auto-generated method stub

                }

                @Override
                public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setItem(EquipmentSlot slot, ItemStack item) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void setItem(int index, ItemStack item) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack getLeggings() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setLeggings(ItemStack leggings) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack getItemInOffHand() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setItemInOffHand(ItemStack item) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack getItemInMainHand() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setItemInMainHand(ItemStack item) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack getItemInHand() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setItemInHand(ItemStack stack) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack getItem(EquipmentSlot slot) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public HumanEntity getHolder() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ItemStack getHelmet() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setHelmet(ItemStack helmet) {
                    // TODO Auto-generated method stub

                }

                @Override
                public int getHeldItemSlot() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                @Override
                public void setHeldItemSlot(int slot) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack[] getExtraContents() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setExtraContents(ItemStack[] items) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack getChestplate() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setChestplate(ItemStack chestplate) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack getBoots() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setBoots(ItemStack boots) {
                    // TODO Auto-generated method stub

                }

                @Override
                public ItemStack[] getArmorContents() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void setArmorContents(ItemStack[] items) {
                    // TODO Auto-generated method stub

                }
            };
        }

        @Override
        public Inventory getEnderChest() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MainHand getMainHand() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean setWindowProperty(Property prop, int value) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public InventoryView getOpenInventory() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InventoryView openInventory(Inventory inventory) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InventoryView openWorkbench(Location location, boolean force) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InventoryView openEnchanting(Location location, boolean force) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void openInventory(InventoryView inventory) {
            // TODO Auto-generated method stub

        }

        @Override
        public InventoryView openMerchant(Villager trader, boolean force) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public InventoryView openMerchant(Merchant merchant, boolean force) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void closeInventory() {
            // TODO Auto-generated method stub

        }

        @Override
        public ItemStack getItemInHand() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setItemInHand(ItemStack item) {
            // TODO Auto-generated method stub

        }

        @Override
        public ItemStack getItemOnCursor() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setItemOnCursor(ItemStack item) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean hasCooldown(Material material) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int getCooldown(Material material) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setCooldown(Material material, int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getSleepTicks() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean sleep(Location location, boolean force) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void wakeup(boolean setSpawnLocation) {
            // TODO Auto-generated method stub

        }

        @Override
        public Location getBedLocation() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public GameMode getGameMode() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setGameMode(GameMode mode) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isBlocking() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isHandRaised() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public ItemStack getItemInUse() {
            return null;
        }

        @Override
        public int getExpToLevel() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public float getAttackCooldown() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean discoverRecipe(NamespacedKey recipe) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int discoverRecipes(Collection<NamespacedKey> recipes) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean undiscoverRecipe(NamespacedKey recipe) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean hasDiscoveredRecipe(NamespacedKey recipe) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Set<NamespacedKey> getDiscoveredRecipes() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Entity getShoulderEntityLeft() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setShoulderEntityLeft(Entity entity) {
            // TODO Auto-generated method stub

        }

        @Override
        public Entity getShoulderEntityRight() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setShoulderEntityRight(Entity entity) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean dropItem(boolean dropAll) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public float getExhaustion() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setExhaustion(float value) {
            // TODO Auto-generated method stub

        }

        @Override
        public float getSaturation() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setSaturation(float value) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getFoodLevel() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setFoodLevel(int value) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getSaturatedRegenRate() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setSaturatedRegenRate(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getUnsaturatedRegenRate() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setUnsaturatedRegenRate(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getStarvationRate() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setStarvationRate(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public Location getLastDeathLocation() {
            return null;
        }

        @Override
        public void setLastDeathLocation(Location location) {

        }

        @Override
        public Firework fireworkBoost(ItemStack fireworkItemStack) {
            return null;
        }

        @Override
        public double getEyeHeight() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getEyeHeight(boolean ignorePose) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Location getEyeLocation() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Block> getLineOfSight(Set<Material> transparent, int maxDistance) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Block getTargetBlock(Set<Material> transparent, int maxDistance) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Block> getLastTwoTargetBlocks(Set<Material> transparent, int maxDistance) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Block getTargetBlockExact(int maxDistance) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Block getTargetBlockExact(int maxDistance, FluidCollisionMode fluidCollisionMode) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public RayTraceResult rayTraceBlocks(double maxDistance) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public RayTraceResult rayTraceBlocks(double maxDistance, FluidCollisionMode fluidCollisionMode) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getRemainingAir() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setRemainingAir(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getMaximumAir() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setMaximumAir(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getArrowCooldown() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setArrowCooldown(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getArrowsInBody() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setArrowsInBody(int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getMaximumNoDamageTicks() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setMaximumNoDamageTicks(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public double getLastDamage() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setLastDamage(double damage) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getNoDamageTicks() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setNoDamageTicks(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public Player getKiller() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean addPotionEffect(PotionEffect effect) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean addPotionEffect(PotionEffect effect, boolean force) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean addPotionEffects(Collection<PotionEffect> effects) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean hasPotionEffect(PotionEffectType type) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public PotionEffect getPotionEffect(PotionEffectType type) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removePotionEffect(PotionEffectType type) {
            // TODO Auto-generated method stub

        }

        @Override
        public Collection<PotionEffect> getActivePotionEffects() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean hasLineOfSight(Entity other) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean getRemoveWhenFarAway() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setRemoveWhenFarAway(boolean remove) {
            // TODO Auto-generated method stub

        }

        @Override
        public EntityEquipment getEquipment() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean getCanPickupItems() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setCanPickupItems(boolean pickup) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isLeashed() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Entity getLeashHolder() throws IllegalStateException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean setLeashHolder(Entity holder) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isGliding() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setGliding(boolean gliding) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isSwimming() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setSwimming(boolean swimming) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isRiptiding() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isSleeping() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isClimbing() {
            return false;
        }

        @Override
        public void setAI(boolean ai) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean hasAI() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void attack(Entity target) {
            // TODO Auto-generated method stub

        }

        @Override
        public void swingMainHand() {
            // TODO Auto-generated method stub

        }

        @Override
        public void swingOffHand() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isCollidable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setCollidable(boolean collidable) {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<UUID> getCollidableExemptions() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> T getMemory(MemoryKey<T> memoryKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> void setMemory(MemoryKey<T> memoryKey, T memoryValue) {
            // TODO Auto-generated method stub

        }

        @Override
        public Sound getHurtSound() {
            return null;
        }

        @Override
        public Sound getDeathSound() {
            return null;
        }

        @Override
        public Sound getFallDamageSound(int fallHeight) {
            return null;
        }

        @Override
        public Sound getFallDamageSoundSmall() {
            return null;
        }

        @Override
        public Sound getFallDamageSoundBig() {
            return null;
        }

        @Override
        public Sound getDrinkingSound(ItemStack itemStack) {
            return null;
        }

        @Override
        public Sound getEatingSound(ItemStack itemStack) {
            return null;
        }

        @Override
        public boolean canBreatheUnderwater() {
            return false;
        }

        @Override
        public EntityCategory getCategory() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isInvisible() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setInvisible(boolean invisible) {
            // TODO Auto-generated method stub

        }

        @Override
        public AttributeInstance getAttribute(Attribute attribute) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void damage(double amount) {
            // TODO Auto-generated method stub

        }

        @Override
        public void damage(double amount, Entity source) {
            // TODO Auto-generated method stub

        }

        @Override
        public double getHealth() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setHealth(double health) {
            // TODO Auto-generated method stub

        }

        @Override
        public double getAbsorptionAmount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setAbsorptionAmount(double amount) {
            // TODO Auto-generated method stub

        }

        @Override
        public double getMaxHealth() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setMaxHealth(double health) {
            // TODO Auto-generated method stub

        }

        @Override
        public void resetMaxHealth() {
            // TODO Auto-generated method stub

        }

        @Override
        public Location getLocation() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Location getLocation(Location loc) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Vector getVelocity() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setVelocity(Vector velocity) {
            // TODO Auto-generated method stub

        }

        @Override
        public double getHeight() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getWidth() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public BoundingBox getBoundingBox() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isInWater() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public World getWorld() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setRotation(float yaw, float pitch) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean teleport(Location location) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean teleport(Location location, TeleportCause cause) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean teleport(Entity destination) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean teleport(Entity destination, TeleportCause cause) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public List<Entity> getNearbyEntities(double x, double y, double z) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getEntityId() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getFireTicks() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setFireTicks(int ticks) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getMaxFireTicks() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean isVisualFire() {
            return false;
        }

        @Override
        public void setVisualFire(boolean fire) {

        }

        @Override
        public int getFreezeTicks() {
            return 0;
        }

        @Override
        public void setFreezeTicks(int ticks) {

        }

        @Override
        public int getMaxFreezeTicks() {
            return 0;
        }

        @Override
        public boolean isFrozen() {
            return false;
        }

        @Override
        public void remove() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isDead() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValid() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Server getServer() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isPersistent() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setPersistent(boolean persistent) {
            // TODO Auto-generated method stub

        }

        @Override
        public Entity getPassenger() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean setPassenger(Entity passenger) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public List<Entity> getPassengers() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean addPassenger(Entity passenger) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean removePassenger(Entity passenger) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isEmpty() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean eject() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public float getFallDistance() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setFallDistance(float distance) {
            // TODO Auto-generated method stub

        }

        @Override
        public EntityDamageEvent getLastDamageCause() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setLastDamageCause(EntityDamageEvent event) {
            // TODO Auto-generated method stub

        }

        @Override
        public UUID getUniqueId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PlayerProfile getPlayerProfile() {
            return null;
        }

        @Override
        public int getTicksLived() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setTicksLived(int value) {
            // TODO Auto-generated method stub

        }

        @Override
        public void playEffect(EntityEffect type) {
            // TODO Auto-generated method stub

        }

        @Override
        public EntityType getType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Sound getSwimSound() {
            return null;
        }

        @Override
        public Sound getSwimSplashSound() {
            return null;
        }

        @Override
        public Sound getSwimHighSpeedSplashSound() {
            return null;
        }

        @Override
        public boolean isInsideVehicle() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean leaveVehicle() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Entity getVehicle() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isCustomNameVisible() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setCustomNameVisible(boolean flag) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isGlowing() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setGlowing(boolean flag) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isInvulnerable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setInvulnerable(boolean flag) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isSilent() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setSilent(boolean flag) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean hasGravity() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setGravity(boolean gravity) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getPortalCooldown() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setPortalCooldown(int cooldown) {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<String> getScoreboardTags() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean addScoreboardTag(String tag) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean removeScoreboardTag(String tag) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public PistonMoveReaction getPistonMoveReaction() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BlockFace getFacing() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Pose getPose() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public SpawnCategory getSpawnCategory() {
            return null;
        }

        @Override
        public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
            // TODO Auto-generated method stub

        }

        @Override
        public List<MetadataValue> getMetadata(String metadataKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean hasMetadata(String metadataKey) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void removeMetadata(String metadataKey, Plugin owningPlugin) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendMessage(String message) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendMessage(String[] messages) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendMessage(UUID sender, String message) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendMessage(UUID sender, String[] messages) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isPermissionSet(String name) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isPermissionSet(Permission perm) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean hasPermission(String name) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean hasPermission(Permission perm) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removeAttachment(PermissionAttachment attachment) {
            // TODO Auto-generated method stub

        }

        @Override
        public void recalculatePermissions() {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isOp() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setOp(boolean value) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getCustomName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setCustomName(String name) {
            // TODO Auto-generated method stub

        }

        @Override
        public PersistentDataContainer getPersistentDataContainer() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isConversing() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void acceptConversationInput(String input) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean beginConversation(Conversation conversation) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void abandonConversation(Conversation conversation) {
            // TODO Auto-generated method stub

        }

        @Override
        public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendRawMessage(UUID sender, String message) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isOnline() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isBanned() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isWhitelisted() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setWhitelisted(boolean value) {
            // TODO Auto-generated method stub

        }

        @Override
        public Player getPlayer() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getFirstPlayed() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getLastPlayed() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean hasPlayedBefore() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public int getStatistic(Statistic statistic) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void incrementStatistic(Statistic statistic, Material material, int amount)
                throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void decrementStatistic(Statistic statistic, Material material, int amount)
                throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void incrementStatistic(Statistic statistic, EntityType entityType, int amount)
                throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
            // TODO Auto-generated method stub

        }

        @Override
        public Map<String, Object> serialize() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void sendPluginMessage(Plugin source, String channel, byte[] message) {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<String> getListeningPluginChannels() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getDisplayName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setDisplayName(String name) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getPlayerListName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setPlayerListName(String name) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getPlayerListHeader() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setPlayerListHeader(String header) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getPlayerListFooter() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setPlayerListFooter(String footer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setPlayerListHeaderFooter(String header, String footer) {
            // TODO Auto-generated method stub

        }

        @Override
        public Location getCompassTarget() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setCompassTarget(Location loc) {
            // TODO Auto-generated method stub

        }

        @Override
        public InetSocketAddress getAddress() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void sendRawMessage(String message) {
            // TODO Auto-generated method stub

        }

        @Override
        public void kickPlayer(String message) {
            // TODO Auto-generated method stub

        }

        @Override
        public void chat(String msg) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean performCommand(String command) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isOnGround() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isSneaking() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setSneaking(boolean sneak) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isSprinting() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setSprinting(boolean sprinting) {
            // TODO Auto-generated method stub

        }

        @Override
        public void saveData() {
            // TODO Auto-generated method stub

        }

        @Override
        public void loadData() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isSleepingIgnored() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setSleepingIgnored(boolean isSleeping) {
            // TODO Auto-generated method stub

        }

        @Override
        public Location getBedSpawnLocation() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setBedSpawnLocation(Location location) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setBedSpawnLocation(Location location, boolean force) {
            // TODO Auto-generated method stub

        }

        @Override
        public void playNote(Location loc, byte instrument, byte note) {
            // TODO Auto-generated method stub

        }

        @Override
        public void playNote(Location loc, Instrument instrument, Note note) {
            // TODO Auto-generated method stub

        }

        @Override
        public void playSound(Location location, Sound sound, float volume, float pitch) {
            // TODO Auto-generated method stub

        }

        @Override
        public void playSound(Location location, String sound, float volume, float pitch) {
            // TODO Auto-generated method stub

        }

        @Override
        public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {
            // TODO Auto-generated method stub

        }

        @Override
        public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {
            // TODO Auto-generated method stub

        }

        @Override
        public void playSound(Entity entity, Sound sound, float volume, float pitch) {

        }

        @Override
        public void playSound(Entity entity, Sound sound, SoundCategory category, float volume, float pitch) {

        }

        @Override
        public void stopSound(Sound sound) {
            // TODO Auto-generated method stub

        }

        @Override
        public void stopSound(String sound) {
            // TODO Auto-generated method stub

        }

        @Override
        public void stopSound(Sound sound, SoundCategory category) {
            // TODO Auto-generated method stub

        }

        @Override
        public void stopSound(String sound, SoundCategory category) {
            // TODO Auto-generated method stub

        }

        @Override
        public void stopSound(SoundCategory category) {

        }

        @Override
        public void stopAllSounds() {

        }

        @Override
        public void playEffect(Location loc, Effect effect, int data) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T> void playEffect(Location loc, Effect effect, T data) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean breakBlock(Block block) {
            return false;
        }

        @Override
        public void sendBlockChange(Location loc, Material material, byte data) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendBlockChange(Location loc, BlockData block) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendBlockChanges(Collection<BlockState> blocks, boolean suppressLightUpdates) {

        }

        @Override
        public void sendBlockDamage(Location loc, float progress) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendEquipmentChange(LivingEntity entity, EquipmentSlot slot, ItemStack item) {

        }

        @Override
        public void sendSignChange(Location loc, String[] lines) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendSignChange(Location loc, String[] lines, DyeColor dyeColor, boolean hasGlowingText) throws IllegalArgumentException {

        }

        @Override
        public void sendMap(MapView map) {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateInventory() {
            // TODO Auto-generated method stub

        }

        @Override
        public GameMode getPreviousGameMode() {
            return null;
        }

        @Override
        public void setPlayerTime(long time, boolean relative) {
            // TODO Auto-generated method stub

        }

        @Override
        public long getPlayerTime() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getPlayerTimeOffset() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean isPlayerTimeRelative() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void resetPlayerTime() {
            // TODO Auto-generated method stub

        }

        @Override
        public WeatherType getPlayerWeather() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setPlayerWeather(WeatherType type) {
            // TODO Auto-generated method stub

        }

        @Override
        public void resetPlayerWeather() {
            // TODO Auto-generated method stub

        }

        @Override
        public void giveExp(int amount) {
            // TODO Auto-generated method stub

        }

        @Override
        public void giveExpLevels(int amount) {
            // TODO Auto-generated method stub

        }

        @Override
        public float getExp() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setExp(float exp) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getLevel() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setLevel(int level) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getTotalExperience() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setTotalExperience(int exp) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendExperienceChange(float progress) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendExperienceChange(float progress, int level) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean getAllowFlight() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setAllowFlight(boolean flight) {
            // TODO Auto-generated method stub

        }

        @Override
        public void hidePlayer(Player player) {
            // TODO Auto-generated method stub

        }

        @Override
        public void hidePlayer(Plugin plugin, Player player) {
            // TODO Auto-generated method stub

        }

        @Override
        public void showPlayer(Player player) {
            // TODO Auto-generated method stub

        }

        @Override
        public void showPlayer(Plugin plugin, Player player) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean canSee(Player player) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void hideEntity(Plugin plugin, Entity entity) {

        }

        @Override
        public void showEntity(Plugin plugin, Entity entity) {

        }

        @Override
        public boolean canSee(Entity entity) {
            return false;
        }

        @Override
        public boolean isFlying() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setFlying(boolean value) {
            // TODO Auto-generated method stub

        }

        @Override
        public float getFlySpeed() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setFlySpeed(float value) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public float getWalkSpeed() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setWalkSpeed(float value) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public void setTexturePack(String url) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setResourcePack(String url) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setResourcePack(String url, byte[] hash) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setResourcePack(String url, byte[] hash, String prompt) {

        }

        @Override
        public void setResourcePack(String url, byte[] hash, boolean force) {

        }

        @Override
        public void setResourcePack(String url, byte[] hash, String prompt, boolean force) {

        }

        @Override
        public Scoreboard getScoreboard() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException {
            // TODO Auto-generated method stub

        }

        @Override
        public WorldBorder getWorldBorder() {
            return null;
        }

        @Override
        public void setWorldBorder(WorldBorder border) {

        }

        @Override
        public boolean isHealthScaled() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setHealthScaled(boolean scale) {
            // TODO Auto-generated method stub

        }

        @Override
        public double getHealthScale() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setHealthScale(double scale) throws IllegalArgumentException {
            // TODO Auto-generated method stub

        }

        @Override
        public Entity getSpectatorTarget() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setSpectatorTarget(Entity entity) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendTitle(String title, String subtitle) {
            // TODO Auto-generated method stub

        }

        @Override
        public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
            // TODO Auto-generated method stub

        }

        @Override
        public void resetTitle() {
            // TODO Auto-generated method stub

        }

        @Override
        public void spawnParticle(Particle particle, Location location, int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public void spawnParticle(Particle particle, double x, double y, double z, int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
            // TODO Auto-generated method stub

        }

        @Override
        public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
                                  double offsetZ) {
            // TODO Auto-generated method stub

        }

        @Override
        public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
                                  double offsetY, double offsetZ) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
                                      double offsetZ, T data) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
                                      double offsetY, double offsetZ, T data) {
            // TODO Auto-generated method stub

        }

        @Override
        public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
                                  double offsetZ, double extra) {
            // TODO Auto-generated method stub

        }

        @Override
        public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
                                  double offsetY, double offsetZ, double extra) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
                                      double offsetZ, double extra, T data) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
                                      double offsetY, double offsetZ, double extra, T data) {
            // TODO Auto-generated method stub

        }

        @Override
        public AdvancementProgress getAdvancementProgress(Advancement advancement) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getClientViewDistance() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getPing() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getLocale() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void updateCommands() {
            // TODO Auto-generated method stub

        }

        @Override
        public void openBook(ItemStack book) {
            // TODO Auto-generated method stub

        }

        @Override
        public void openSign(Sign sign) {

        }

        @Override
        public void showDemoScreen() {

        }

        @Override
        public boolean isAllowingServerListings() {
            return false;
        }

        @Override
        public Spigot spigot() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private static class DummyServer implements Server {

        @Override
        public void sendPluginMessage(Plugin source, String channel, byte[] message) {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<String> getListeningPluginChannels() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getVersion() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getBukkitVersion() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<? extends Player> getOnlinePlayers() {
            return Arrays.asList(dummies);
        }

        @Override
        public int getMaxPlayers() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getPort() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getViewDistance() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getSimulationDistance() {
            return 0;
        }

        @Override
        public String getIp() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getWorldType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean getGenerateStructures() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int getMaxWorldSize() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean getAllowEnd() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean getAllowNether() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String getResourcePack() {
            return null;
        }

        @Override
        public String getResourcePackHash() {
            return null;
        }

        @Override
        public String getResourcePackPrompt() {
            return null;
        }

        @Override
        public boolean isResourcePackRequired() {
            return false;
        }

        @Override
        public boolean hasWhitelist() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setWhitelist(boolean value) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isWhitelistEnforced() {
            return false;
        }

        @Override
        public void setWhitelistEnforced(boolean value) {

        }

        @Override
        public Set<OfflinePlayer> getWhitelistedPlayers() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void reloadWhitelist() {
            // TODO Auto-generated method stub

        }

        @Override
        public int broadcastMessage(String message) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String getUpdateFolder() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public File getUpdateFolderFile() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getConnectionThrottle() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getTicksPerAnimalSpawns() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getTicksPerMonsterSpawns() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getTicksPerWaterSpawns() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getTicksPerWaterAmbientSpawns() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getTicksPerWaterUndergroundCreatureSpawns() {
            return 0;
        }

        @Override
        public int getTicksPerAmbientSpawns() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getTicksPerSpawns(SpawnCategory spawnCategory) {
            return 0;
        }

        @Override
        public Player getPlayer(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Player getPlayerExact(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Player> matchPlayer(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Player getPlayer(UUID id) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PluginManager getPluginManager() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BukkitScheduler getScheduler() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ServicesManager getServicesManager() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<World> getWorlds() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public World createWorld(WorldCreator creator) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean unloadWorld(String name, boolean save) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean unloadWorld(World world, boolean save) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public World getWorld(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public World getWorld(UUID uid) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public WorldBorder createWorldBorder() {
            return null;
        }

        @Override
        public MapView getMap(int id) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public MapView createMap(World world) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ItemStack createExplorerMap(World world, Location location, StructureType structureType) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ItemStack createExplorerMap(World world, Location location, StructureType structureType, int radius,
                                           boolean findUnexplored) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void reload() {
            // TODO Auto-generated method stub

        }

        @Override
        public void reloadData() {
            // TODO Auto-generated method stub

        }

        @Override
        public Logger getLogger() {
            return Logger.getLogger("Test");
        }

        @Override
        public PluginCommand getPluginCommand(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void savePlayers() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean dispatchCommand(CommandSender sender, String commandLine) throws CommandException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean addRecipe(Recipe recipe) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public List<Recipe> getRecipesFor(ItemStack result) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Recipe getRecipe(NamespacedKey recipeKey) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Recipe getCraftingRecipe(ItemStack[] craftingMatrix, World world) {
            return null;
        }

        @Override
        public ItemStack craftItem(ItemStack[] craftingMatrix, World world, Player player) {
            return null;
        }

        @Override
        public Iterator<Recipe> recipeIterator() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void clearRecipes() {
            // TODO Auto-generated method stub

        }

        @Override
        public void resetRecipes() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean removeRecipe(NamespacedKey key) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Map<String, String[]> getCommandAliases() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getSpawnRadius() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setSpawnRadius(int value) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean shouldSendChatPreviews() {
            return false;
        }

        @Override
        public boolean isEnforcingSecureProfiles() {
            return false;
        }

        @Override
        public boolean getHideOnlinePlayers() {
            return false;
        }

        @Override
        public boolean getOnlineMode() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean getAllowFlight() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isHardcore() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void shutdown() {
            // TODO Auto-generated method stub

        }

        @Override
        public int broadcast(String message, String permission) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public OfflinePlayer getOfflinePlayer(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public OfflinePlayer getOfflinePlayer(UUID id) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PlayerProfile createPlayerProfile(UUID uniqueId, String name) {
            return null;
        }

        @Override
        public PlayerProfile createPlayerProfile(UUID uniqueId) {
            return null;
        }

        @Override
        public PlayerProfile createPlayerProfile(String name) {
            return null;
        }

        @Override
        public Set<String> getIPBans() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void banIP(String address) {
            // TODO Auto-generated method stub

        }

        @Override
        public void unbanIP(String address) {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<OfflinePlayer> getBannedPlayers() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BanList getBanList(Type type) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Set<OfflinePlayer> getOperators() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public GameMode getDefaultGameMode() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setDefaultGameMode(GameMode mode) {
            // TODO Auto-generated method stub

        }

        @Override
        public ConsoleCommandSender getConsoleSender() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public File getWorldContainer() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public OfflinePlayer[] getOfflinePlayers() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Messenger getMessenger() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public HelpMap getHelpMap() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Inventory createInventory(InventoryHolder owner, InventoryType type) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Inventory createInventory(InventoryHolder owner, InventoryType type, String title) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Inventory createInventory(InventoryHolder owner, int size) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Inventory createInventory(InventoryHolder owner, int size, String title)
                throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Merchant createMerchant(String title) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getMaxChainedNeighborUpdates() {
            return 0;
        }

        @Override
        public int getMonsterSpawnLimit() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getAnimalSpawnLimit() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getWaterAnimalSpawnLimit() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getWaterAmbientSpawnLimit() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getWaterUndergroundCreatureSpawnLimit() {
            return 0;
        }

        @Override
        public int getAmbientSpawnLimit() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getSpawnLimit(SpawnCategory spawnCategory) {
            return 0;
        }

        @Override
        public boolean isPrimaryThread() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String getMotd() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getShutdownMessage() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public WarningState getWarningState() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ItemFactory getItemFactory() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ScoreboardManager getScoreboardManager() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Criteria getScoreboardCriteria(String name) {
            return null;
        }

        @Override
        public CachedServerIcon getServerIcon() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public CachedServerIcon loadServerIcon(File file) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public CachedServerIcon loadServerIcon(BufferedImage image) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getIdleTimeout() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setIdleTimeout(int threshold) {
            // TODO Auto-generated method stub

        }

        @Override
        public ChunkData createChunkData(World world) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BossBar createBossBar(String title, BarColor color, BarStyle style, BarFlag... flags) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public KeyedBossBar createBossBar(NamespacedKey key, String title, BarColor color, BarStyle style,
                                          BarFlag... flags) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterator<KeyedBossBar> getBossBars() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public KeyedBossBar getBossBar(NamespacedKey key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean removeBossBar(NamespacedKey key) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Entity getEntity(UUID uuid) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Advancement getAdvancement(NamespacedKey key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterator<Advancement> advancementIterator() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BlockData createBlockData(Material material) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BlockData createBlockData(Material material, Consumer<BlockData> consumer) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BlockData createBlockData(String data) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BlockData createBlockData(Material material, String data) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends Keyed> Tag<T> getTag(String registry, NamespacedKey tag, Class<T> clazz) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends Keyed> Iterable<Tag<T>> getTags(String registry, Class<T> clazz) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public LootTable getLootTable(NamespacedKey key) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Entity> selectEntities(CommandSender sender, String selector) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public StructureManager getStructureManager() {
            return null;
        }

        @Override
        public <T extends Keyed> Registry<T> getRegistry(Class<T> tClass) {
            return null;
        }

        @Override
        public UnsafeValues getUnsafe() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Spigot spigot() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
