package net.frozenorb.Raven.Listeners;

import mkremins.fanciful.FancyMessage;
import net.frozenorb.Network.Events.PlayerTabCompleteCommandEvent;
import net.frozenorb.Raven.CommandSystem.Commands.Potato;
import net.frozenorb.Raven.CommandSystem.Commands.ToggleEnd;
import net.frozenorb.Raven.CommandSystem.SpawnCommand;
import net.frozenorb.Raven.CommandSystem.WarpCommand;
import net.frozenorb.Raven.Events.TeamEvent;
import net.frozenorb.Raven.Managers.ServerManager;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Tracking.Tracker;
import net.frozenorb.Raven.Tracking.TrackerFactory;
import net.frozenorb.Raven.Traps.Trap;
import net.frozenorb.Raven.Types.*;
import net.frozenorb.Raven.Utilities.ItemUtils;
import net.frozenorb.Raven.Visual.SpawnCooldown;
import net.frozenorb.Raven.Visual.TimeLock;
import net.frozenorb.Utilities.DataSystem.Regioning.RegionManager;
import net.frozenorb.Utilities.Message.*;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mBasic.Utilities.NbtFactory;
import net.frozenorb.mCommon.Common;
import net.frozenorb.mCommon.Types.User;
import net.frozenorb.mShared.Shared;
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftMushroomCow;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;

@SuppressWarnings("deprecation")
public class GeneralListener implements Listener {
    public static HashMap<String, CombatLogRunnable> combatLogRunnables = new HashMap<String, CombatLogRunnable>();

    public static final Integer[] DEBUFF_POTION_VALUES = {24620, 16428, 16460,
            16388, 16420, 16452, 16456, 16424, 16426, 16458};
    private static final Integer[] DAMAGE_POTION_IDS = {16460, 16428};

    private RavenPlugin plugin;

    public static int VIP_CAP = 200;
    public static int MVP_CAP = 250;

    private static final int COMBAT_LOG_TIME = 60;

    public static boolean CAP_ENABLED = true;

    private final static Material[] INTERACTABLE_WITH_XP_BOTTLE = new Material[]{
            Material.CHEST, Material.TRAPPED_CHEST, Material.DISPENSER,
            Material.WORKBENCH};

    public GeneralListener(RavenPlugin plugin) {
        this.plugin = plugin;
    }

    private HashMap<String, Long> enderPearlCD = new HashMap<String, Long>();
    private HashMap<String, Long> lastMsgSent = new HashMap<String, Long>();

    private static final Material[] TRAP_BLOCKS = {Material.ANVIL,
            Material.ENCHANTMENT_TABLE, Material.DIAMOND_BLOCK,
            Material.IRON_BLOCK, Material.CHEST, Material.TRAPPED_CHEST,
            Material.FURNACE, Material.DROPPER, Material.DISPENSER};

    private final Material[] VALID_ARMOR = {
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,

            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS
    };

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        Entity ent = e.getEntity();
        if (e.getEntityType() == EntityType.ENDER_PEARL) {
            Player shooter = (Player) e.getEntity().getShooter();

            if (ent.getWorld().getEnvironment() == Environment.THE_END) {
                EnderPearl ep = (EnderPearl) ent;
                if (timeLocks.containsKey(shooter.getName()) && !timeLocks.get(shooter.getName()).done()) {
                    BlockIterator bi = new BlockIterator(ent.getWorld(), ep.getLocation().toVector(), ep.getVelocity(), 0, 80);

                    while (bi.hasNext()) {
                        if (bi.next().getTypeId() == 36) {
                            ep.remove();
                            break;
                        }
                    }
                }
            }

            if (enderPearlCD.containsKey(shooter.getName()) && enderPearlCD.get(shooter.getName()) > System.currentTimeMillis()) {
                e.setCancelled(true);

                long then = enderPearlCD.get(shooter.getName());
                long millisLeft = then - System.currentTimeMillis();

                double value = (millisLeft / 1000D);
                double d = Math.round(10.0 * value) / 10.0;

                String msg = "§cYou cannot use this for another §l" + d + "§c seconds!";
                shooter.sendMessage(msg);
                shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                shooter.updateInventory();
            } else {
                enderPearlCD.put(shooter.getName(), System.currentTimeMillis() + 16000);
            }
        }
    }

    // @EventHandler
    // public void onPlayerProfileRequestColors(PlayerProfileSetColorEvent e) {
    // String tag = null;
    // if (plugin.getTeamManager().isOnTeam(e.getProfile().getName())) {
    // net.frozenorb.Raven.Team.Team t =
    // plugin.getTeamManager().getPlayerTeam(e.getProfile().getName());
    // if (t.getTag() != null) {
    // tag = t.getTag();
    // }
    // }
    // e.setSlot(NameSlot.ABOVE_HEAD);
    // e.setPrefix(tag);
    // }

    /**
     * Stops Withers from being able to break Obsidian blocks (and allowing for
     * Obsidian farms).
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.WITHER && event.getBlock().getType() == Material.OBSIDIAN) {
            event.setCancelled(true);
        }
    }

    /**
     * Stop Zombie Pigmen from spawning in the Overworld near nether portals
     * (and allowing for Zombie Pigmen farms).
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onZombiePigmenSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.PIG_ZOMBIE && event.getLocation().getWorld().getEnvironment() == Environment.NORMAL && event.getSpawnReason() != SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    /**
     * Stop players from placing blocks above the bedrock ceiling in the nether.
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceAboveNether(BlockPlaceEvent event) {
        if (isAboveNether(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    /**
     * Stop players from breaking blocks above the bedrock ceiling in the
     * nether.
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakAboveNether(BlockBreakEvent event) {
        if (isAboveNether(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    /**
     * Stop players from interacting with blocks above the bedrock ceiling in
     * the nether.
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAboveNether(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && isAboveNether(clickedBlock.getLocation())) {
            event.setCancelled(true);
        }
    }

    /**
     * @param location
     * @return true, if the location is above the bedrock ceiling in the nether,
     * false otherwise
     */
    public boolean isAboveNether(Location location) {
        return location.getWorld().getEnvironment() == Environment.NETHER && location.getY() >= 128;
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
//        for (LivingEntity le : e.getAffectedEntities()) {
//            if (le instanceof Player) {
//                if (RavenPlugin.get().getScoreboardTask() != null) {
//                    RavenPlugin.get().getScoreboardTask().updatePlayerLater((Player) le);
//                }
//            }
//        }
        Player shooter = (Player) e.getPotion().getShooter();

        Iterator<LivingEntity> iter = e.getAffectedEntities().iterator();

        while (iter.hasNext()) {
            LivingEntity ent = iter.next();

            if (ent instanceof Player) {
                Player p = (Player) ent;

                if (!RavenPlugin.spawnprot.contains(p.getName())) {

                    if (RavenPlugin.spawnprot.contains(shooter.getName())) {
                        RavenPlugin.spawnprot.remove(shooter.getName());

                        shooter.sendMessage(ChatColor.GRAY + "You no longer have spawn protection.");
                    }
                } else {

                    if (Arrays.asList(DEBUFF_POTION_VALUES).contains(Integer.valueOf(e.getEntity().getItem().getDurability()))) {
                        iter.remove();
                        e.setIntensity(p, 0D);

                    }
                }
            }

        }

    }

    @EventHandler
    public void onPlayerConsumeItem(PlayerItemConsumeEvent e) {
//        if (RavenPlugin.get().getScoreboardTask() != null) {
//            RavenPlugin.get().getScoreboardTask().updatePlayerLater(e.getPlayer());
//        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntityMonitor(EntityDamageByEntityEvent e) {
        if (((e.getEntity() instanceof Player)) && ((e.getDamager() instanceof Player))) {
            {
                final Player p = (Player) e.getEntity();
                if (!combatLogRunnables.containsKey(p.getName())) {
                    CombatLogRunnable c = new CombatLogRunnable(p, COMBAT_LOG_TIME) {

                        @Override
                        public void onFinish() {
                            combatLogRunnables.remove(p.getName());

                        }
                    };
                    c.runTaskTimer(plugin, 20L, 20L);
                    combatLogRunnables.put(p.getName(), c);
                } else {
                    ((CombatLogRunnable) combatLogRunnables.get(p.getName())).setTime(COMBAT_LOG_TIME);
                }
            }
            {
                final Player p = (Player) e.getDamager();
                if (!combatLogRunnables.containsKey(p.getName())) {
                    CombatLogRunnable c = new CombatLogRunnable(p, COMBAT_LOG_TIME) {

                        @Override
                        public void onFinish() {
                            combatLogRunnables.remove(p.getName());

                        }
                    };
                    c.runTaskTimer(plugin, 20L, 20L);
                    combatLogRunnables.put(p.getName(), c);
                } else {
                    ((CombatLogRunnable) combatLogRunnables.get(p.getName())).setTime(COMBAT_LOG_TIME);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        Potato.potatoSet.remove(e.getPlayer().getName().toLowerCase());
        WarpCommand.confirms.remove(e.getPlayer().getName());
//        if (RavenPlugin.get().getScoreboardTask() != null) {
//
//        }
        RavenPlugin.get().getBossBarManager().unregisterPlayer(e.getPlayer());

        if (combatLogRunnables.containsKey(e.getPlayer().getName())) {
            if (!plugin.getServerManager().safeToLogout(e.getPlayer())) {
                e.getPlayer().setHealth(0D);
                return;
            }
        }
        if (e.getPlayer().getWorld().getEnvironment() == Environment.THE_END) {
            if (((Damageable) e.getPlayer()).getHealth() > 0D) {
                e.getPlayer().setHealth(0D);
            }
        }

    }

    //   @EventHandler(priority = EventPriority.MONITOR)
    public void onAnvilClick(InventoryClickEvent e) {
        // check if the event has been cancelled by another plugin
        if (!e.isCancelled()) {
            HumanEntity ent = e.getWhoClicked();

            // not really necessary
            if (ent instanceof Player) {
                Player player = (Player) ent;
                Inventory inv = e.getInventory();

                // see if the event is about an anvil
                if (inv instanceof AnvilInventory) {
                    InventoryView view = e.getView();
                    int rawSlot = e.getRawSlot();

                    // compare the raw slot with the inventory view to make sure
                    // we are talking about the upper inventory
                    if (rawSlot == view.convertSlot(rawSlot)) {
                        /*
                         * slot 0 = left item slot slot 1 = right item slot slot
						 * 2 = result item slot
						 * 
						 * see if the player clicked in the result item slot of
						 * the anvil inventory
						 */
                        if (rawSlot == 2) {
                            /*
                             * get the current item in the result slot I think
							 * inv.getItem(rawSlot) would be possible too
							 */
                            ItemStack item = e.getCurrentItem();
                            ItemStack baseItem = inv.getItem(0);

                            // check if there is an item in the result slot
                            if (item != null) {
                                ItemMeta meta = item.getItemMeta();

                                // it is possible that the item does not have
                                // meta data

                                if (meta != null) {
                                    // see whether the item is beeing renamed
                                    if (meta.hasDisplayName()) {

                                        String displayName = fixName(meta.getDisplayName());

                                        if (baseItem.hasItemMeta() && baseItem.getItemMeta().getDisplayName() != null && RavenPlugin.get().getServerManager().getUsedNames().contains(fixName(baseItem.getItemMeta().getDisplayName())) && !baseItem.getItemMeta().getDisplayName().equals(meta.getDisplayName())) {
                                            e.setCancelled(true);
                                            player.sendMessage(ChatColor.RED + "You cannot rename an item with a name!");
                                            return;
                                        }

                                        if (RavenPlugin.get().getServerManager().getUsedNames().contains(displayName) && (baseItem.hasItemMeta() && baseItem.getItemMeta().getDisplayName() != null ? !baseItem.getItemMeta().getDisplayName().equals(meta.getDisplayName()) : true)) {
                                            e.setCancelled(true);
                                            player.sendMessage(ChatColor.RED + "An item with that name already exists.");

                                        } else {

                                            List<String> lore = new ArrayList<String>();

                                            if (meta.getLore() != null) {
                                                lore = meta.getLore();
                                            }

                                            DateFormat sdf = DateFormat.getDateTimeInstance();
                                            lore.add(" ");
                                            lore.add("§eForged by " + player.getDisplayName() + "§e on " + sdf.format(new Date()));

                                            meta.setLore(lore);
                                            item.setItemMeta(meta);

                                            RavenPlugin.get().getServerManager().getUsedNames().add(displayName);
                                            RavenPlugin.get().getServerManager().save();
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private String fixName(String name) {
        String b = name.toLowerCase().trim();
        char[] allowed = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()-_'".toCharArray();
        char[] charArray = b.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : charArray) {
            for (char a : allowed) {
                if (c == a) {
                    result.append(a);
                }
            }
        }

        return result.toString();
    }

    /**
     * We do this so that when a player lands, they can be launched again
     *
     * @param e the entity damage event
     */
    @EventHandler
    public void onEntityDamageSponge(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            new SpongeLauncher(((Player) e.getEntity())).launch();
//            if (RavenPlugin.get().getScoreboardTask() != null) {
//                RavenPlugin.get().getScoreboardTask().updatePlayerLater(((Player) e.getEntity()));
//            }
        }
    }

    /**
     * Sponges and shit
     *
     * @param e event
     */
    @EventHandler
    public void onPlayerMoveSponge(PlayerMoveEvent e) {
        if ((RavenPlugin.spawnprot.contains(e.getPlayer().getName())))
            new SpongeLauncher(e.getPlayer()).launch();

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            if (e.getEntity().hasMetadata("Spawner")) {
                // Trello: Reduce XP multiplier by 75%
                // 3 * .75 = 2.25 (75% of 3)
                // 3 - 3 * . 75 = 0.75
                double xp = e.getDroppedExp() * 0.75;
                // Yes? No? Maybe so?
                e.setDroppedExp((int) Math.round(xp));
            }

        }

        if (e.getEntityType() == EntityType.WITHER) {
            e.getDrops().clear(); // clear the drops (The Nether Star)

            /*
             Trello: Decrease the chance of dropping a nether star by 75%
             essentially a 25/100 ish chance of dropping a nether star.
             I guess one could do RANDOM.nextInt(100) <= 25: drop a star.
             After some testing with this code (Ignore the C#):

            int count1 = 0;

            for (int i = 0; i < 100; i++)
            {
                if (random.Next(100) <= 25)
                {
                    count1++;
                    Console.WriteLine("Dropped Star");
                }
            }
            Console.WriteLine("After 100 runs the nether star dropped: " + count1);

             Run 1: 21
             Run 2: 25
             Run 3: 30
             Run 4: 23
             Run 5: 25

             Which averages out to ~24 Nether stars from 100 withers
             */
            if (RavenPlugin.RANDOM.nextInt(100) <= 25) {
                // drop the nether star
                e.getEntity().getLocation().getWorld().dropItem(e.getEntity().getLocation(),
                        new ItemStack(Material.NETHER_STAR));

            }

        }
    }

    private HashMap<String, TimeLock> timeLocks = new HashMap<String, TimeLock>();

    @EventHandler
    public void onEndPortal(final PlayerPortalEvent e) {
        if (e.getCause() == TeleportCause.END_PORTAL) {

            if (e.getTo().getWorld().getEnvironment() == Environment.NORMAL && e.getFrom().getWorld().getEnvironment() == Environment.THE_END) {

                if (timeLocks.containsKey(e.getPlayer().getName())) {
                    if (timeLocks.get(e.getPlayer().getName()).done()) {
                        RavenPlugin.get().getBossBarManager().unregisterPlayer(e.getPlayer());
                        Bukkit.getScheduler().runTaskLater(RavenPlugin.get(), new Runnable() {

                            @Override
                            public void run() {
                                e.getPlayer().teleport(RavenPlugin.get().getServerManager().getSpawn(e.getTo().getWorld()));

                                RavenPlugin.spawnprot.remove(e.getPlayer().getName());
                                RavenPlugin.spawnprot.add(e.getPlayer().getName());
                            }
                        }, 1L);
                    } else {
                        Bukkit.getScheduler().runTaskLater(RavenPlugin.get(), new Runnable() {

                            @Override
                            public void run() {
                                e.getPlayer().teleport(SpawnCommand.portalLoc);
                            }
                        }, 1L);
                        e.getPlayer().sendMessage(ChatColor.RED + "You may not exit The End yet!");
                    }
                }
                return;
            }
            if (e.getTo().getWorld().getEnvironment() == Environment.THE_END) {

                if (!RavenPlugin.spawnprot.contains(e.getPlayer().getName())) {

                    if (!(lastMsgSent.containsKey(e.getPlayer().getName()) && lastMsgSent.get(e.getPlayer().getName()) > System.currentTimeMillis())) {
                        e.getPlayer().sendMessage(ChatColor.RED + "You need spawn protection to enter this event!");
                        lastMsgSent.put(e.getPlayer().getName(), System.currentTimeMillis() + 2500);

                    }
                    e.setCancelled(true);
                    return;
                }
                if (!ToggleEnd.enabled) {
                    e.setCancelled(true);
                    if (!(lastMsgSent.containsKey(e.getPlayer().getName()) && lastMsgSent.get(e.getPlayer().getName()) > System.currentTimeMillis())) {
                        e.getPlayer().sendMessage(ChatColor.RED + "End portals are currently disabled.");
                        lastMsgSent.put(e.getPlayer().getName(), System.currentTimeMillis() + 2500);

                    }
                    return;
                } else {

                    // e.setTo(e.getTo().getWorld().getSpawnLocation());

                    Bukkit.getScheduler().runTaskLater(RavenPlugin.get(), new Runnable() {

                        @Override
                        public void run() {
                            e.getPlayer().teleport(e.getTo().getWorld().getSpawnLocation());

                            for (Entity player : e.getPlayer().getNearbyEntities(30, 30, 30)) {
                                if (player instanceof Player) {
                                    refresh(e.getPlayer(), (Player) player);
                                }
                            }
                        }
                    }, 2L);

                }
            }

            if (plugin.getServerManager().isSpawn(e.getTo())) {
                if (!RavenPlugin.spawnprot.contains(e.getPlayer().getName())) {
                    RavenPlugin.spawnprot.add(e.getPlayer().getName());
                }
            }

        }
    }

    public void refresh(Player fix, Player p) {
        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn(((CraftPlayer) p).getHandle());
        ((CraftPlayer) fix).getHandle().playerConnection.sendPacket(packet);

        for (int i = 0; i < 5; i++) {
            ItemStack item = null;
            if (i == 0) {
                item = p.getItemInHand();
            } else {
                int index = i - 1;
                item = p.getInventory().getArmorContents()[index];
            }
            PacketPlayOutEntityEquipment armor = new PacketPlayOutEntityEquipment(p.getEntityId(), i, CraftItemStack.asNMSCopy(item));
            ((CraftPlayer) fix).getHandle().playerConnection.sendPacket(armor);

        }
    }

    @EventHandler
    public void onPlayerTabCompleteCommand(PlayerTabCompleteCommandEvent e) {
        if (e.getCommand().equals("/"))
            e.setCancelled(true);
        if (e.getCommand().equals("/ "))
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
//        if (RavenPlugin.get().getScoreboardTask() != null) {
//            RavenPlugin.get().getScoreboardTask().updatePlayerLater((Player) e.getWhoClicked());
//        }
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != null) {
            if (e.getInventory().getType() == InventoryType.BREWING) {
                if (e.getClick().toString().toLowerCase().contains("shift") || e.getClick().isKeyboardClick()) {
                    if (e.getCurrentItem().getAmount() > 1) {
                        if (!(e.getCursor().getType() != Material.POTION)) {
                            ItemStack giveBack = new ItemStack(e.getCurrentItem()).clone();
                            giveBack.setAmount(e.getCurrentItem().getAmount() - 1);
                            e.getWhoClicked().getInventory().addItem(giveBack);
                            e.getCurrentItem().setAmount(1);

                        }
                    }
                }
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < 3; i += 1) {
                            if (((BrewerInventory) e.getInventory()).getItem(i) != null) {
                                if (((BrewerInventory) e.getInventory()).getItem(i).getAmount() > 1) {
                                    int amount = ((BrewerInventory) e.getInventory()).getItem(i).getAmount() - 1;
                                    ((BrewerInventory) e.getInventory()).getItem(i).setAmount(1);
                                    ItemStack gv = ((BrewerInventory) e.getInventory()).getItem(i).clone();
                                    gv.setAmount(amount);
                                    e.getWhoClicked().getInventory().addItem(gv);
                                }
                            }
                        }
                    }
                }, 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMoveAgain(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLocation().getY() > 128 && player.getWorld().getEnvironment() == Environment.NETHER) {
            if (((Damageable) player).getHealth() > 0D && player.getGameMode() != GameMode.CREATIVE) {
                player.setHealth(0D);
            }
        }
        Location playerLocation = player.getLocation();
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            if (playerLocation.getWorld().getEnvironment() == Environment.NORMAL)
                if ((Math.abs(playerLocation.getX()) < 512.0D) && (Math.abs(playerLocation.getZ()) < 512.0D)) {
                    boolean hasArmor = false;

                    for (ItemStack is : player.getInventory().getArmorContents()) {
                        if (is != null && is.getType() != Material.AIR && (is.getType().name().contains("IRON") || is.getType().name().contains("DIAMOND"))) {
                            hasArmor = true;
                        }
                    }
                    if (!hasArmor) {
                        player.removePotionEffect(PotionEffectType.INVISIBILITY);
                        player.sendMessage(ChatColor.RED + "You may not be invisible within 512 blocks of spawn without iron or diamond armor.");
                    }
                }
        }

        if (plugin.getServerManager().getWarpmove().contains(event.getPlayer().getName())) {
            if ((event.getFrom().getBlockX() == event.getTo().getBlockX()) && (event.getFrom().getBlockZ() == event.getTo().getBlockZ()) && (event.getFrom().getBlockY() == event.getTo().getBlockY())) {
                return;
            }
            plugin.getServerManager().getWarpmove().remove(event.getPlayer().getName());
            event.getPlayer().sendMessage(ChatColor.GRAY + "Warp cancelled.");
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        net.frozenorb.Raven.Team.Team team = plugin.getTeamManager().getPlayerTeam(p.getUniqueId());

        if (team == null) {
            return;
        }

        Set<UUID> members = team.getMembers();

        if (plugin.getServerManager().getTeamChatMap().get(p.getName()) == "tchat") {
            e.setCancelled(true);
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (members.contains(pl.getUniqueId())) {
                    if (team.isOwner(p.getUniqueId()))
                        pl.sendMessage(ChatColor.WHITE + "<" + ChatColor.DARK_AQUA + p.getName() + ChatColor.WHITE + ">" + ChatColor.GRAY + "[" + ChatColor.GRAY + team.getFriendlyName() + "]" + ChatColor.WHITE + " " + e.getMessage());
                    else
                        pl.sendMessage(ChatColor.WHITE + "<" + ChatColor.GRAY + p.getName() + ChatColor.WHITE + ">" + ChatColor.GRAY + "[" + ChatColor.GRAY + team.getFriendlyName() + "]" + ChatColor.WHITE + " " + e.getMessage());
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.BEDROCK && !Shared.get().getProfileManager().getProfile(e.getPlayer()).getPermissions().contains("raven.build.bedrock")) {
            e.setBuild(false);
            e.setCancelled(true);
        }
        if (e.getPlayer().hasPermission("op")) {
            e.getBlock().setMetadata("flow", new FixedMetadataValue(plugin, true));
        }

    }

    @EventHandler
    public void onBlockFrom(BlockFormEvent e) {
        Location loc = e.getBlock().getLocation();
        if (plugin.getServerManager().isOuterSpawn(loc) && plugin.getServerManager().shouldPreventFlow(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        Location loc = e.getToBlock().getLocation();
        if (plugin.getServerManager().isOuterSpawn(loc) && plugin.getServerManager().shouldPreventFlow(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFrom(BlockSpreadEvent e) {
        Location loc = e.getSource().getLocation();
        if (plugin.getServerManager().isOuterSpawn(loc) && plugin.getServerManager().shouldPreventFlow(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onblockP(BlockPlaceEvent e) {
        if (plugin.getServerManager().isOuterSpawn(e.getBlock().getLocation())) {
            if (e.getBlock().getLocation().getY() > 70 && e.getBlock().getType() == Material.WATER) {
                e.setCancelled(true);
                e.setBuild(false);
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot place water here.");
            }
        }
        if (plugin.getServerManager().isOuterSpawn(e.getBlock().getLocation())) {
            if (e.getBlock().getLocation().getY() > 70 && e.getBlock().getType() == Material.LAVA) {
                e.setCancelled(true);
                e.setBuild(false);
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot place lava here.");
            }
        }
    }

    @EventHandler
    public void playerhit(EntityDamageByEntityEvent e) {
        if ((e.getEntity() instanceof Player && e.getDamager() instanceof Player)) {

            if (e.isCancelled())
                return;

            Player p = (Player) e.getEntity();
            Player pl = (Player) e.getDamager();

            if (plugin.getTeamManager().getPlayerTeam(p.getUniqueId()) == null)
                return;

            if (plugin.getTeamManager().getPlayerTeam(pl.getUniqueId()) == null)
                return;

            net.frozenorb.Raven.Team.Team team = plugin.getTeamManager().getPlayerTeam(p.getUniqueId());

            if (!team.isFriendlyFire()) {
                if (plugin.getTeamManager().getPlayerTeam(pl.getUniqueId()) == team)
                    e.setCancelled(true);
            } else
                e.setCancelled(false);
        }
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Arrow) {

            if (e.isCancelled())
                return;

            Player p = (Player) e.getEntity();
            if (!(((Arrow) e.getDamager()).getShooter() instanceof Player)) {
                return;
            }
            Player pl = ((Player) ((Arrow) e.getDamager()).getShooter());

            if (plugin.getTeamManager().getPlayerTeam(p.getUniqueId()) == null)
                return;

            if (plugin.getTeamManager().getPlayerTeam(pl.getUniqueId()) == null)
                return;

            net.frozenorb.Raven.Team.Team team = plugin.getTeamManager().getPlayerTeam(p.getUniqueId());

            if (!team.isFriendlyFire()) {
                if (plugin.getTeamManager().getPlayerTeam(pl.getUniqueId()) == team)
                    e.setCancelled(true);
            } else
                e.setCancelled(false);

        }
    }

    // @EventHandler(priority = EventPriority.LOW)
    public void onRighClickTrack(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.DIAMOND_BLOCK) {
                if (e.getClickedBlock().getLocation().equals(e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation())) {
                    User u = Common.get().getUserManager().getUser(e.getPlayer());
                    String name = u.getServerData().getString("right-click-track", "all");
                    Tracker tracker = TrackerFactory.createTracker(e.getClickedBlock().getLocation());
                    if (name.equalsIgnoreCase("all")) {

                        tracker.trackAll(e.getPlayer());
                    } else {
                        if (Bukkit.getPlayerExact(name) == null) {
                            e.getPlayer().sendMessage(String.format("§6Player§f '%s' §6not found!", name));
                            u.getServerData().append("right-click-track", "all");
                            return;
                        }
                        tracker.track(e.getPlayer(), Bukkit.getPlayerExact(name));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractSalvage(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getPlayer().getItemInHand() != null) {
                ItemStack i = e.getPlayer().getItemInHand();
                boolean recipeNotFound = Bukkit.getRecipesFor(new ItemStack(i.getType())).size() == 0;
                if (i.getType().getMaxDurability() != 0 || i.getType() == Material.IRON_BARDING || i.getType() == Material.GOLD_BARDING || i.getType() == Material.DIAMOND_BARDING) {
                    double durRatio = (double) ((double) i.getType().getMaxDurability() - i.getDurability()) / (double) i.getType().getMaxDurability();
                    if (i.getType() == Material.IRON_BARDING || i.getType() == Material.GOLD_BARDING || i.getType() == Material.DIAMOND_BARDING) {
                        durRatio = 1.0D;
                    }
                    if ((e.getClickedBlock().getType() == Material.IRON_BLOCK && i.getType().toString().contains("IRON")) || (e.getClickedBlock().getType() == Material.DIAMOND_BLOCK && i.getType().toString().contains("DIAMOND")) || (e.getClickedBlock().getType() == Material.GOLD_BLOCK && i.getType().toString().contains("GOLD"))) {
                        if (isTouching(e.getClickedBlock(), Material.FURNACE)) {
                            e.setCancelled(true);
                            e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                            e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                            if (recipeNotFound) {
                                Material type = Material.DIAMOND;
                                if (i.getType() == Material.IRON_BARDING) {
                                    type = Material.IRON_INGOT;
                                } else if (i.getType() == Material.GOLD_BARDING) {
                                    type = Material.GOLD_INGOT;
                                }
                                for (int co = 0; co < 4; co++) {
                                    e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), new ItemStack(type));
                                    i.setType(Material.AIR);
                                    e.getPlayer().setItemInHand(null);
                                }

                                return;
                            }
                            Recipe rsc = Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                            if (durRatio == 1.0) {
                                if (isTouching(e.getClickedBlock(), Material.ENCHANTMENT_TABLE)) {

                                    ExperienceOrb eo = (ExperienceOrb) e.getPlayer().getWorld().spawnEntity(e.getClickedBlock().getLocation(), EntityType.EXPERIENCE_ORB);
                                    eo.setExperience(getLevel(i) * 15);
                                }
                                if (rsc instanceof ShapedRecipe) {
                                    Recipe rc = (ShapedRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapedRecipe) rc).getIngredientMap().values()) {
                                        if (is != null) {

                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                        }
                                    }
                                }
                                if (rsc instanceof ShapelessRecipe) {
                                    Recipe rc = (ShapelessRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapelessRecipe) rc).getIngredientList()) {
                                        if (is != null) {

                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                        }
                                    }
                                }

                            } else if (durRatio > .75) {
                                int counter = 0;
                                if (isTouching(e.getClickedBlock(), Material.ENCHANTMENT_TABLE)) {
                                    ExperienceOrb eo = (ExperienceOrb) e.getPlayer().getWorld().spawnEntity(e.getClickedBlock().getLocation(), EntityType.EXPERIENCE_ORB);
                                    eo.setExperience(getLevel(i) * 14);
                                }

                                if (rsc instanceof ShapedRecipe) {
                                    Recipe rc = (ShapedRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapedRecipe) rc).getIngredientMap().values()) {
                                        if (counter > ((ShapedRecipe) rc).getIngredientMap().size() * .75)
                                            return;

                                        if (is != null) {
                                            counter++;
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                        }
                                    }
                                }
                                if (rsc instanceof ShapelessRecipe) {
                                    Recipe rc = (ShapelessRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapelessRecipe) rc).getIngredientList()) {
                                        if (counter > ((ShapelessRecipe) rc).getIngredientList().size() * .75)
                                            return;

                                        if (is != null) {
                                            counter++;
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                        }
                                    }
                                }

                            } else if (durRatio > .50) {
                                if (isTouching(e.getClickedBlock(), Material.ENCHANTMENT_TABLE)) {

                                    ExperienceOrb eo = (ExperienceOrb) e.getPlayer().getWorld().spawnEntity(e.getClickedBlock().getLocation(), EntityType.EXPERIENCE_ORB);
                                    eo.setExperience(getLevel(i) * 13);
                                }
                                int counter = 0;

                                if (rsc instanceof ShapedRecipe) {
                                    Recipe rc = (ShapedRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapedRecipe) rc).getIngredientMap().values()) {
                                        if (counter > ((ShapedRecipe) rc).getIngredientMap().size() * .50)
                                            return;
                                        if (is != null) {
                                            counter++;
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                        }
                                    }
                                }
                                if (rsc instanceof ShapelessRecipe) {
                                    Recipe rc = (ShapelessRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapelessRecipe) rc).getIngredientList()) {
                                        if (counter > ((ShapelessRecipe) rc).getIngredientList().size() * .50)
                                            return;

                                        if (is != null) {
                                            counter++;
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                        }
                                    }
                                }

                            } else if (durRatio > .25) {
                                if (isTouching(e.getClickedBlock(), Material.ENCHANTMENT_TABLE)) {

                                    ExperienceOrb eo = (ExperienceOrb) e.getPlayer().getWorld().spawnEntity(e.getClickedBlock().getLocation(), EntityType.EXPERIENCE_ORB);
                                    eo.setExperience(getLevel(i) * 12);

                                }
                                int counter = 0;

                                if (rsc instanceof ShapedRecipe) {
                                    Recipe rc = (ShapedRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapedRecipe) rc).getIngredientMap().values()) {
                                        if (counter > 1)
                                            return;

                                        if (is != null) {
                                            counter++;
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                        }
                                    }
                                }
                                if (rsc instanceof ShapelessRecipe) {
                                    Recipe rc = (ShapelessRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapelessRecipe) rc).getIngredientList()) {
                                        if (counter > 1)
                                            return;

                                        if (is != null) {
                                            counter++;
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                        }
                                    }
                                }

                            } else if (durRatio > .10) {
                                if (isTouching(e.getClickedBlock(), Material.ENCHANTMENT_TABLE)) {

                                    ExperienceOrb eo = (ExperienceOrb) e.getPlayer().getWorld().spawnEntity(e.getClickedBlock().getLocation(), EntityType.EXPERIENCE_ORB);
                                    eo.setExperience(getLevel(i) * 11);

                                }
                                int counter = 0;

                                if (rsc instanceof ShapedRecipe) {
                                    Recipe rc = (ShapedRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapedRecipe) rc).getIngredientMap().values()) {
                                        if (counter > 0)
                                            return;
                                        if (is != null) {
                                            counter++;
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                            e.getPlayer().setItemInHand(null);
                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                        }
                                    }
                                }
                                if (rsc instanceof ShapelessRecipe) {
                                    Recipe rc = (ShapelessRecipe) Bukkit.getRecipesFor(new ItemStack(i.getType())).get(0);
                                    for (ItemStack is : ((ShapelessRecipe) rc).getIngredientList()) {
                                        if (counter > 0)
                                            return;
                                        if (is != null) {
                                            counter++;
                                            i.setType(Material.AIR);
                                            e.getPlayer().setItemInHand(null);
                                            e.getClickedBlock().getWorld().dropItem(e.getClickedBlock().getLocation().add(new Vector(0, 1, 0)), is);
                                        }
                                    }
                                }

                            } else {
                                e.getPlayer().sendMessage(ChatColor.RED + "This item is too broken to repair.");
                            }

                        }
                    }
                }
            }

        }

    }


    @EventHandler
    public void onPlayerblock(BlockBreakEvent e) {
        if (Potato.potatoSet.contains(e.getPlayer().getName().toLowerCase())) {
            if (e.getBlock().getType().name().contains("ORE")) {
                e.setCancelled(true);
                e.getBlock().setType(Material.AIR);

                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(Material.POTATO_ITEM));
                e.getBlock().getWorld().playSound(e.getBlock().getLocation(), Sound.VILLAGER_IDLE, 29, 1F);
                return;
            }
        }

        if (e.getBlock().getType() == Material.ENDER_PORTAL_FRAME || e.getBlock().getType() == Material.ENDER_PORTAL) {
            e.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to break this.");
            e.setCancelled(true);
        }
        if (!e.getPlayer().hasPermission("raven.spawn.build") && RavenPlugin.spawnprot.contains(e.getPlayer().getName()) && plugin.getServerManager().isSpawn(e.getPlayer().getLocation())) {
            e.setCancelled(true);
        }

        if (e.getBlock().getType() == Material.DIAMOND_ORE &&
                e.getPlayer().getItemInHand() != null && !e.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            // 3000
            if (RavenPlugin.RANDOM.nextInt(3000) == 0) {
                ItemStack ascendedPick = new AscendedItem(e.getPlayer().getDisplayName(), new ItemStack(Material.DIAMOND_PICKAXE)).getParent();
                e.getPlayer().getWorld().dropItem(e.getBlock().getLocation(), ascendedPick).setVelocity(new Vector(0, 0, 0));

                FancyMessage msg = generateItemMessage(ascendedPick, e.getPlayer(), true, null);
                for (Player pp : Bukkit.getOnlinePlayers()) {
                    msg.send(pp);
                }
            } else if (RavenPlugin.RANDOM.nextInt(1000) == 0) {
                ItemStack wrench = new Wrench().get();
                e.getPlayer().getWorld().dropItem(e.getBlock().getLocation(), wrench).setVelocity(new Vector(0, 0, 0));

                FancyMessage msg = generateItemMessage(wrench, e.getPlayer(), true, null);
                for (Player pp : Bukkit.getOnlinePlayers()) {
                    msg.send(pp);
                }
            }

        }
        if (e.getBlock().getType() == Material.EMERALD_ORE &&
                e.getPlayer().getItemInHand() != null && !e.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
            // 1500
            if (RavenPlugin.RANDOM.nextInt(1500) == 0) {
                ItemStack ascendedPick = new AscendedItem(e.getPlayer().getDisplayName(), new ItemStack(Material.DIAMOND_PICKAXE)).getParent();
                e.getPlayer().getWorld().dropItem(e.getBlock().getLocation(), ascendedPick).setVelocity(new Vector(0, 0, 0));
                FancyMessage msg = generateItemMessage(ascendedPick, e.getPlayer(), true, null);
                for (Player pp : Bukkit.getOnlinePlayers()) {
                    msg.send(pp);
                }
            } else if (RavenPlugin.RANDOM.nextInt(500) == 0) {
                ItemStack wrench = new Wrench().get();
                e.getPlayer().getWorld().dropItem(e.getBlock().getLocation(), wrench).setVelocity(new Vector(0, 0, 0));

                FancyMessage msg = generateItemMessage(wrench, e.getPlayer(), true, null);
                for (Player pp : Bukkit.getOnlinePlayers()) {
                    msg.send(pp);
                }
            }
        }

        // 10_000
        if (e.getBlock().getType() == Material.OBSIDIAN) { //
            //  10_000
            if (RavenPlugin.RANDOM.nextInt(10_000) == 0) {
                ItemStack ascendedPick = new AscendedItem(e.getPlayer().getDisplayName(), new ItemStack(Material.DIAMOND_PICKAXE)).getParent();
                e.getPlayer().getWorld().dropItem(e.getBlock().getLocation(), ascendedPick).setVelocity(new Vector(0, 0, 0));
                FancyMessage msg = generateItemMessage(ascendedPick, e.getPlayer(), true, null);
                for (Player pp : Bukkit.getOnlinePlayers()) {
                    msg.send(pp);
                }
            } else if (RavenPlugin.RANDOM.nextInt(7500) == 0) {
                ItemStack wrench = new Wrench().get();
                e.getPlayer().getWorld().dropItem(e.getBlock().getLocation(), wrench).setVelocity(new Vector(0, 0, 0));

                FancyMessage msg = generateItemMessage(wrench, e.getPlayer(), true, null);
                for (Player pp : Bukkit.getOnlinePlayers()) {
                    msg.send(pp);
                }
            }
        }

        // 10_000
        if ((e.getBlock().getType() == Material.LOG_2 || e.getBlock().getType() == Material.LOG) && RavenPlugin.RANDOM.nextInt(10_000) == 0) {
            ItemStack wrench = new Wrench().get();
            e.getPlayer().getWorld().dropItem(e.getBlock().getLocation(), wrench).setVelocity(new Vector(0, 0, 0));

            FancyMessage msg = generateItemMessage(wrench, e.getPlayer(), true, null);
            for (Player pp : Bukkit.getOnlinePlayers()) {
                msg.send(pp);
            }
        }


        RavenPlugin.get().getServerManager().handleRaid(e.getPlayer());
    }

    @EventHandler
    public void onPlayerblock(BlockPlaceEvent e) {
        RavenPlugin.get().getServerManager().handleRaid(e.getPlayer());
    }

    @EventHandler
    public void onPlayerOpenChest(final PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && Arrays.asList(TRAP_BLOCKS).contains(e.getClickedBlock().getType())) {
            Bukkit.getScheduler().runTaskAsynchronously(RavenPlugin.get(), new Runnable() {
                @Override
                public void run() {
                    for (final Trap a : RavenPlugin.get().getTrapManager().getTraps()) {
                        if (a.canTrigger(e.getPlayer().getName()) && a.isRaiding(e.getPlayer())) {
                            String msg = "§4§l[§a§l!§4§l] §c§l" + e.getPlayer().getDisplayName() + "§c§l triggered xray trap '" + a.getName() + "'!";

                            JSONChatMessage jcm = new JSONChatMessage("", JSONChatColor.GREEN);

                            jcm.addExtra(new JSONChatExtra(msg, JSONChatColor.GREEN) {
                                {
                                    setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/trap " + a.getName());
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to warp to the trap!");
                                }
                            });

                            a.trigger(e.getPlayer().getName());

                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.hasPermission("common.alerts")) {
                                    User u = Common.get().getUserManager().getUser(p);

                                    if (u.isAlertModeOn()) {
                                        jcm.sendToPlayer(p);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.CHEST)
                RavenPlugin.get().getServerManager().handleRaid(e.getPlayer());
        }
        if (e.getItem() != null) {
            ItemStack i = e.getItem();

            if (i.getType() == Material.POTION || i.getType() == Material.SNOW_BALL || i.getType() == Material.EGG || i.getType() == Material.FLINT_AND_STEEL) {
                if (isAttackBanned(e.getPlayer())) {
                    e.setCancelled(true);
                    e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                    e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                    e.getPlayer().sendMessage(ChatColor.RED + "You are currently on spawn cooldown!");
                    e.getPlayer().updateInventory();
                    return;
                }
            }

            if (i.getAmount() > 1 && i.getType() == Material.POTION && Arrays.asList(DAMAGE_POTION_IDS).contains((Integer.valueOf((int) i.getDurability())))) {
                e.setCancelled(true);

                e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                e.getPlayer().updateInventory();
            }
        }
    }

    private HashMap<String, Long> lastItemMsg = new HashMap<String, Long>();

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {

        if (isAttackBanned(e.getPlayer())) {
            e.setCancelled(true);
            if (lastItemMsg.containsKey(e.getPlayer().getName()) && lastItemMsg.get(e.getPlayer().getName()) > System.currentTimeMillis()) {
                return;
            }
            lastItemMsg.put(e.getPlayer().getName(), System.currentTimeMillis() + 5000);
            e.getPlayer().sendMessage(ChatColor.RED + "You cannot pickup items while on spawn cooldown!");
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(EntityDamageByEntityEvent e) {
        if (e.getCause() == DamageCause.PROJECTILE) {
            if (e.getDamager().getType() == EntityType.EGG) {
                Player p = (Player) ((Egg) e.getDamager()).getShooter();
                if (!p.isOnline())
                    return;
                e.setCancelled(true);
                if (!e.getEntityType().isAlive()) {
                    return;
                }
                net.minecraft.server.v1_7_R4.Entity ent = ((CraftEntity) e.getEntity()).getHandle();
                if (ent instanceof EntityLiving) {
                    if (((EntityLiving) ent).isBaby()) {
                        if (e.getEntityType() != EntityType.SLIME) {
                            p.sendMessage(ChatColor.RED + "You cannot capture babies.");
                            return;
                        }
                    }
                }
                if (e.getEntityType() == EntityType.WITHER || e.getEntityType() == EntityType.ENDER_DRAGON || e.getEntityType() == EntityType.GHAST || e.getEntityType() == EntityType.PLAYER) {
                    return;
                }
                int level = p.getLevel();
                if (p.getLocation().distance(e.getEntity().getLocation()) < 5) {
                    p.sendMessage(ChatColor.RED + "You must be 5 blocks away to capture.");
                    return;
                }
                if (!(level > 24)) {
                    p.sendMessage(ChatColor.RED + "You must be at least level 25 to capture.");
                    return;
                } else {
                    if (level > 34) {
                        if (e.getEntityType() == EntityType.MUSHROOM_COW)
                            p.setLevel(0);

                        SpawnEgg egg = new SpawnEgg(e.getEntity().getType());
                        org.bukkit.inventory.ItemStack items = new org.bukkit.inventory.ItemStack(egg.getItemType(), 1, egg.getData());
                        Item i = e.getEntity().getLocation().getWorld().dropItem(e.getEntity().getLocation(), items);
                        i.setPickupDelay(0);
                        e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.LAVA_POP, 20F, 20F);
                        e.getEntity().remove();
                        e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 100);
                        return;
                    }
                    if (level > 29) {

                        if (new Random().nextInt(100) > 33) {
                            if (e.getEntityType() == EntityType.MUSHROOM_COW)
                                p.setLevel(0);

                            SpawnEgg egg = new SpawnEgg(e.getEntity().getType());
                            org.bukkit.inventory.ItemStack items = new org.bukkit.inventory.ItemStack(egg.getItemType(), 1, egg.getData());
                            Item i = e.getEntity().getLocation().getWorld().dropItem(e.getEntity().getLocation(), items);
                            i.setPickupDelay(0);
                            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.LAVA_POP, 20F, 20F);
                            e.getEntity().remove();
                            e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 100);
                            return;

                        } else {
                            if (e.getEntityType() == EntityType.MUSHROOM_COW)
                                p.setLevel(p.getLevel() / 2);

                            p.sendMessage(ChatColor.RED + "Capture failed. Gather more levels and try again!");
                        }
                        return;
                    }
                    if (level > 24) {
                        if (new Random().nextInt(100) > 66) {
                            if (e.getEntityType() == EntityType.MUSHROOM_COW)
                                p.setLevel(0);

                            SpawnEgg egg = new SpawnEgg(e.getEntity().getType());
                            org.bukkit.inventory.ItemStack items = new org.bukkit.inventory.ItemStack(egg.getItemType(), 1, egg.getData());
                            Item i = e.getEntity().getLocation().getWorld().dropItem(e.getEntity().getLocation(), items);
                            i.setPickupDelay(0);
                            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.LAVA_POP, 20F, 20F);
                            e.getEntity().remove();
                            e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 100);
                            return;

                        } else {
                            if (e.getEntityType() == EntityType.MUSHROOM_COW)
                                p.setLevel(p.getLevel() / 2);

                            p.sendMessage(ChatColor.RED + "Capture failed. Gather more levels and try again!");
                        }
                        return;
                    }
                }
            }
        }

    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof MushroomCow) {
            if (e.getPlayer().getItemInHand().getType() == Material.MONSTER_EGG) {
                if (e.getPlayer().getItemInHand().getDurability() == 92) {
                    if (e.getPlayer().getItemInHand().getAmount() == 1) {
                        e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                    } else {
                        e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
                    }
                    Cow mcow = (Cow) e.getRightClicked().getWorld().spawnEntity(e.getRightClicked().getLocation(), EntityType.COW);
                    mcow.setBaby();
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent e) {
        if (e.getRecipe().getResult().getType() == (RavenPlugin.get().getRecipeManager().getBottledXPRecipe().getResult().getType())) {
            Player p = (Player) e.getWhoClicked();
            ExperienceManager experienceManager = new ExperienceManager(p);
            int xp = experienceManager.getCurrentExp();
            experienceManager.setExp(0D);
            if (e.getRecipe().getResult().getType() == Material.EXP_BOTTLE) {
                ItemStack bottle = e.getInventory().getResult();
                if (bottle != null && bottle.hasItemMeta() && bottle.getItemMeta().getLore() != null) {

                    List<String> lore = bottle.getItemMeta().getLore();
                    lore.set(1, ChatColor.BLUE + "XP: " + ChatColor.WHITE + NumberFormat.getInstance().format(xp));

                    if (xp == 0) {
                        e.getInventory().setResult(new ItemStack(Material.AIR));
                        e.setCancelled(true);
                        return;
                    }

                    ItemStack res = e.getInventory().getResult();

                    ItemMeta m = res.getItemMeta();
                    m.setLore(lore);
                    res.setItemMeta(m);

                    e.getInventory().getResult().setItemMeta(m);
                    e.getInventory().setResult(res);

                }
            }
        }
    }

    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent e) {
        if (e.getRecipe().getResult().getType() == (RavenPlugin.get().getRecipeManager().getBottledXPRecipe().getResult().getType())) {
            Player p = (Player) e.getInventory().getHolder();
            ItemStack m = e.getRecipe().getResult();
            ItemMeta meta = m.getItemMeta();
            ArrayList<String> lotr = new ArrayList<String>();
            final ExperienceManager experienceManager = new ExperienceManager(p);
            int doubl = experienceManager.getCurrentExp();
            if (doubl == 0) {
                e.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
            lotr.add(ChatColor.BLUE + "Right click to get all of the bottled xp.");
            lotr.add(ChatColor.BLUE + "XP: " + ChatColor.WHITE + NumberFormat.getInstance().format(doubl));
            meta.setLore(lotr);
            m.setItemMeta(meta);
            e.getInventory().setResult(m);
        }
    }

    @EventHandler
    public void onPlayerConsumeEnchantingBottle(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (p.getItemInHand() != null) {
            if (p.getItemInHand().getType() == Material.EXP_BOTTLE && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                ItemStack bottle = p.getItemInHand();
                if (e.getClickedBlock() != null) {
                    if (Arrays.asList(INTERACTABLE_WITH_XP_BOTTLE).contains(e.getClickedBlock().getType())) {
                        return;
                    }
                }
                if (bottle.hasItemMeta() && bottle.getItemMeta().getLore() != null) {
                    String data = bottle.getItemMeta().getLore().get(1);
                    e.setCancelled(true);
                    e.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                    e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
                    double val = Double.parseDouble(ChatColor.stripColor(data).split(":")[1].replace(",", "").trim());
                    if (p.getItemInHand().getAmount() == 1) {
                        p.setItemInHand(new ItemStack(Material.GLASS_BOTTLE));
                    } else {
                        p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);

                        ItemStack glassBottle = new ItemStack(Material.GLASS_BOTTLE);

                        if (!addItem(p.getInventory(), glassBottle)) {
                            p.getWorld().dropItemNaturally(p.getLocation(), glassBottle);
                        }
                        p.updateInventory();

                    }
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 20F, 0.1F);
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 20F, 20F);
                    ExperienceManager experienceManager = new ExperienceManager(p);
                    experienceManager.setExp(experienceManager.getCurrentExp() + (float) val);
                }
            }
        }
    }

    @EventHandler
    public void ontc(PlayerInteractEvent e) {
        if (e.getPlayer().getItemInHand() != null && (e.getPlayer().getItemInHand().getType() == Material.LAVA_BUCKET || e.getPlayer().getItemInHand().getType() == Material.WATER_BUCKET) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Location loc = e.getClickedBlock().getLocation();
            if (!(loc.getWorld().getEnvironment() == Environment.NETHER))
                if (Math.abs(loc.getX()) < 70 && Math.abs(loc.getZ()) < 70) {
                    e.getPlayer().sendMessage(ChatColor.RED + "You cannot use lava or water buckets within 70 blocks of spawn.");
                    e.setCancelled(true);
                }
        }
        if (e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType() == Material.LAVA_BUCKET && e.getAction().toString().toLowerCase().contains("right")) {
            Location loc = ((Player) e.getPlayer()).getLocation();
            if (!(loc.getWorld().getEnvironment() == Environment.NETHER))

                if (Math.abs(loc.getX()) < 150 && Math.abs(loc.getZ()) < 150) {
                    e.getPlayer().sendMessage(ChatColor.RED + "You cannot use lava buckets within 150 blocks of spawn.");
                    e.setCancelled(true);
                }
        }
    }

    @EventHandler
    public void onPlayerREspawn(PlayerRespawnEvent e) {
        RavenPlugin.spawnprot.remove(e.getPlayer().getName());
        RavenPlugin.spawnprot.add(e.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().toLowerCase().startsWith("/op ") || e.getMessage().toLowerCase().startsWith("/rl")) {
            e.setCancelled(true);
        }

    }

    private boolean isAttackBanned(Player p) {
        if (p.hasMetadata("attack-ban")) {
            if (p.getMetadata("attack-ban").get(0).asLong() > System.currentTimeMillis()) {
                return true;
            }
            p.removeMetadata("attack-ban", RavenPlugin.get());
        }
        return false;
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        String msg = e.getDeathMessage();

        boolean endDeathban = e.getEntity().getWorld().getEnvironment() == Environment.THE_END;

        if (RegionManager.get().hasTag(e.getEntity().getLocation(), "attack-ban")) {
            if (RavenPlugin.get().getServerManager().shouldDeathban(e.getEntity())) {

                for (ItemStack i : e.getEntity().getInventory()) {
                    if (i != null && i.getType() != Material.AIR) {

                        if ((i.getType() == Material.POTION && Arrays.asList(DEBUFF_POTION_VALUES).contains(Integer.valueOf(i.getDurability()))) || i.getType() == Material.WOOD_SWORD || i.getType() == Material.STONE_SWORD) {
                            e.getEntity().setMetadata("attack-ban", new FixedMetadataValue(RavenPlugin.get(), System.currentTimeMillis() + 60000));

                            e.getEntity().sendMessage("§e§lYou have received a Spawn Cooldown!");
                            RavenPlugin.get().getBossBarManager().registerMessage(e.getEntity(), new SpawnCooldown(), 200);
                            return;
                        }
                    }
                }
            }
        }

        if (endDeathban) {
            msg = "§c[End Deathban]§f " + msg + " §c(10:00)";

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.AMBIENCE_THUNDER, 1, 0.8F);
            }
        }

        e.setDeathMessage(null);
        if (combatLogRunnables.containsKey(e.getEntity().getName()))
            combatLogRunnables.get(e.getEntity().getName()).cancel();
        combatLogRunnables.remove(e.getEntity().getName());



        final Player p = e.getEntity();
        final String pName = p.getName();
        if (p.hasPermission("raven.nodeathkick")) {
            if (!endDeathban || (p.isOp() || p.hasPermission("raven.bypassdeathkick"))) {
                return;
            }
        }

        for (Player op : Bukkit.getOnlinePlayers()) {
            if (!Basic.get().getMessageManager().toggledPlayers.contains(op.getName())) {
                op.sendMessage(msg);
            }
        }

        if (!endDeathban) {
            PlayerInventory pi = e.getEntity().getInventory();

            int sofar = 0;
            for (int i = 0; i < 4; i++) {
                if (pi.getArmorContents()[i] != null) {
                    String name = pi.getArmorContents()[i].getType().name();

                    if (name.contains("IRON") || name.contains("DIAMOND")) {
                        sofar++;
                    }
                }
            }
            if (sofar == 4) {
                return;
            }
        }

        final int seconds = endDeathban ? 600 : 60;

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                plugin.getServerManager().getDeathbans().put(pName, System.currentTimeMillis());
                p.kickPlayer(ChatColor.RED + "You were just killed. Come back in " + seconds + " seconds!");
                final Listener l = new Listener() {
                    @EventHandler
                    public void onJoin(PlayerLoginEvent e) {
                        if (plugin.getServerManager().getDeathbans().containsKey(e.getPlayer().getName())) {
                            long diff = System.currentTimeMillis() - plugin.getServerManager().getDeathbans().get(e.getPlayer().getName());
                            int diffS = (int) diff / 1000;
                            e.setResult(Result.KICK_OTHER);
                            e.disallow(Result.KICK_OTHER, "§cYou were just killed. Come back in " + (seconds - diffS) + " seconds!");
                            e.setKickMessage("§cYou were just killed. Come back in " + (seconds - diffS) + " seconds!");
                        }
                    }
                };
                Bukkit.getPluginManager().registerEvents(l, plugin);
                Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                    public void run() {
                        plugin.getServerManager().getDeathbans().remove(pName);
                        HandlerList.unregisterAll(l);
                    }
                }, seconds * 20);
            }
        }, 1L);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (RavenPlugin.tasks.containsKey(p.getName())) {
                Bukkit.getScheduler().cancelTask(RavenPlugin.tasks.get(p.getName()));
                RavenPlugin.tasks.remove(p.getName());
                p.sendMessage(ChatColor.GRAY + "Warp cancelled!");
            }
        }
        if (e instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                Player p = ((Player) ((EntityDamageByEntityEvent) e).getDamager());
                if (RavenPlugin.tasks.containsKey(p.getName())) {
                    Bukkit.getScheduler().cancelTask(RavenPlugin.tasks.get(p.getName()));
                    RavenPlugin.tasks.remove(p.getName());
                    p.sendMessage(ChatColor.GRAY + "Warp cancelled!");
                }
            }
        }
    }


    @EventHandler
    public void onPlayerMoveSpawn(final PlayerMoveEvent e) {
        Location from = e.getFrom();

        Location to = e.getTo();
        double toX = to.getX();
        double toZ = to.getZ();
        double toY = to.getY();
        double fromX = from.getX();
        double fromZ = from.getZ();
        double fromY = from.getY();
        if (RavenPlugin.spawnprot.contains(e.getPlayer().getName())) {
            if (!(plugin.getServerManager().isSpawn(to)) && plugin.getServerManager().isSpawn(from)) {
                e.getPlayer().sendMessage(ChatColor.GRAY + "You no longer have spawn protection.");
                RavenPlugin.spawnprot.remove(e.getPlayer().getName());

                if (e.getFrom().getWorld().getEnvironment() == Environment.THE_END) {
                    TimeLock tl = new TimeLock();
                    tl.setWhen(System.currentTimeMillis() + (600 * 1000));
                    RavenPlugin.get().getBossBarManager().registerMessage(e.getPlayer(), tl, 200);
                    timeLocks.put(e.getPlayer().getName(), tl);
                }
            }

        }

        if (RegionManager.get().hasTag(to, "portal") && !RegionManager.get().hasTag(from, "portal")) {
            if (timeLocks.containsKey(e.getPlayer().getName()) && !timeLocks.get(e.getPlayer().getName()).done()) {

                e.setTo(e.getFrom());
//                Vector delta = e.getTo().toVector().subtract(e.getPlayer().getLocation().toVector());
//                e.getPlayer().setVelocity(delta.multiply(2));
//
                e.getPlayer().sendMessage(ChatColor.RED + "You may not exit The End yet!");
            }
        }
        if (RavenPlugin.tasks.containsKey(e.getPlayer().getName())) {
            if (from.distance(to) > 0.1 && (fromX != toX || fromZ != toZ || fromY != toY)) {
                Bukkit.getScheduler().cancelTask(RavenPlugin.tasks.get(e.getPlayer().getName()));
                RavenPlugin.tasks.remove(e.getPlayer().getName());
                e.getPlayer().sendMessage(ChatColor.GRAY + "Warp cancelled!");
            }
        }
    }


    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {

        if (e.getTo().getWorld().getEnvironment() != Environment.THE_END && e.getFrom().getWorld().getEnvironment() == Environment.THE_END) {
            RavenPlugin.get().getBossBarManager().unregisterPlayer(e.getPlayer());

        } else {

            if (e.getCause() == TeleportCause.ENDER_PEARL) {

                if (RegionManager.get().hasTag(e.getTo(), "portal") || RegionManager.get().hasTag(e.getTo(), "spawn")) {
                    e.setCancelled(true);
                }
            }
        }
        if (e.getCause() != TeleportCause.ENDER_PEARL) {
            if (plugin.getServerManager().isSpawn(e.getTo()) && !plugin.getServerManager().isSpawn(e.getFrom())) {
                if (!RavenPlugin.spawnprot.contains(e.getPlayer().getName())) {
                    RavenPlugin.spawnprot.add(e.getPlayer().getName());
                }
            }
        }
    }

    @EventHandler
    public void onent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (e instanceof EntityDamageByEntityEvent) {
                if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                    Player da = (Player) ((EntityDamageByEntityEvent) e).getDamager();

                    if (isAttackBanned(da)) {
                        e.setCancelled(true);
                        da.sendMessage(ChatColor.RED + "You cannot attack players while on spawn cooldown!");
                        return;
                    }

                }
                if (((EntityDamageByEntityEvent) e).getDamager() instanceof Projectile) {
                    Projectile pr = (Projectile) (((EntityDamageByEntityEvent) e).getDamager());

                    if (pr.getShooter() instanceof Player) {
                        Player da = (Player) pr.getShooter();

                        if (isAttackBanned(da)) {
                            e.setCancelled(true);
                            da.sendMessage(ChatColor.RED + "You cannot attack players while on spawn cooldown!");
                            return;
                        }
                    }
                }
            }

            if (!plugin.getServerManager().isSpawn(p.getLocation())) {

                if (e instanceof EntityDamageByEntityEvent) {
                    if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                        Player da = (Player) ((EntityDamageByEntityEvent) e).getDamager();

                        RavenPlugin.spawnprot.remove(da.getName());

                    }
                }
                RavenPlugin.spawnprot.remove(p.getName());

            }
            ((CraftPlayer) p).getHandle().getDataWatcher().watch(9, (byte) 0);

            if (RavenPlugin.spawnprot.contains(p.getName())) {
                e.setCancelled(true);
            }
            if (e instanceof EntityDamageByEntityEvent) {
                if (((EntityDamageByEntityEvent) e).getDamager().getType() == EntityType.SNOWBALL) {
                    if (((Snowball) ((EntityDamageByEntityEvent) e).getDamager()).getShooter() instanceof Player) {
                        Player damager = (Player) ((Snowball) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
                        if (RavenPlugin.spawnprot.contains(damager.getName()) && !RavenPlugin.spawnprot.contains(p.getName())) {
                            RavenPlugin.spawnprot.remove(damager.getName());
                            damager.sendMessage(ChatColor.GRAY + "You no longer have spawn protection.");

                            if (damager.getWorld().getEnvironment() == Environment.THE_END) {
                                TimeLock tl = new TimeLock();
                                RavenPlugin.get().getBossBarManager().registerMessage(damager, tl, 200);
                                timeLocks.put(damager.getName(), tl);
                            }
                        }
                    }
                }
                if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                    Player da = (Player) ((EntityDamageByEntityEvent) e).getDamager();
                    if (RavenPlugin.spawnprot.contains(da.getName()) && !RavenPlugin.spawnprot.contains(p.getName())) {
                        RavenPlugin.spawnprot.remove(da.getName());
                        da.sendMessage(ChatColor.GRAY + "You no longer have spawn protection.");

                        if (da.getWorld().getEnvironment() == Environment.THE_END) {
                            TimeLock tl = new TimeLock();
                            RavenPlugin.get().getBossBarManager().registerMessage(da, tl, 200);
                            timeLocks.put(da.getName(), tl);
                        }

                    }
                }
                if (((EntityDamageByEntityEvent) e).getDamager() instanceof Arrow) {
                    if (((Arrow) ((EntityDamageByEntityEvent) e).getDamager()).getShooter() instanceof Player) {
                        Player da = (Player) ((Arrow) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
                        if (RavenPlugin.spawnprot.contains(da.getName()) && !RavenPlugin.spawnprot.contains(p.getName())) {
                            RavenPlugin.spawnprot.remove(da.getName());
                            da.sendMessage(ChatColor.GRAY + "You no longer have spawn protection.");

                            if (da.getWorld().getEnvironment() == Environment.THE_END) {
                                TimeLock tl = new TimeLock();
                                RavenPlugin.get().getBossBarManager().registerMessage(da, tl, 200);
                                timeLocks.put(da.getName(), tl);
                            }

                        }
                    }
                }
            }

        }
    }

    @EventHandler
    public void onCommandPre(PlayerCommandPreprocessEvent e) {
        if (e.getMessage().startsWith("/me "))
            e.setCancelled(true);
        if (e.getMessage().startsWith("/i ")) {
            e.setCancelled(true);
            e.getPlayer().chat(e.getMessage().replace("/i ", "/item "));
        }
        // easter eggs ftw
        if (e.getMessage().startsWith("/i love you")) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("I love you too, §c❤");
        }
        if ((e.getMessage().toLowerCase().startsWith("/sell ") || e.getMessage().toLowerCase().startsWith("/buy ")) && !RavenPlugin.spawnprot.contains(e.getPlayer().getName())) {
            Location loc = ((Player) e.getPlayer()).getLocation();
            if ((loc.getWorld().getEnvironment() == Environment.NORMAL)) {

                if (Math.abs(loc.getX()) < 512 && Math.abs(loc.getZ()) < 512) {
                    e.getPlayer().sendMessage(ChatColor.RED + "You cannot use the economy within 512 blocks of spawn.");
                    e.setCancelled(true);
                }
            }
            if ((loc.getWorld().getEnvironment() == Environment.THE_END)) {

                e.getPlayer().sendMessage(ChatColor.RED + "You cannot use the economy in The End!");
                e.setCancelled(true);
            }
        }

        if (e.getPlayer().isOp()) {
            if (e.getMessage().startsWith("/SETSPAWN HERE")) {
                Location loc = e.getPlayer().getLocation();
                e.setCancelled(true);
                e.getPlayer().getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && RavenPlugin.get().getServerManager().isSpawn(e.getPlayer().getLocation())) {
            if (!RavenPlugin.spawnprot.contains(e.getPlayer().getName()))
                e.setCancelled(true);
        }
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            double h = ((Damageable) e.getPlayer()).getHealth();
            if (e.getPlayer().getItemInHand().getTypeId() == 282 && h < 20D) {
                e.setCancelled(true);
                Player p = e.getPlayer();
                p.setHealth((p.getHealth() + 7) > 20D ? 20 : p.getHealth() + 7); // Ternary ^.^
                p.getItemInHand().setType(Material.BOWL);
            } else if (e.getPlayer().getItemInHand().getTypeId() == 282 && e.getPlayer().getFoodLevel() < 20) {
                e.setCancelled(true);
                Player p = e.getPlayer();
                p.setFoodLevel((p.getFoodLevel() + 7) > 20D ? 20 : p.getFoodLevel() + 7); // Ternary ^.^
                p.getItemInHand().setType(Material.BOWL);
            }
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        if (e.getBlock().getType() == Material.NETHERRACK && !isSurroundedByWater(e.getBlock()) && e.getPlayer().getItemInHand().getType() != null && e.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE && e.getItemInHand().getEnchantments().containsKey(Enchantment.DIG_SPEED) && e.getItemInHand().getEnchantmentLevel(Enchantment.DIG_SPEED) > 2) {
            e.getPlayer().sendBlockChange(e.getBlock().getLocation(), 0, (byte) 0);
        }
    }

    public boolean isSurroundedByWater(Block b) {
        for (BlockFace bf : BlockFace.values()) {
            if (b.getRelative(bf).isLiquid())
                return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(final BlockPlaceEvent e) {
        if (!e.getPlayer().hasPermission("raven.spawn.build") && RavenPlugin.spawnprot.contains(e.getPlayer().getName()) && plugin.getServerManager().isSpawn(e.getPlayer().getLocation())) {
            e.setBuild(false);
            e.setCancelled(true);
        }
        Location locs = e.getBlock().getLocation();
        if (Math.abs(locs.getBlockX()) > ServerManager.getBorderDistance() || Math.abs(locs.getBlockZ()) > ServerManager.getBorderDistance()) {

            e.setCancelled(true);
            e.setBuild(false);
            return;
        }

    }

    public boolean isTouching(Block b, Material m) {
        BlockFace[] bfs = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST};
        for (BlockFace bf : bfs) {
            if (b.getRelative(bf).getType() == m)
                return true;
        }
        return false;
    }

    public boolean isTouchingDouble(Block b, Material m, Material m2) {
        BlockFace[] bfs = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST};
        for (BlockFace bf : bfs) {

            if (b.getRelative(bf).getType() == m) {

                for (BlockFace bf2 : bfs) {

                    if (b.getRelative(bf).getRelative(bf2).getType() == m2)
                        return true;
                }
            }
        }
        return false;
    }

    public int getLevel(ItemStack item) {
        int level = 0;
        for (Enchantment e : item.getEnchantments().keySet()) {
            level += item.getEnchantments().get(e);
        }
        return level * 1;
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        ItemStack i = e.getItemDrop().getItemStack();
        if (i.hasItemMeta()) {
            if (i.getItemMeta().getLore() != null) {
                if (i.getItemMeta().getLore().get(0).contains("seconds:")) {
                    e.getItemDrop().remove();
                }
            }
        }
    }

    private void startUpdate(final Furnace tile, final int increase) {
        new BukkitRunnable() {

            @Override
            public void run() {
                if (tile.getCookTime() > 0 || tile.getBurnTime() > 0) {
                    tile.setCookTime((short) (tile.getCookTime() + increase));
                    tile.update();
                } else {
                    cancel();
                }
            }

        }.runTaskTimer(RavenPlugin.get(), 1L, 1L);
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        startUpdate((Furnace) event.getBlock().getState(), RavenPlugin.RANDOM.nextBoolean() ? 1 : 2); // Averages
        // to
        // 1.5
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getType() == Material.BEDROCK) {
            e.getEntity().remove();
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPortale(EntityPortalEvent e) {
        if (e.getEntityType().toString().contains("MINECART")) {
            e.getEntity().remove();
            e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.ENDER_SIGNAL, 110);
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ANVIL_BREAK, 20L, 1L);
            e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.EXPLODE, 20L, 2L);
            e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.MOBSPAWNER_FLAMES, 14111);

        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        Location locs = e.getTo();
        if (Math.abs(locs.getBlockX()) > ServerManager.getBorderDistance() || Math.abs(locs.getBlockZ()) > ServerManager.getBorderDistance()) {
            e.getPlayer().sendMessage(ChatColor.RED + "That portal's location is past the border.");
            if (locs.getX() > ServerManager.getBorderDistance() - 1)
                locs.setX(ServerManager.getBorderDistance() - 3);
            if (locs.getZ() > ServerManager.getBorderDistance() - 1)
                locs.setZ(ServerManager.getBorderDistance() - 3);
            e.setCancelled(true);

        }


    }

    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (!e.getTo().getChunk().isLoaded()) {
            e.getTo().getChunk().load();
        }

        if (e.getTo().getWorld().getName().equals(e.getFrom().getWorld().getName())) {
            if (e.getTo().distance(e.getFrom()) > 0) {
                Location locs = e.getTo();
                if (Math.abs(locs.getBlockX()) > ServerManager.getBorderDistance() || Math.abs(locs.getBlockZ()) > ServerManager.getBorderDistance()) {
                    e.getPlayer().sendMessage(ChatColor.RED + "That location is past the border.");
                    if (locs.getX() > ServerManager.getBorderDistance() - 1)
                        locs.setX(ServerManager.getBorderDistance() - 3);
                    if (locs.getZ() > ServerManager.getBorderDistance() - 1)
                        locs.setZ(ServerManager.getBorderDistance() - 3);
                    e.setTo(e.getFrom());
                    e.getPlayer().setVelocity(new Vector(0, 0, 0));

                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        double fromY = from.getY();
        Location to = e.getTo();
        double toY = to.getY();
        double fromX = from.getX();
        double fromZ = from.getZ();
        double toX = to.getX();
        double toZ = to.getZ();
        if (fromX != toX || fromZ != toZ || fromY != toY) {
            Location locs = e.getTo();
            if (Math.abs(locs.getBlockX()) > ServerManager.getBorderDistance() || Math.abs(locs.getBlockZ()) > ServerManager.getBorderDistance()) {
                if (e.getPlayer().getVehicle() != null)
                    e.getPlayer().getVehicle().eject();
                e.getPlayer().sendMessage(ChatColor.RED + "You have hit the border!");
                Location loc = e.getFrom();
                loc.setPitch(e.getPlayer().getLocation().getPitch());
                loc.setYaw(e.getPlayer().getLocation().getYaw());
                e.getPlayer().teleport(loc);
                Vector vec = e.getFrom().toVector().subtract(e.getTo().toVector()).normalize();
                e.getPlayer().setVelocity(vec.multiply(0.8));

            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        RavenPlugin.get().getBossBarManager().unregisterPlayer(event.getPlayer());
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        e.setJoinMessage(null);
        if (!e.getPlayer().hasPlayedBefore()) {
            e.getPlayer().teleport(plugin.getServerManager().getSpawn(e.getPlayer().getWorld()));
            RavenPlugin.spawnprot.remove(e.getPlayer().getName());
            RavenPlugin.spawnprot.add(e.getPlayer().getName());
        }
        if (!RavenPlugin.spawnprot.contains(e.getPlayer().getName()))
            RavenPlugin.get().getServerManager().disablePlayerAttacking(e.getPlayer());

        e.getPlayer().performCommand("hud on");

    }

    @EventHandler
    public void onOeonin(InventoryOpenEvent e) {
        if (e.getInventory().getType() == InventoryType.ENDER_CHEST) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeam(TeamEvent event) {
    }


    // @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof MushroomCow) {
            MushroomCow moo = (MushroomCow) event.getRightClicked();

            event.setCancelled(true);

            if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.EMERALD) {
                ItemStack s = event.getPlayer().getItemInHand();

                if (moo.isAdult()) {

                    EntityMushroomCow cow = ((CraftMushroomCow) moo).getHandle();


                    NBTTagCompound compound = new NBTTagCompound();
                    cow.b(compound);

                    if (!moo.hasMetadata("LastBred") || moo.hasMetadata("LastBred") && System.currentTimeMillis() >=
                            (moo.getMetadata("LastBred").size() > 0 ? moo.getMetadata("LastBred").get(0).asLong() : -1L)) {
                        //  event.getPlayer().sendMessage("Last Bred Diff: " + ( (moo.getMetadata("LastBred").size() > 1 ? moo.getMetadata("LastBred").get(0).asLong() : -1L ) - System.currentTimeMillis()));
                        if (s.getAmount() == 1) {
                            event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                        } else {
                            ItemStack s2 = s.clone();
                            s2.setAmount(s.getAmount() - 1);
                            event.getPlayer().setItemInHand(s2);
                        }

                        compound.setLong("InLove", (20 * 30)); // 30 seconds to breed and search etc.
                        moo.setMetadata("LastBred", new FixedMetadataValue(RavenPlugin.get(), System.currentTimeMillis() + 300000)); // only breed cows once every 5 minutes.
                        cow.a(compound);

                        for (int i = 0; i < 7; i++) {
                            double rand = RavenPlugin.RANDOM.nextGaussian() * 0.02;
                            event.getPlayer().getWorld().spigot().playEffect(moo.getEyeLocation().add(rand, 0, rand), Effect.HEART);
                        }
                    }
                }
            } else if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.WHEAT) {
                event.getPlayer().sendMessage("§cYou cannot breed Mushroom Cows with wheat, use emeralds!");
            }

        }
    }


    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        /*
          Trello: Decrease chance of receiving protection enchantment by 35%

          Note: I was hesitant (To an extent I still am) to use this, but after checking the Minecraft source, it appears
          enchanting is completely random. I was not sure if there was some formula to calculate the enchantments you are going to get on the item.
          The only "formula" that appears is the enchantability of the item being enchanted. This should give it around a 65 out of 100 chance (65%) of
          KEEPING the Protection enchant (Reason I say keeping, is because I believe this is called when an item is enchanted, not when it is about to be.)
          If it falls into that 65% it should leave that enchant. If it does not, it removes the enchantment (~35% of NOT keeping the enchant), thus a ~35% reduction of
          getting protection IF the item was going to have Protection.

          ISSUE: Books...lol. (Recalculate enchantments for books.
         */
        if (event.getEnchantsToAdd().containsKey(Enchantment.PROTECTION_ENVIRONMENTAL)) {

            if (RavenPlugin.RANDOM.nextInt(100) <= 65) {

                if (event.getEnchantsToAdd().size() <= 1) { // dunno why it would be less than or equal too but meh
                    int prevLevel = event.getEnchantsToAdd().get(Enchantment.PROTECTION_ENVIRONMENTAL); // bit of a lazy workaround so i don't have to recalculate levels...
                    event.setCancelled(true);
                    event.getEnchantsToAdd().remove(Enchantment.PROTECTION_ENVIRONMENTAL);
                    List<Enchantment> enchantments;
                    HashMap<Enchantment, Integer> newEnchantstoAdd = new HashMap<>();
                    if (isArmor(event.getItem())) {
                        // Default valid enchantments for armor
                        enchantments = new ArrayList<Enchantment>() {{
                            add(Enchantment.DURABILITY);
                            add(Enchantment.PROTECTION_EXPLOSIONS);
                            add(Enchantment.PROTECTION_FIRE);
                            add(Enchantment.PROTECTION_PROJECTILE);
                            add(Enchantment.THORNS); // You can get this enchant on ALL armor.
                        }};

                        // Helmet
                        if (event.getItem().getType().name().contains("HELMET")) {
                            enchantments.add(Enchantment.OXYGEN); // Respiration
                            enchantments.add(Enchantment.WATER_WORKER); // Aqua Affinity

                            // Boots
                        } else if (event.getItem().getType().name().contains("BOOTS")) {
                            enchantments.add(Enchantment.PROTECTION_FALL);
                        }

                        int index = RavenPlugin.RANDOM.nextInt(enchantments.size());
                        Enchantment enchToAdd = enchantments.get(index);

                        prevLevel = prevLevel > enchToAdd.getMaxLevel() ? enchToAdd.getMaxLevel() : prevLevel; // should make it so we can't get Aqua Affinity V or something.
                        newEnchantstoAdd.put(enchToAdd, prevLevel);
                        Event event1 = new EnchantItemEvent(
                                event.getEnchanter(),
                                event.getView(),
                                event.getEnchantBlock(),
                                event.getItem(),
                                event.getExpLevelCost(),
                                newEnchantstoAdd,
                                event.whichButton()
                        );
                        Bukkit.getPluginManager().callEvent(event1);


                        // Should be a book. If not then why does it have protection on it!
                    } else {
                        enchantments = Arrays.asList(Enchantment.values());
                        enchantments.remove(Enchantment.PROTECTION_ENVIRONMENTAL); // should be in the list. Welp now its not!

                        int index = RavenPlugin.RANDOM.nextInt(enchantments.size()); // I think this is inclusive/exclusive so thee should be no ArrayIndexOutOfBounds, i can't
                        // really test right now, don't have a server
                        Enchantment enchToAdd = enchantments.get(index);

                        prevLevel = prevLevel > enchToAdd.getMaxLevel() ? enchToAdd.getMaxLevel() : prevLevel; // should make it so we can't get Aqua Affinity V or something.
                        newEnchantstoAdd.put(enchToAdd, prevLevel);
                        Event event1 = new EnchantItemEvent(
                                event.getEnchanter(),
                                event.getView(),
                                event.getEnchantBlock(),
                                event.getItem(),
                                event.getExpLevelCost(),
                                newEnchantstoAdd,
                                event.whichButton()
                        );
                        Bukkit.getPluginManager().callEvent(event1);

                    }


                } else {
                    event.getEnchantsToAdd().remove(Enchantment.PROTECTION_ENVIRONMENTAL);
                }
            }
        }


        // 5000
        if (inRange(event.getExpLevelCost(), 25, 30) && RavenPlugin.RANDOM.nextInt(5000) == 0) {
            ItemStack ascendedSomething = new AscendedItem(event.getEnchanter().getDisplayName(),
                    // 50/50 chance of recieving a pick or a sword
                    new ItemStack((RavenPlugin.RANDOM.nextInt(1) == 0 ? Material.DIAMOND_PICKAXE : Material.DIAMOND_SWORD))).getParent();
            event.getEnchanter().getWorld().dropItem(event.getEnchanter().getLocation(), ascendedSomething).setVelocity(new Vector(0, 0, 0));
        }

    }

    /**
     * Returns true if the stack is Armor, false if not.
     *
     * @param stack
     * @return
     */
    private boolean isArmor(ItemStack stack) {
        return CraftItemStack.asNMSCopy(stack).getItem() instanceof ItemArmor;
    }


    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        if (killed.getKiller() != null) {
            Player killer = killed.getKiller();
            if (killer.getItemInHand() != null) {
                ItemStack stack = killer.getItemInHand();
                // May talk to Ramsey about adding axes. I know they are not commonly used, but they might be used,
                // and are classified as secondary weapons according to Minecraft enchanting.
                if (stack.getType().name().contains("SWORD") || stack.getType() == Material.BOW) {
//                    KillTrackerStack killTrack = KillTrackerStack.parseFromStack(stack);
//                    killTrack.addKill(killer.getDisplayName(), killed.getDisplayName());
//                    killer.setItemInHand(killTrack.toItem());
//                    killer.updateInventory();

                    short durabilty = stack.getDurability();
                    addKill(stack, killer.getDisplayName(), killed.getDisplayName());
                    stack.setDurability(durabilty);
                    killer.updateInventory();
                }
            }
            // 3000
            if (RavenPlugin.RANDOM.nextInt(3000) == 0 && isFullSet(killed.getInventory().getArmorContents(), VALID_ARMOR)
                    && killed.getActivePotionEffects().size() > 0) {
                ItemStack ascendedSword = new AscendedItem(killer.getDisplayName(), new ItemStack(Material.DIAMOND_SWORD)).getParent();
                killer.getWorld().dropItem(killer.getLocation(), ascendedSword).setVelocity(new Vector(0, 0, 0));
                FancyMessage message = generateItemMessage(ascendedSword, killer, false, killed);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    message.send(p);
                }

            }

        }

    }


    public boolean isFullSet(ItemStack[] armor, Material[] targets) {
        boolean isFull = true;
        for (ItemStack stack : armor) {
            if (stack == null) {
                isFull = false;
            } else if (!Arrays.asList(targets).contains(stack.getType())) {
                isFull = false;
            }
        }
        return isFull;
    }

    @EventHandler
    public void onWrench(PlayerInteractEvent event) {
        if (event.hasBlock() && event.hasItem() && event.getAction() == Action.RIGHT_CLICK_BLOCK ||
                event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ItemStack wrench = event.getItem();
            Block spawner = event.getClickedBlock();
            if (wrench != null) {
                if (Wrench.isWrench(wrench)) {
                    event.setCancelled(true);
                    if (spawner.getType() == Material.MOB_SPAWNER) {
                        CreatureSpawner spawner1 = (CreatureSpawner) spawner.getState();
                        ItemStack spawner_item = NbtFactory.getCraftItemStack(new ItemStack(Material.MOB_SPAWNER));
                        NbtFactory.NbtCompound compound = NbtFactory.fromItemTag(spawner_item);
                        compound.put("mobType", spawner1.getSpawnedType().name());
                        NbtFactory.setItemTag(spawner_item, compound);
                        spawner.getWorld().dropItemNaturally(spawner.getLocation(), spawner_item);
                        spawner.setType(Material.AIR);
                        event.getPlayer().playSound(spawner.getLocation(), Sound.HORSE_SKELETON_DEATH, 1, 0.1f);
                        ItemUtils.pop(event.getPlayer(), wrench);
                        event.getPlayer().sendMessage(ChatColor.RED + "Your wrench slowly fades away...");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        if (event.getItemInHand() != null) {
            ItemStack stack = event.getItemInHand();
            if (stack.getType() == Material.MOB_SPAWNER && event.getBlockPlaced().getType() == Material.MOB_SPAWNER) {
                NbtFactory.NbtCompound compound = NbtFactory.fromItemTag(NbtFactory.getCraftItemStack(stack));
                if (compound.containsKey("mobType")) {
                    EntityType spawnType = EntityType.valueOf(compound.getString("mobType", "PIG"));
                    CreatureSpawner spawner = (CreatureSpawner) event.getBlockPlaced().getState();
                    spawner.setSpawnedType(spawnType);
                    spawner.update();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCriticalAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            EntityPlayer player = ((CraftPlayer) e.getDamager()).getHandle();
            net.minecraft.server.v1_7_R4.Entity entity = ((CraftEntity) e.getEntity()).getHandle();
            if (entity.av()) {
                if (!entity.i(player)) {
                    float f = (float) player.getAttributeInstance(GenericAttributes.e).getValue();
                    float f1 = 0.0F;

                    if (entity instanceof EntityLiving) {
                        f1 = EnchantmentManager.a((EntityLiving) player, (EntityLiving) entity);
                    }

                    if (f > 0.0F || f1 > 0.0F) {
                        boolean flag = player.fallDistance > 0.0F && !player.onGround && !player.h_() && !player.M() && !player.hasEffect(MobEffectList.BLINDNESS) && player.vehicle == null && entity instanceof EntityLiving;

                        if (flag && f > 0.0F) {
                            e.setDamage(e.getDamage() / 1.5);
                        }
                    }
                }
            }

        }
    }

    /**
     * @param inventory
     * @param stack
     * @return true if the item was successfully added to the inventory, false if not
     * @author rbrick
     */
    public boolean addItem(Inventory inventory, ItemStack stack) {
        HashMap<Integer, ItemStack> stacksThatCouldNotBeAdded = inventory.addItem(stack);
        return !(stacksThatCouldNotBeAdded.size() > 0) || (stacksThatCouldNotBeAdded.size() >= 1 && !stacksThatCouldNotBeAdded.get(0).equals(stack));
    }

    public FancyMessage generateItemMessage(ItemStack stack, Player p, boolean pick, @Nullable Player killed) {
        return new FancyMessage()
                .text(p.getDisplayName() + " §breceived " + (startsWithVowel(ChatColor.stripColor(stack.getItemMeta().getDisplayName()))
                        ? "an" : "a"))
                .color(ChatColor.AQUA).then()
                .text(stack.getItemMeta().getDisplayName())
                .itemTooltip(stack).then()
                .text((pick ? " while mining!" : (killed == null) ?
                        " while enchanting!" : " by killing " + killed.getName() + "!")).color(ChatColor.AQUA);
    }

    public boolean inRange(int provided, int min, int max) {
        return provided >= min && provided <= max;
    }


    /**
     * @param item
     * @param kill
     * @return
     */
    public ItemStack addKill(ItemStack item, String who, String kill) {
        int beginIndex = -1;
        int killIndex = -1;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = item.getItemMeta().getLore() == null ? new ArrayList<String>() : item.getItemMeta().getLore();

        for (int i = 0; i < lore.size(); i++) {
            String l = lore.get(i);
            if (ChatColor.stripColor(l).contains("Kills:")) {
                beginIndex = i + 2; // "Kills"
                killIndex = i;
            }
        }

        if (beginIndex == -1 && killIndex == -1) {
            lore.add(" ");
            lore.add("§6§lKills: §f" + 1);
            lore.add(" ");
            lore.add(who + " §ekilled §f" + kill);
        } else {
            String killnum = ChatColor.stripColor(lore.get(killIndex)).replace(" ", "").replace("Kills:", "");
            int kills = Integer.parseInt(killnum); // in theory
            List<String> playerKills = new ArrayList<>();
            for (int i = beginIndex; i < lore.size(); i++) {
                playerKills.add(lore.get(i));
            }
            KillTrackerStack blah = new KillTrackerStack(kills, playerKills);
            blah.addKill(who, kill);
            lore.set(killIndex, "§6§lKills: §f" + blah.getKillCount());
            // If there is only 1 kill, there would only be 1 element left to loop through.
            // so count until i == lore.size OR j == kills.size so both can be complete.
            // TODO (Not really a todo, just an improvement): check kills with regex (something like [^\\skilled\\s]) in case more lore related things are added.
            for (int i = beginIndex, j = 0; i < lore.size() || j < blah.getKills().size(); i++, j++) {
                try {
                    lore.set(i, blah.getKills().get(j));
                } catch (Exception ex) {
                    lore.add(blah.getKills().get(j));
                }

            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public boolean startsWithVowel(String s) {
        char first = s.toLowerCase().toCharArray()[0];
        return first == 'a' || first == 'e' || first == 'i' || first == 'o' || first == 'u';
    }

}
