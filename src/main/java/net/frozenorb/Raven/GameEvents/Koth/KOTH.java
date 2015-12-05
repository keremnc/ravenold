package net.frozenorb.Raven.GameEvents.Koth;

import lombok.Getter;
import lombok.Setter;
import net.frozenorb.Raven.GameEvents.Koth.Events.KOTHActivatedEvent;
import net.frozenorb.Raven.GameEvents.Koth.Events.KOTHCapturedEvent;
import net.frozenorb.Raven.GameEvents.Koth.Events.KOTHControlLostEvent;
import net.frozenorb.Raven.GameEvents.Koth.Events.KOTHDeactivatedEvent;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Utilities.Types.Scrollable;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import java.util.*;

public class KOTH {

    @Getter
    private String name;
    @Getter
    private BlockVector capLocation;
    @Getter
    private String world;
    @Getter
    private int capDistance;
    @Getter
    private int capTime;
    @Getter
    private boolean hidden = false;

    @Getter
    private transient boolean active;
    @Getter
    private transient String currentCapper;
    @Getter
    private transient int remainingCapTime;
    @Getter
    private transient HashMap<String, KOTHPercentage> percentages;
    @Getter
    @Setter
    private transient int level;
    @Getter
    @Setter
    private transient boolean terminate;


    public KOTH(String name, Location location) {
        this.name = name;
        this.capLocation = location.toVector().toBlockVector();
        this.world = location.getWorld().getName();
        this.capDistance = 3;
        this.capTime = 60 * 15;
        this.level = 2;
        this.terminate = false;
        RavenPlugin.get().getKOTHHandler().getKOTHs().add(this);
        RavenPlugin.get().getKOTHHandler().saveKOTHs();
    }

    public void setLocation(Location location) {
        this.capLocation = location.toVector().toBlockVector();
        this.world = location.getWorld().getName();
        RavenPlugin.get().getKOTHHandler().saveKOTHs();
    }

    public void setCapDistance(int capDistance) {
        this.capDistance = capDistance;
        RavenPlugin.get().getKOTHHandler().saveKOTHs();
    }

    public void setCapTime(int capTime) {
        this.capTime = capTime;

        if (this.remainingCapTime > this.capTime) {
            this.capTime = capTime;
        }

        RavenPlugin.get().getKOTHHandler().saveKOTHs();
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        RavenPlugin.get().getKOTHHandler().saveKOTHs();
    }

    public boolean activate() {
        return activate(true);
    }

    public boolean activate(boolean callEvent) {
        if (active) {
            return (false);
        }

        if (callEvent) {
            RavenPlugin.get().getServer().getPluginManager().callEvent(new KOTHActivatedEvent(this));
        }
        this.active = true;
        this.currentCapper = null;
        this.remainingCapTime = this.capTime;
        this.level = 2;
        this.terminate = false;
        this.percentages = new HashMap<>();
        return (true);
    }

    public boolean deactivate() {
        if (!active) {
            return (false);
        }

        RavenPlugin.get().getServer().getPluginManager().callEvent(new KOTHDeactivatedEvent(this));

        this.active = false;
        this.currentCapper = null;
        this.remainingCapTime = this.capTime;
        this.level = 2;
        this.terminate = false;

        for (KOTHPercentage percentage : percentages.values()) {
            percentage.cancel();
        }
        RavenPlugin.TIMER.purge(); // clear all the cancelled tasks

        this.percentages = new HashMap<>();


        return (true);
    }

//    public void startCapping(Player player) {
//        if (currentCapper != null) {
//            resetCapTime();
//        }
//
//        this.currentCapper = player.getName();
//        this.remainingCapTime = capTime;
//    }

    /**
     * update globally
     */
    private void updateBossBar() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            RavenPlugin.get().getBossBarManager().registerMessage(player, new KOTHScrollable(this), 200);

        }
    }

    /**
     * update per world
     *
     * @param world - update for only one world
     */
    private void updateBossBarForWorld(World world) {
        for (Player player : world.getPlayers()) {
            RavenPlugin.get().getBossBarManager().registerMessage(player, new KOTHScrollable(this), 200);
        }
    }


    /**
     * find a world based on the environment
     *
     * @param environment - the environment you want to search for
     * @return a world
     */
    private Vector<World> findWorld(World.Environment environment) {
        Vector<World> worlds = new Vector<>();

        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment().equals(environment)) {
                worlds.add(w);
            }
        }
        return worlds;
    }

    public void startCapping(Player player) {
        KOTHPercentage percentage;

        if (percentages.containsKey(player.getName())) {
            percentage = percentages.get(player.getName());
            // already running, do not activate
            if (percentage != null) {
                percentage.cancel();
                double prevPercent = percentage.getPercentage();
                percentage = new KOTHPercentage(this, player.getName(), prevPercent);
                percentages.put(player.getName(), percentage);
                RavenPlugin.TIMER.schedule(percentage, 600, 600);
            } else {
                percentage = new KOTHPercentage(this, player.getName(), 0);
                percentage.setRunning(true); // just in case
                RavenPlugin.TIMER.schedule(percentage, 600, 600);
                percentages.put(player.getName(), percentage);
            }
        } else {
            //   System.out.println("Percentage null, creating...");
            percentage = new KOTHPercentage(this, player.getName(), 0);
            percentage.setRunning(true); // just in case
            RavenPlugin.TIMER.schedule(percentage, 600, 600);
            percentages.put(player.getName(), percentage);
        }

        currentCapper = player.getName();
        String prefix = (this.getName().equalsIgnoreCase("End") || this.getName().equalsIgnoreCase("EndEvent"))
                ?  "§9[End Event]" : "§6[KingOfTheHill]";
        player.sendMessage(prefix + " §eAttempting to control §9The End§e.");
    }

    public boolean finishCapping() {
        Player capper = RavenPlugin.get().getServer().getPlayerExact(currentCapper);

        if (capper == null) {
            resetCapture();
            return (false);
        }

        KOTHCapturedEvent event = new KOTHCapturedEvent(this, capper);
        RavenPlugin.get().getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            resetCapture();
            return (false);
        }

        deactivate();
        return (true);
    }


    public void resetCapTime() {
        RavenPlugin.get().getServer().getPluginManager().callEvent(new KOTHControlLostEvent(this));

        this.currentCapper = null;
        this.remainingCapTime = capTime;

        if (terminate) {
            deactivate();
            RavenPlugin.get().getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.BLUE + getName() + ChatColor.YELLOW + " has been terminated.");
        }
    }

    /**
     * Resets the capture
     */
    public void resetCapture() {
        RavenPlugin.get().getServer().getPluginManager().callEvent(new KOTHControlLostEvent(this));

        if (currentCapper != null) {
            KOTHPercentage percentage = percentages.get(currentCapper);

            if (percentage != null) {
                // already running, do not activate
                percentage.cancel(); // cancel
                double prevPercent = percentage.getPercentage();
                percentage = new KOTHPercentage(this, currentCapper, prevPercent);
                percentage.setIncreasing(false);
                RavenPlugin.TIMER.schedule(percentage, 2000, 2000);
                percentages.put(currentCapper, percentage); // replace the old percentage

            }
        }


        this.currentCapper = null;

        if (terminate) {
            deactivate();
            RavenPlugin.get().getServer().broadcastMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.BLUE + getName() + ChatColor.YELLOW + " has been terminated.");
        }
    }

//    protected void tick() {
//        if (currentCapper != null) {
//            Player capper = RavenPlugin.get().getServer().getPlayerExact(currentCapper);
//
//            if (capper == null || !onCap(capper) || capper.isDead() || capper.getGameMode() != GameMode.SURVIVAL || capper.hasPermission("invisible")) {
//                resetCapTime();
//            } else {
//
//
//                if (remainingCapTime % 10 == 0 && remainingCapTime > 1 && !isHidden()) {
//                    capper.sendMessage(ChatColor.GOLD + "[KingOfTheHill]" + ChatColor.YELLOW + " Attempting to control " + ChatColor.BLUE + getName() + ChatColor.YELLOW + ".");
//                }
//
//                if (remainingCapTime <= 0) {
//                    finishCapping();
//                } else {
//                    RavenPlugin.get().getServer().getPluginManager().callEvent(new KOTHControlTickEvent(this));
//                }
//
//                this.remainingCapTime--;
//            }
//        } else {
//            List<Player> onCap = new ArrayList<>();
//
//            for (Player player : RavenPlugin.get().getServer().getOnlinePlayers()) {
//                if (onCap(player) && !player.isDead() && player.getGameMode() == GameMode.SURVIVAL && !player.hasMetadata("invisible")) {
//                    onCap.add(player);
//                }
//            }
//
//            Collections.shuffle(onCap);
//
//            if (onCap.size() != 0) {
//                startCapping(onCap.get(0));
//            }
//        }
//    }


    protected void tick() {
        if (currentCapper != null) {
            Player capper = RavenPlugin.get().getServer().getPlayer(currentCapper);
            if (capper == null || !onCap(capper) || capper.isDead() || capper.getGameMode() != GameMode.SURVIVAL) {
                resetCapture();
            } else {
                if (percentages.containsKey(currentCapper)) {
                    KOTHPercentage percentage = percentages.get(currentCapper);

                    // Should never happen but you never know
                    if (percentage != null) {
                        if (percentage.isCompleted()) {
                            finishCapping();
                        }
                    }

                }
            }

        } else {
            List<Player> onCap = new ArrayList<>();

            for (Player player : RavenPlugin.get().getServer().getOnlinePlayers()) {
                if (onCap(player) && !player.isDead() && player.getGameMode() == GameMode.SURVIVAL && !player.hasMetadata("invisible")) {
                    onCap.add(player);
                }
            }

            Collections.shuffle(onCap);

            if (onCap.size() != 0) {
                startCapping(onCap.get(0));
            }


        }
      //  updateBossBarForWorld(Bukkit.getWorld(getWorld()));
    }

    public boolean onCap(Player player) {
        if (!player.getWorld().getName().equalsIgnoreCase(world)) {
            return (false);
        }

        int yDistance = player.getLocation().getBlockY() - capLocation.getBlockY();
        return (Math.abs(player.getLocation().getBlockX() - capLocation.getBlockX()) <= capDistance && yDistance >= 0 && yDistance <= 5 && Math.abs(player.getLocation().getBlockZ() - capLocation.getBlockZ()) <= capDistance);
    }


    public double getPercentage() {
        return (currentCapper == null ? 0 : (percentages.get(currentCapper) == null ||
                !percentages.containsKey(currentCapper) ? 0 : percentages.get(currentCapper).getPercentage()));
    }

    private class KOTHScrollable implements Scrollable {


        KOTH koth;

        public KOTHScrollable(KOTH koth) {
            this.koth = koth;
        }

        @Override
        public String next() {
            return ChatColor.translateAlternateColorCodes('&', "&9&l" + (koth.getName().equalsIgnoreCase("End") ||
                    koth.getName().equalsIgnoreCase("EndEvent") ? "End Event" : koth.getName())
                    + " &6" + koth.getPercentage() + "% &9Captured");

        }
    }


}