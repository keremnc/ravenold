package net.frozenorb.Raven.GameEvents.Koth.Listeners;

import net.frozenorb.Raven.CommandSystem.Commands.ToggleEnd;
import net.frozenorb.Raven.GameEvents.Koth.Events.*;
import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.Raven.GameEvents.Koth.KOTHScheduledTime;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.Raven.Types.KOTHKey;
import net.frozenorb.Raven.Types.LootTables.EndKothRewardLootTable;
import net.frozenorb.Raven.Types.LootTables.LootTable;
import net.frozenorb.Raven.Utilities.ImageMessage;
import net.frozenorb.Raven.Utilities.MathUtils;
import net.frozenorb.Utilities.Utils.MathUtil;
import net.frozenorb.qlib.event.HourEvent;
import net.frozenorb.qlib.util.TimeUtils;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class KOTHListener implements Listener {

    @EventHandler
    public void onKOTHActivated(KOTHActivatedEvent event) {
        if (event.getKOTH().isHidden()) {
            return;
        }

        final String[] messages;

        switch (event.getKOTH().getName().toLowerCase()) {
            case "eotw":
                messages = new String[]{
                        ChatColor.RED + "███████",
                        ChatColor.RED + "█" + ChatColor.DARK_RED + "█████" + ChatColor.RED + "█" + " " + ChatColor.DARK_RED + "[EOTW]",
                        ChatColor.RED + "█" + ChatColor.DARK_RED + "█" + ChatColor.RED + "█████" + " " + ChatColor.RED.toString() + ChatColor.BOLD + "The cap point at spawn",
                        ChatColor.RED + "█" + ChatColor.DARK_RED + "████" + ChatColor.RED + "██" + " " + ChatColor.RED.toString() + ChatColor.BOLD + "is now active.",
                        ChatColor.RED + "█" + ChatColor.DARK_RED + "█" + ChatColor.RED + "█████" + " " + ChatColor.DARK_RED + "EOTW " + ChatColor.GOLD + "can be contested now.",
                        ChatColor.RED + "█" + ChatColor.DARK_RED + "█████" + ChatColor.RED + "█",
                        ChatColor.RED + "███████"
                };

                for (Player player : RavenPlugin.get().getServer().getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1F, 1F);
                }

                break;
            case "citadel":
                messages = new String[]{
                        ChatColor.GRAY + "███████",
                        ChatColor.GRAY + "██" + ChatColor.DARK_PURPLE + "████" + ChatColor.GRAY + "█",
                        ChatColor.GRAY + "█" + ChatColor.DARK_PURPLE + "█" + ChatColor.GRAY + "█████ " + ChatColor.GOLD + "[Citadel]",
                        ChatColor.GRAY + "█" + ChatColor.DARK_PURPLE + "█" + ChatColor.GRAY + "█████ " + ChatColor.DARK_PURPLE + event.getKOTH().getName(),
                        ChatColor.GRAY + "█" + ChatColor.DARK_PURPLE + "█" + ChatColor.GRAY + "█████ " + ChatColor.GOLD + "can be contested now.",
                        ChatColor.GRAY + "█" + ChatColor.DARK_PURPLE + "█" + ChatColor.GRAY + "█████",
                        ChatColor.GRAY + "██" + ChatColor.DARK_PURPLE + "████" + ChatColor.GRAY + "█",
                        ChatColor.GRAY + "███████"
                };

                break;
            case "end":
            case "endevent":
                ImageMessage message = new ImageMessage(RavenPlugin.get().getEndermanImage(), 9, ImageMessage.ImageChar.BLOCK.getChar())
                        .appendText("", "", "", " §6[§eEnd Event§6]", " §eThe End is now contestable.");
                messages = message.getLines();
                for (Player player : RavenPlugin.get().getServer().getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1F, 1F);
                }
                break;
            default:
                messages = new String[]{
                        ChatColor.GRAY + "███████",
                        ChatColor.GRAY + "█" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "███" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "█",
                        ChatColor.GRAY + "█" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "██" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "██" + " " + ChatColor.GOLD + "[KingOfTheHill]",
                        ChatColor.GRAY + "█" + ChatColor.DARK_AQUA + "███" + ChatColor.GRAY + "███" + " " + ChatColor.YELLOW + event.getKOTH().getName() + " KOTH",
                        ChatColor.GRAY + "█" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "██" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "██" + " " + ChatColor.GOLD + "can be contested now.",
                        ChatColor.GRAY + "█" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "███" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "█",
                        ChatColor.GRAY + "█" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "███" + ChatColor.DARK_AQUA + "█" + ChatColor.GRAY + "█",
                        ChatColor.GRAY + "███████"
                };

                break;
        }

        new BukkitRunnable() {

            public void run() {
                for (Player player : RavenPlugin.get().getServer().getOnlinePlayers()) {
                    player.sendMessage(messages);
                }
            }

        }.runTaskAsynchronously(RavenPlugin.get());

        // Can't forget console now can we
        for (String message : messages) {
            RavenPlugin.get().getLogger().info(message);
        }
    }

    @EventHandler
    public void onKOTHCaptured(final KOTHCapturedEvent event) {
        if (event.getKOTH().isHidden()) {
            return;
        }

        final Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(event.getPlayer().getUniqueId());
        String teamName = ChatColor.GOLD + "[" + ChatColor.YELLOW + "-" + ChatColor.GOLD + "]";

        if (team != null) {
            teamName = ChatColor.GOLD + "[" + ChatColor.YELLOW + team.getFriendlyName() + ChatColor.GOLD + "]";
        }

        final String[] filler = {"", "", ""};
        final String[] messages;

        if (event.getKOTH().getName().equalsIgnoreCase("Citadel")) {
            messages = new String[]{
                    ChatColor.GRAY + "███████",
                    ChatColor.GRAY + "██" + ChatColor.DARK_PURPLE + "████" + ChatColor.GRAY + "█",
                    ChatColor.GRAY + "█" + ChatColor.DARK_PURPLE + "█" + ChatColor.GRAY + "█████ " + ChatColor.GOLD + "[Citadel]",
                    ChatColor.GRAY + "█" + ChatColor.DARK_PURPLE + "█" + ChatColor.GRAY + "█████ " + ChatColor.YELLOW + "controlled by",
                    ChatColor.GRAY + "█" + ChatColor.DARK_PURPLE + "█" + ChatColor.GRAY + "█████ " + teamName + ChatColor.WHITE + event.getPlayer().getDisplayName(),
                    ChatColor.GRAY + "█" + ChatColor.DARK_PURPLE + "█" + ChatColor.GRAY + "█████",
                    ChatColor.GRAY + "██" + ChatColor.DARK_PURPLE + "████" + ChatColor.GRAY + "█",
                    ChatColor.GRAY + "███████"
            };
        } else if(event.getKOTH().getName().equalsIgnoreCase("end") || event.getKOTH().getName().equalsIgnoreCase("endevent")) {
            ImageMessage message = new ImageMessage(RavenPlugin.get().getEndermanImage(), 9, ImageMessage.ImageChar.BLOCK.getChar())
                    .appendText("", "", "", " §6[§eEnd Event§6]", " §eThe End captured by " + teamName + ChatColor.WHITE + event.getPlayer().getDisplayName());
            messages = message.getLines();
            for (Player player : RavenPlugin.get().getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1F, 1F);
            }

        } else {
            messages = new String[]{
                    ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.BLUE + event.getKOTH().getName() + ChatColor.YELLOW + " has been controlled by " + teamName + ChatColor.WHITE + event.getPlayer().getDisplayName() + ChatColor.YELLOW + "!",
                    ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.YELLOW + "Awarded" + ChatColor.BLUE + " Level " + event.getKOTH().getLevel() + " Key" + ChatColor.YELLOW + " to " + teamName + ChatColor.WHITE + event.getPlayer().getDisplayName() + ChatColor.YELLOW + "."
            };
        }

        new BukkitRunnable() {

            public void run() {
                for (Player player : RavenPlugin.get().getServer().getOnlinePlayers()) {
                    player.sendMessage(filler);
                    player.sendMessage(messages);
                }
            }

        }.runTaskAsynchronously(RavenPlugin.get());

        event.getPlayer().giveExpLevels(MathUtils.random(1, 50));

        EndKothRewardLootTable rewards = new EndKothRewardLootTable();
        rewards.populate(event.getPlayer().getInventory());
        final StringBuilder builder = new StringBuilder();

        for (ItemStack itemStack : rewards.getRewards()) {
            String displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? ChatColor.RED.toString() + ChatColor.ITALIC + ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()) : ChatColor.BLUE.toString() + itemStack.getAmount() + "x " + ChatColor.YELLOW + WordUtils.capitalize(itemStack.getType().name().replace("_", " ").toLowerCase());

            builder.append(ChatColor.YELLOW).append(displayName).append(ChatColor.GOLD).append(", ");
        }

        if (builder.length() > 2) {
            builder.setLength(builder.length() - 2);
        }

        Bukkit.broadcastMessage(ChatColor.BLUE + "[End Event] §e" + event.getPlayer().getDisplayName() + " §eobtained " + builder.toString() + " §efrom capturing §9The End§e.");
        ToggleEnd.enabled = false;


        // Can't forget console now can we
        // but we don't want to give console the filler.
        for (String message : messages) {
            RavenPlugin.get().getLogger().info(message);
        }

        // TODO: Save koth win in Mongo i guess.
//        new BukkitRunnable() {
//
//            public void run() {
//                DBCollection kothCapturesCollection = RavenPlugin.get().getMongoPool().getDB("HCTeams").getCollection("KOTHCaptures");
//                BasicDBObject dbObject = new BasicDBObject();
//
//                dbObject.put("KOTH", event.getKOTH().getName());
//                dbObject.put("Level", event.getKOTH().getLevel());
//                dbObject.put("CapturedAt", new Date());
//                dbObject.put("Capper", event.getPlayer().getUniqueId().toString());
//                dbObject.put("CapperTeam", team == null ? null : team.getName()); // No Team uuid :c
//                dbObject.put("KOTHLocation", LocationSerializer.serialize(event.getKOTH().getCapLocation().toLocation(event.getPlayer().getWorld())));
//
//                kothCapturesCollection.insert(dbObject);
//            }
//
//        }.runTaskAsynchronously(RavenPlugin.get());
    }

    @EventHandler
    public void onKOTHControlLost(final KOTHControlLostEvent event) {
        if (event.getKOTH().getRemainingCapTime() <= (event.getKOTH().getCapTime() - 30)) {
            new BukkitRunnable() {

                public void run() {
                    RavenPlugin.get().getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] Control of " + ChatColor.YELLOW + event.getKOTH().getName() + ChatColor.GOLD + " lost.");
                }

            }.runTaskAsynchronously(RavenPlugin.get());
        }
    }

    @EventHandler
    public void onKOTHControlTick(KOTHControlTickEvent event) {
        if (event.getKOTH().getRemainingCapTime() % 180 == 0 && event.getKOTH().getRemainingCapTime() <= (event.getKOTH().getCapTime() - 30)) {
            RavenPlugin.get().getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.YELLOW + event.getKOTH().getName() + ChatColor.GOLD + " is trying to be controlled.");
            RavenPlugin.get().getServer().broadcastMessage(ChatColor.GOLD + " - Time left: " + ChatColor.BLUE + TimeUtils.formatIntoMMSS(event.getKOTH().getRemainingCapTime()));
        }
    }

    @EventHandler
    public void onKOTHDeactivate(KOTHDeactivatedEvent event) {
//        Bukkit.getScheduler().scheduleSyncDelayedTask(RavenPlugin.get(), new Runnable() {
//            @Override
//            public void run() {
//                for (Player player : Bukkit.getOnlinePlayers()) {
//                    RavenPlugin.get().getBossBarManager().unregisterPlayer(player);
//                }
//            }
//        }, 2*20);

        if (event.getKOTH().getName().equalsIgnoreCase("end") || event.getKOTH().getName().equalsIgnoreCase("endevent")) {
            ToggleEnd.enabled = false;
        }

    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().isOp() || !event.getLine(0).equalsIgnoreCase("[KOTH]")) {
            return;
        }

        event.setLine(0, ChatColor.translateAlternateColorCodes('&', event.getLine(1)));
        event.setLine(1, "");

        RavenPlugin.get().getKOTHHandler().getKOTHSigns().add(event.getBlock().getLocation());
        RavenPlugin.get().getKOTHHandler().saveSigns();

        event.getPlayer().sendMessage(ChatColor.GREEN + "Created a KOTH sign!");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign)) {
            return;
        }

        if (RavenPlugin.get().getKOTHHandler().getKOTHSigns().contains(event.getBlock().getLocation())) {
            RavenPlugin.get().getKOTHHandler().getKOTHSigns().remove(event.getBlock().getLocation());
            RavenPlugin.get().getKOTHHandler().saveSigns();

            event.getPlayer().sendMessage(ChatColor.GREEN + "Removed a KOTH sign!");
        }
    }

    private void activateKOTHs() {
        // Don't start a KOTH if another one is active.
        for (KOTH koth : RavenPlugin.get().getKOTHHandler().getKOTHs()) {
            if (!koth.isHidden() && koth.isActive()) {
                return;
            }
        }

        KOTHScheduledTime scheduledTime = KOTHScheduledTime.parse(new Date());

        if (RavenPlugin.get().getKOTHHandler().getKOTHSchedule().containsKey(scheduledTime)) {
            String resolvedName = RavenPlugin.get().getKOTHHandler().getKOTHSchedule().get(scheduledTime);
            KOTH resolved = RavenPlugin.get().getKOTHHandler().getKOTH(resolvedName);

            if (resolved == null) {
                RavenPlugin.get().getLogger().warning("The KOTH Scheduler has a schedule for a KOTH named " + resolvedName + ", but the KOTH does not exist.");
                return;
            }

            resolved.activate();
        }
    }

    private void terminateKOTHs() {
        KOTHScheduledTime nextScheduledTime = KOTHScheduledTime.parse(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)));

        if (RavenPlugin.get().getKOTHHandler().getKOTHSchedule().containsKey(nextScheduledTime)) {
            // We have a KOTH about to start. Prepare for it.
            for (KOTH activeKoth : RavenPlugin.get().getKOTHHandler().getKOTHs()) {
                if (!activeKoth.isHidden() && activeKoth.isActive() && !activeKoth.getName().equals("Citadel") && !activeKoth.getName().equals("EOTW")) {
                    if (activeKoth.getCurrentCapper() != null) {
                        activeKoth.setTerminate(true);
                        RavenPlugin.get().getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.BLUE + activeKoth.getName() + ChatColor.YELLOW + " will be terminated if knocked.");
                    } else {
                        activeKoth.deactivate();
                        RavenPlugin.get().getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.BLUE + activeKoth.getName() + ChatColor.YELLOW + " has been terminated.");
                    }
                }
            }
        }
    }

    // TODO: Find out why this event does not exist. Probably something silly, but i am tired and am not going to look into right now.
    @EventHandler
    public void onHalfHour(HourEvent event) {
        terminateKOTHs();
        activateKOTHs();
    }

    // @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getItem() != null) {
            KOTHKey key = KOTHKey.parseFromStack(event.getItem());
            if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST
                    || !RavenPlugin.get().getServerManager().isSpawn(event.getPlayer().getLocation())
                    || key == null) {
                return;
            }

            event.setCancelled(true);

            int open = 0;

            for (ItemStack itemStack : event.getPlayer().getInventory().getContents()) {
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    open++;
                }
            }

            if (open < 5) {
                event.getPlayer().sendMessage(ChatColor.RED + "You must have at least 5 open inventory slots to use a KOTH reward key!");
                return;
            }

            final int tier = key.getTier();
            Block block = event.getClickedBlock().getRelative(BlockFace.DOWN, tier + 3);

            if (block.getType() != Material.CHEST) {
                return;
            }

            event.getPlayer().setItemInHand(null);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.FIREWORK_BLAST, 1F, 1F);

            Chest chest = (Chest) block.getState();
            ItemStack[] lootTables = chest.getBlockInventory().getContents();
            final List<ItemStack> loot = new ArrayList<>();
            int given = 0;
            int tries = 0;

            while (given < 5 && tries < 100) {
                tries++;

                ItemStack chosenItem = lootTables[RavenPlugin.RANDOM.nextInt(lootTables.length)];

                if (chosenItem == null || chosenItem.getType() == Material.AIR || chosenItem.getAmount() == 0) {
                    continue;
                }

                // This is a dirty hack so we can run 'continue' on the while loop instead of the for loop.
                // There's no better way to do this :/
                boolean runContinue = false;

                for (ItemStack givenLoot : loot) {
                    if (givenLoot.getType() == chosenItem.getType()) {
                        runContinue = true;
                    }
                }

                if (runContinue) {
                    continue;
                }

                given++;
                loot.add(chosenItem);
            }

            final StringBuilder builder = new StringBuilder();

            for (ItemStack itemStack : loot) {
                String displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? ChatColor.RED.toString() + ChatColor.ITALIC + ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()) : ChatColor.BLUE.toString() + itemStack.getAmount() + "x " + ChatColor.YELLOW + WordUtils.capitalize(itemStack.getType().name().replace("_", " ").toLowerCase());

                builder.append(ChatColor.YELLOW).append(displayName).append(ChatColor.GOLD).append(", ");
            }

            if (builder.length() > 2) {
                builder.setLength(builder.length() - 2);
            }

            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW + " is obtaining loot for a " + ChatColor.BLUE.toString() + ChatColor.ITALIC + "Level " + tier + " Key" + ChatColor.YELLOW + " obtained from " + ChatColor.GOLD + key.getKoth() + ChatColor.YELLOW + " at " + ChatColor.GOLD + key.getTime() + ChatColor.YELLOW + ".");

            new BukkitRunnable() {

                public void run() {
                    new BukkitRunnable() {

                        public void run() {
                            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW + " obtained " + builder.toString() + ChatColor.GOLD + "," + ChatColor.YELLOW + " from a " + ChatColor.BLUE.toString() + ChatColor.ITALIC + "Level " + tier + " Key" + ChatColor.YELLOW + ".");
                        }

                    }.runTaskAsynchronously(RavenPlugin.get());

                    for (ItemStack lootItem : loot) {
                        event.getPlayer().getInventory().addItem(lootItem);
                    }

                    event.getPlayer().updateInventory();
                }

            }.runTaskLater(RavenPlugin.get(), 20 * 5L);
        }
    }


}