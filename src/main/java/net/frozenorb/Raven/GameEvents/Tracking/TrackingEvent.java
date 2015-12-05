package net.frozenorb.Raven.GameEvents.Tracking;

import net.frozenorb.Raven.GameEvents.Game;
import net.frozenorb.Raven.GameEvents.GameEvent;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Types.LootTables.LootTable;
import net.frozenorb.Raven.Types.LootTables.Tier1LootTable;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Ryan on 3/26/2015
 * <p/>
 * Project: raven
 */
@Game(name = "TrackingEvent")
public class TrackingEvent extends GameEvent {

    static boolean started = false;

    Location center;

    public TrackingEvent(@Nullable Player host) {
        super(host);
    }

    @Override
    public void run() {


        String[] parts = {
                "§7█████████",
                "§7███§9████§7██",
                "§7██§9█§7██████",
                "§7██§9█§7██████" + " §6[§eSupply Drop§6]",
                "§7███§9███§7███" + " §6Dropping supplies in 5 minutes...",
                "§7██████§9█§7██",
                "§7██████§9█§7██",
                "§7██§9████§7███",
                "§7█████████",
        };

        // Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a[Supply Drop] &fDropping in 5 minutes..."));
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.sendMessage(parts);
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                // spawn chests at random locations

                // vectors of the triangle
                if (isStarted()) {
                    Vector[] vectors = {
                            new Vector(10, 0, -10),
                            new Vector(-10, 0, -10),
                            new Vector(-10, 0, 10),
                    };

                    // determine the center of the triangle
                    Location center = new Location(Bukkit.getWorld("world"), (double) random(-3000, 3000), 90.0, (double) random(-3000, 3000));

                    TrackingEventManager.setLocation(center);

                    String[] parts = {
                            "§7█████████",
                            "§7███§9████§7██",
                            "§7██§9█§7██████",
                            "§7██§9█§7██████" + " §6[§eSupply Drop§6]",
                            "§7███§9███§7███" + " §6Dropped at X: §e" + center.toVector().getX() + "§6, Z: §e" + center.toVector().getZ(),
                            "§7██████§9█§7██",
                            "§7██████§9█§7██",
                            "§7██§9████§7███",
                            "§7█████████",
                    };

                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        p.sendMessage(parts);
                        p.playSound(p.getLocation(), Sound.WITHER_SPAWN, 1f, 1f);
                    }

                    //  Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a[Supply Drop] &fSupply drop spawned at X: " + center.toVector().getX() + ", Z: " + center.toVector().getZ()));

                    final Set<Chest> ch = new HashSet<>();

                    final LootTable lootTable = new Tier1LootTable();

                    for (final Vector vector : vectors) {

                        final Location newLoc = center.add(vector);

                        // Move 4 bits down.

                        newLoc.getWorld().loadChunk(newLoc.getBlockX() >> 4, newLoc.getBlockZ() >> 4);
                        final FallingBlock fb = Bukkit.getWorld("world").spawnFallingBlock(newLoc, Material.CHEST, (byte) 0);
                        fb.setDropItem(false);


                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Block currBlock = fb.getLocation().getBlock();
                                newLoc.getWorld().playSound(newLoc, Sound.FIREWORK_LAUNCH, 1f, 1f);

                                if (!fb.isValid() || currBlock.getRelative(BlockFace.DOWN).isLiquid() || currBlock.getLocation().getBlockY() < 63 || !isStarted()) {
                                    this.cancel();
                                    if (isStarted()) {
                                        Location fbLoc = fb.getLocation();
                                        fb.remove();
                                        fbLoc.getBlock().setType(Material.CHEST);
                                        Chest chest = (Chest) fbLoc.getBlock().getState();

                                        // populate the chests with loot tables


                                        lootTable.populate(chest.getBlockInventory());

//                                for (int i = 0; i < chest.getBlockInventory().getSize(); i++) {
//                                    if ((Math.random() * i) % i < 5) {
//                                        if (RavenPlugin.RANDOM.nextInt(1000) == 0) {
//                                            chest.getBlockInventory().setItem(i, new ItemStack(Material.MONSTER_EGG, 1, (short) 96));
//                                        } else {
//                                            // TODO: Work out loot tables. Waiting for @Thefiresgone
//                                            chest.getBlockInventory().setItem(i, new ItemStack((RavenPlugin.RANDOM.nextBoolean() ? Material.GOLD_INGOT : Material.IRON_INGOT), random(1, 6)));
//                                        }
//                                    }
//                                }

                                        chest.update();

                                        ch.add(chest);
                                    } else {
                                        fb.remove();
                                    }

                                }
                                // Bukkit.broadcastMessage("blah " + fb.getLocation().toVector().toString());
                            }
                        }.runTaskTimer(RavenPlugin.get(), 1L, 1L);

                    }


                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            TrackingEvent.setStarted(false);

                            for (Chest c : ch) {
                                c.getBlockInventory().clear();
                                c.getBlock().setType(Material.AIR);
                            }

                        }
                    }.runTaskLater(RavenPlugin.get(), (60 * 20) * 15);

                }
            }
        }.runTaskLater(RavenPlugin.get(), (60 * 20) * 5);


    }

    public Location getCenter() {
        return center;
    }

    public static boolean isStarted() {
        return started;
    }

    public static void setStarted(boolean started) {
        TrackingEvent.started = started;
    }

    private int random(int min, int max) {
        int range = max - min;
        return (int) (min + (Math.random() * range));
    }
}
