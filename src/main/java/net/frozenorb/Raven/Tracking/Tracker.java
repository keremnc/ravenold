package net.frozenorb.Raven.Tracking;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.frozenorb.Raven.GameEvents.Tracking.TrackingEvent;
import net.frozenorb.Raven.GameEvents.Tracking.TrackingEventManager;
import net.frozenorb.Utilities.Message.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents a Tracker structure in the game, used to track other players.
 *
 * @author Kerem Celik
 */
@AllArgsConstructor
@SuppressWarnings("deprecation")
public class Tracker {

    public static final int TRACK_EMERALD_LENGTH = 0;
    public static final int TRACK_IRON_LENGTH = 0;
    public static final int TRACK_OBBY_LENGTH = 25;
    public static final int TRACK_COBBLE_LENGTH = 25;

    int mx;
    int my;
    int mz;

    /**
     * Gets if the player is currently trackable with this tracker.
     *
     * @param pl player to check
     * @param x  amount to check in x
     * @param z  amount to check in z
     * @return if the player is in a valid tracking position
     */
    public boolean checkPlayer(Player pl, int x, int z) {
        int num = 0;
        if (x == 0) {
            int plz = pl.getLocation().getBlockZ();
            num = Math.abs(z);
            if (Math.abs(this.mz - plz) <= num) {
                if (z <= 0) {
                    if (plz <= this.mz) {
                        return true;
                    }
                } else if (plz >= this.mz) {
                    return true;
                }
            }
        } else if (z == 0) {
            int plz = pl.getLocation().getBlockX();
            num = Math.abs(x);
            if (Math.abs(this.mx - plz) <= num) {
                if (x <= 0) {
                    if (plz <= this.mx) {
                        return true;
                    }
                } else if (plz >= this.mx) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkLocation(Location pl, int x, int z) {
        int num = 0;
        if (x == 0) {
            int plz = pl.getBlockZ();
            num = Math.abs(z);
            if (Math.abs(this.mz - plz) <= num) {
                if (z <= 0) {
                    if (plz <= this.mz) {
                        return true;
                    }
                } else if (plz >= this.mz) {
                    return true;
                }
            }
        } else if (z == 0) {
            int plz = pl.getBlockX();
            num = Math.abs(x);
            if (Math.abs(this.mx - plz) <= num) {
                if (x <= 0) {
                    if (plz <= this.mx) {
                        return true;
                    }
                } else if (plz >= this.mx) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Tracks a player in one given direction.
     *
     * @param tracker the player who's tracking
     * @param x       amount to track on x axis
     * @param z       amount to track on z axis
     * @param victim  the person to track
     */
    public void trackDir(Player tracker, int x, int z, Player victim) {
        String compass = "North";
        List<Player> plist = TrackerHelper.getTrackablePlayers();
        List<String> in = new ArrayList<String>();
        int num = Math.abs(x) + Math.abs(z);
        if (victim == null) {
//            for (int i = 0; i < plist.size(); i++) {
//                Player pl = (Player) plist.get(i);
//                if (checkPlayer(pl, x, z))
//                    in.add(pl.getDisplayName());
//            }
            // NO-OP
            return;
        } else {
            boolean can = checkPlayer(victim, x, z);
            if (can) {
                in.add(victim.getDisplayName());
            }
        }

        boolean canB = TrackingEvent.isStarted() && TrackingEventManager.getLocation() != null && checkLocation(TrackingEventManager.getLocation(), x, z);

        if (canB) {
            in.add("§cSupply Crates");
        }

        if (z < 0)
            compass = "North";
        else if (z > 0)
            compass = "South";
        if (x < 0)
            compass = "West";
        else if (x > 0)
            compass = "East";
        final ArrayList<String> strList = new ArrayList<String>();
        for (String s : in) {
            strList.add(ChatColor.stripColor(s));
        }
        if (victim == null) {
            strList.remove(tracker.getName());

            Collections.sort(strList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return ChatColor.stripColor(o1.toLowerCase()).compareTo(ChatColor.stripColor(o2.toLowerCase()));
                }
            });
            JSONChatMessage msg = new JSONChatMessage(compass + " (" + num + "): ", JSONChatColor.DARK_AQUA, new ArrayList<JSONChatFormat>()) {
                {
                    boolean first = true;
                    for (final String str : strList) {
                        if (!first)
                            addExtra(new JSONChatExtra(", ", JSONChatColor.WHITE, new ArrayList<JSONChatFormat>()));
                        first = false;
                        addExtra(new JSONChatExtra(ChatColor.stripColor(str), (str.equalsIgnoreCase("Supply Crates") ? JSONChatColor.RED : JSONChatColor.GRAY), new ArrayList<JSONChatFormat>()) {
                            {
                                if (!str.equalsIgnoreCase("Supply Crates")) {
                                    setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/track " + str);
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to track §f" + Bukkit.getPlayerExact(str).getDisplayName() + "§a.");
                                }
                            }
                        });
                    }
                }
            };
            msg.sendToPlayer(tracker);
        } else {
            if (strList.contains(victim.getName())) {
                tracker.sendMessage(ChatColor.WHITE + victim.getDisplayName() + "§a is within " + num + " blocks " + compass + " of here.");
            } else {
                tracker.sendMessage(ChatColor.WHITE + victim.getDisplayName() + "§c is NOT within " + num + " blocks " + compass + " of here.");

            }
        }
    }

    public TrackReturn gatherForDirection(Player player, int x, int z) {
        List<Player> players = TrackerHelper.getTrackablePlayers();
        List<String> in = new ArrayList<>();
        //     int num = Math.abs(x) + Math.abs(z);

        for (int i = 0; i < players.size(); i++) {
            Player pl = (Player) players.get(i);
            if (checkPlayer(pl, x, z))
                in.add(pl.getName());
        }


        boolean canB = TrackingEvent.isStarted() && TrackingEventManager.getLocation() != null && checkLocation(TrackingEventManager.getLocation(), x, z);

        if (canB) {
            in.add("§cSupply Crates");
        }

        in.remove(player.getName());

        Collections.sort(in, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return ChatColor.stripColor(o1.toLowerCase()).compareTo(ChatColor.stripColor(o2.toLowerCase()));
            }
        });

        return new TrackReturn(in, TrackDirection.getDirection(x, z));
    }


    public void trackAll(Player tracker) {
        Block block = new Location(tracker.getWorld(), tracker.getLocation().getBlockX(), tracker.getLocation().getBlockY() - 1, tracker.getLocation().getBlockZ()).getBlock();

        TrackResult northDist = findBlock(tracker.getWorld(), 0, -1, Material.GOLD_BLOCK, false);
        TrackResult eastDist = findBlock(tracker.getWorld(), 1, 0, Material.GOLD_BLOCK, false);
        TrackResult southDist = findBlock(tracker.getWorld(), 0, 1, Material.GOLD_BLOCK, false);
        TrackResult westDist = findBlock(tracker.getWorld(), -1, 0, Material.GOLD_BLOCK, false);

        TrackReturn north = null;
        TrackReturn east = null;
        TrackReturn south = null;
        TrackReturn west = null;

        final List<String> all = new ArrayList<>();

        boolean done = false;

        if (northDist.isValidTrack()) {
            done = true;
            north = gatherForDirection(tracker, 0, -northDist.getTotalBlocks());
            all.addAll(north.getPlayers());

        }

        if (eastDist.isValidTrack()) {
            done = true;
            east = gatherForDirection(tracker, eastDist.getTotalBlocks(), 0);
            all.addAll(east.getPlayers());
        }

        if (southDist.isValidTrack()) {
            done = true;
            south = gatherForDirection(tracker, 0, southDist.getTotalBlocks());
            all.addAll(south.getPlayers());
        }

        if (westDist.isValidTrack()) {
            done = true;
            west = gatherForDirection(tracker, -westDist.getTotalBlocks(), 0);
            all.addAll(west.getPlayers());
        }

        if (!done) {
            tracker.sendMessage(ChatColor.WHITE + "Not a valid tracking compass.");
        } else {
            tracker.sendMessage("§7§m-----§r §3Tracking Results §7§m-----");
            if (northDist.isValidTrack() && north != null) {
                final TrackReturn finalNorth = north;
                final List<String> allCopy = new ArrayList<>(all);
                JSONChatMessage msg = new JSONChatMessage(finalNorth.getDirection().display() + " (" + Math.abs(northDist.getTotalBlocks()) + "): ", JSONChatColor.DARK_AQUA, new ArrayList<JSONChatFormat>()) {
                    {
                        boolean first = true;
                        for (final String str : finalNorth.getPlayers()) {
                            if (!first)
                                addExtra(new JSONChatExtra(", ", JSONChatColor.WHITE, new ArrayList<JSONChatFormat>()));
                            first = false;
                            addExtra(new JSONChatExtra(ChatColor.stripColor(str), (ChatColor.stripColor(str).equalsIgnoreCase("Supply Crates") ? JSONChatColor.RED : JSONChatColor.GRAY),
                                    allCopy.remove(str) && allCopy.remove(str) ? Collections.singletonList(JSONChatFormat.BOLD) : new ArrayList<JSONChatFormat>()) {
                                {
                                    if (!ChatColor.stripColor(str).equalsIgnoreCase("Supply Crates")) {
                                        setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/track " + str);
                                        setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to track §f" + Bukkit.getPlayerExact(str).getDisplayName() + "§a.");
                                    }
                                }
                            });
                        }
                    }
                };
                msg.sendToPlayer(tracker);
            }

            if (eastDist.isValidTrack() && east != null) {
                final TrackReturn finalEast = east;
                final List<String> allCopy = new ArrayList<>(all);
                JSONChatMessage msg = new JSONChatMessage(finalEast.getDirection().display() + " (" + Math.abs(eastDist.getTotalBlocks()) + "): ", JSONChatColor.DARK_AQUA, new ArrayList<JSONChatFormat>()) {
                    {
                        boolean first = true;
                        for (final String str : finalEast.getPlayers()) {
                            if (!first)
                                addExtra(new JSONChatExtra(", ", JSONChatColor.WHITE, new ArrayList<JSONChatFormat>()));
                            first = false;
                            addExtra(new JSONChatExtra(ChatColor.stripColor(str), (ChatColor.stripColor(str).equalsIgnoreCase("Supply Crates") ? JSONChatColor.RED : JSONChatColor.GRAY),
                                    allCopy.remove(str) && allCopy.remove(str) ? Collections.singletonList(JSONChatFormat.BOLD) : new ArrayList<JSONChatFormat>()) {
                                {
                                    if (!ChatColor.stripColor(str).equalsIgnoreCase("Supply Crates")) {
                                        setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/track " + str);
                                        setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to track §f" + Bukkit.getPlayerExact(str).getDisplayName() + "§a.");
                                    }
                                }
                            });
                        }
                    }
                };
                msg.sendToPlayer(tracker);
            }

            if (southDist.isValidTrack() && south != null) {
                final TrackReturn finalSouth = south;
                final List<String> allCopy = new ArrayList<>(all);
                JSONChatMessage msg = new JSONChatMessage(finalSouth.getDirection().display() + " (" + Math.abs(southDist.getTotalBlocks()) + "): ", JSONChatColor.DARK_AQUA, new ArrayList<JSONChatFormat>()) {
                    {
                        boolean first = true;
                        for (final String str : finalSouth.getPlayers()) {
                            if (!first)
                                addExtra(new JSONChatExtra(", ", JSONChatColor.WHITE, new ArrayList<JSONChatFormat>()));
                            first = false;
                            addExtra(new JSONChatExtra(ChatColor.stripColor(str), (ChatColor.stripColor(str).equalsIgnoreCase("Supply Crates") ? JSONChatColor.RED : JSONChatColor.GRAY),
                                    allCopy.remove(str) && allCopy.remove(str) ? Collections.singletonList(JSONChatFormat.BOLD) : new ArrayList<JSONChatFormat>()) {
                                {
                                    if (!ChatColor.stripColor(str).equalsIgnoreCase("Supply Crates")) {
                                        setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/track " + str);
                                        setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to track §f" + Bukkit.getPlayerExact(str).getDisplayName() + "§a.");
                                    }
                                }
                            });
                        }
                    }
                };
                msg.sendToPlayer(tracker);
            }

            if (westDist.isValidTrack() && west != null) {
                final TrackReturn finalWest = west;
                final List<String> allCopy = new ArrayList<>(all);
                JSONChatMessage msg = new JSONChatMessage(west.getDirection().display() + " (" + Math.abs(westDist.getTotalBlocks()) + "): ", JSONChatColor.DARK_AQUA, new ArrayList<JSONChatFormat>()) {
                    {
                        boolean first = true;
                        for (final String str : finalWest.getPlayers()) {
                            if (!first)
                                addExtra(new JSONChatExtra(", ", JSONChatColor.WHITE, new ArrayList<JSONChatFormat>()));
                            first = false;
                            addExtra(new JSONChatExtra(ChatColor.stripColor(str), (ChatColor.stripColor(str).equalsIgnoreCase("Supply Crates") ? JSONChatColor.RED : JSONChatColor.GRAY),
                                    allCopy.remove(str) && allCopy.remove(str) ? Collections.singletonList(JSONChatFormat.BOLD) : new ArrayList<JSONChatFormat>()) {
                                {
                                    if (!ChatColor.stripColor(str).equalsIgnoreCase("Supply Crates")) {
                                        setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/track " + str);
                                        setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to track §f" + Bukkit.getPlayerExact(str).getDisplayName() + "§a.");
                                    }
                                }
                            });
                        }
                    }
                };
                msg.sendToPlayer(tracker);
            }
        }


    }


    /**
     * Begins tracking in one direction, and gets the amount to go in that
     * direction.
     *
     * @param world  world being tracked in
     * @param x      x offset
     * @param z      z offset
     * @param finish block to finish at
     * @param temp   if the tracker is a temporary tracker and will be destroyed on
     *               finish
     * @return result
     */
    public TrackResult findBlock(World world, int x, int z, Material finish, boolean temp) {
        boolean tracking = true;
        ArrayList<Block> obsidianTracking = new ArrayList<Block>();

        int emer = 0, obby = 0, iron = 0, cobble = 0;

        for (int i = 1; i < 30000; i++) {
            Block block = world.getBlockAt(this.mx + x * i, this.my, this.mz + z * i);
            Material bmat = block.getType();
            if (tracking) {
                if (isValidTrackingMaterial(bmat, temp)) {

                    if (bmat == Material.IRON_BLOCK) {
                        iron++;
                    }
                    if (bmat == Material.OBSIDIAN) {
                        obby++;
                    }
                    if (bmat == Material.EMERALD_BLOCK) {
                        emer++;
                    }
                    if (bmat == Material.COBBLESTONE) {
                        cobble++;
                    }
                    if (temp) {
                        obsidianTracking.add(block);
                    }
                } else {
                    if (bmat == finish) {
                        tracking = false;

                        obby++;

                        if (temp) {

                            for (Block blockss : obsidianTracking) {
                                blockss.getLocation().getWorld().playEffect(blockss.getLocation(), Effect.STEP_SOUND, blockss.getTypeId());
                                blockss.setType(Material.AIR);
                            }

                            block.getLocation().getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
                            block.setType(Material.AIR);
                        }

                        break;
                    }
                    return TrackResult.REUSLT_NONE_FOUND;
                }
            }
        }

        if ((emer + obby + iron > 0) && (!tracking)) {
            return new TrackResult(emer, obby, iron, cobble);
        }
        return TrackResult.REUSLT_NONE_FOUND;
    }

    /**
     * Tracks a player with more options.
     *
     * @param mat2    the block to use as the end block
     * @param tracker the player tracking
     * @param victim  the victim
     * @param temp    if the tracker is a temporary tracker that is removed on
     *                finish
     */
    public void track(Material mat2, Player tracker, Player victim, boolean temp) {
        Block block = new Location(tracker.getWorld(), tracker.getLocation().getBlockX(), tracker.getLocation().getBlockY() - 1, tracker.getLocation().getBlockZ()).getBlock();


        TrackResult northDist = findBlock(tracker.getWorld(), 0, -1, mat2, temp);
        TrackResult eastDist = findBlock(tracker.getWorld(), 1, 0, mat2, temp);
        TrackResult southDist = findBlock(tracker.getWorld(), 0, 1, mat2, temp);
        TrackResult westDist = findBlock(tracker.getWorld(), -1, 0, mat2, temp);

        boolean done = false;
        if (northDist.isValidTrack()) {
            done = true;
            trackDir(tracker, 0, -northDist.getTotalBlocks(), victim);
        }
        if (eastDist.isValidTrack()) {
            done = true;
            trackDir(tracker, eastDist.getTotalBlocks(), 0, victim);
        }
        if (southDist.isValidTrack()) {
            done = true;
            trackDir(tracker, 0, southDist.getTotalBlocks(), victim);
        }
        if (westDist.isValidTrack()) {
            done = true;
            trackDir(tracker, -westDist.getTotalBlocks(), 0, victim);
        }
        if (!done) {
            tracker.sendMessage(ChatColor.WHITE + "Not a valid tracking compass.");
            return;
        }

        if ((block.getType() == Material.OBSIDIAN)) {
            block.getLocation().getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
            block.setType(Material.AIR);
        }
    }

    /**
     * Begins the tracking process.
     *
     * @param player  the player doing the tracking
     * @param player2 the player being tracked, null for tracking all
     */
    public void track(Player player, Player player2) {
        Block block = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()).getBlock();
        if (block.getType() == Material.DIAMOND_BLOCK) {
            track(Material.GOLD_BLOCK, player, player2, false);
            return;
        } else if (block.getType() == Material.OBSIDIAN) {
            for (BlockFace fc : BlockFace.values()) {
                if (block.getRelative(fc).getType() == Material.COBBLESTONE || block.getRelative(fc).getType() == Material.STONE) {
                    track(Material.STONE, player, player2, true);
                    return;

                }
            }
        }
        player.sendMessage(ChatColor.WHITE + "Not a valid tracking compass.");

    }

    /**
     * Gets if the given material is valid for tracking.
     *
     * @param m material to check
     * @return if valid
     */
    public boolean isValidTrackingMaterial(Material m, boolean temp) {
        if (temp) {
            if (m == Material.COBBLESTONE) {
                return TRACK_COBBLE_LENGTH > 0;
            }
        } else {
            if (m == Material.IRON_BLOCK) {
                return TRACK_IRON_LENGTH > 0;
            }
            if (m == Material.OBSIDIAN) {
                return TRACK_OBBY_LENGTH > 0;
            }
            if (m == Material.EMERALD_BLOCK) {
                return TRACK_EMERALD_LENGTH > 0;
            }
        }
        return false;

    }

    @Data
    @AllArgsConstructor
    private static class TrackResult {

        /**
         * Result to use if the tracker is invalid.
         */
        public static final TrackResult REUSLT_NONE_FOUND = new TrackResult(0, 0, 0, 0) {
            public int getTotalBlocks() {
                return 0;
            }

            ;

            public boolean isValidTrack() {
                return false;
            }

            ;
        };

        private int emeraldBlocks, obsidianBlocks, ironBlocks, cobbleBlocks;

        /**
         * Gets the total amount of blocks to track in the chain.
         *
         * @return total amount
         */
        public int getTotalBlocks() {
            return emeraldBlocks * TRACK_EMERALD_LENGTH + obsidianBlocks * TRACK_OBBY_LENGTH + ironBlocks * TRACK_IRON_LENGTH + cobbleBlocks * TRACK_COBBLE_LENGTH;
        }

        /**
         * Gets if the result was from a valid tracker.
         *
         * @return valid
         */
        public boolean isValidTrack() {
            return !((emeraldBlocks > 0 && TRACK_EMERALD_LENGTH == 0) || (cobbleBlocks > 0 && TRACK_COBBLE_LENGTH == 0) || (ironBlocks > 0 && TRACK_IRON_LENGTH == 0) || (obsidianBlocks > 0 && TRACK_OBBY_LENGTH == 0)) && getTotalBlocks() > 0;
        }
    }

    private enum TrackDirection {
        NORTH("North"),
        SOUTH("South"),
        WEST("West"),
        EAST("East");

        String display;

        TrackDirection(String display) {
            this.display = display;
        }

        public String display() {
            return display;
        }

        public static TrackDirection getDirection(int x, int z) {
            if (z < 0)
                return NORTH;
            else if (z > 0)
                return SOUTH;
            if (x < 0)
                return WEST;
            else if (x > 0)
                return EAST;
            else
                return NORTH;
        }
    }

    @Data
    @AllArgsConstructor
    private class TrackReturn {
        List<String> players;
        TrackDirection direction;
    }

}