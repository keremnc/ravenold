package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.Managers.HomeManager.Home;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.Raven.Types.TimestampedLocation;
import net.frozenorb.Utilities.Message.*;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class History extends BaseCommand {

    public History() {
        super("history", new String[]{"h"});
        registerSubcommand(new Subcommand("hq") {
            @Override
            public List<String> tabComplete() {
                ArrayList<String> teamNames = new ArrayList<String>();
                for (Team tem : RavenPlugin.get().getTeamManager().getTeams()) {
                    teamNames.add(tem.getFriendlyName());
                }
                return teamNames;
            }

            @Override
            protected void syncExecute() {

                if (args.length > 1) {
                    String id = args[1];
                    sender.sendMessage(ChatColor.YELLOW + "--- Old HQs for §a" + id + "§e ---");
                    if (RavenPlugin.get().getTeamManager().teamExists(id)) {
                        for (final TimestampedLocation h : RavenPlugin.get().getTeamManager().getTeam(id).getPreviousHQs()) {
                            long diff = System.currentTimeMillis() - h.timestamp;
                            final Location l = h.loc;
                            JSONChatMessage jcm = new JSONChatMessage("HQ at ", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>());
                            jcm.addExtra(new JSONChatExtra(getString(l), JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to warp to the HQ.");
                                    setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/tppos " + l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch());
                                }
                            });
                            jcm.addExtra(new JSONChatExtra(" was set ", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>()));
                            jcm.addExtra(new JSONChatExtra(getConvertedTime(diff / 1000) + " ago.", JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§a" + getDate(h.timestamp));
                                }
                            });
                            jcm.sendToPlayer(((Player) sender));
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Team '" + id + "' could not be found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "/h " + getName() + " <identifier>");
                }

            }
        });

        registerSubcommand(new Subcommand("rally", new String[]{"r"}) {
            @Override
            public List<String> tabComplete() {
                ArrayList<String> teamNames = new ArrayList<String>();
                for (Team tem : RavenPlugin.get().getTeamManager().getTeams()) {
                    teamNames.add(tem.getFriendlyName());
                }
                return teamNames;
            }

            @Override
            protected void syncExecute() {

                if (args.length > 1) {
                    String id = args[1];
                    sender.sendMessage(ChatColor.YELLOW + "--- Old rallies for §a" + id + "§e ---");
                    if (RavenPlugin.get().getTeamManager().teamExists(id)) {
                        for (final TimestampedLocation h : RavenPlugin.get().getTeamManager().getTeam(id).getPreviousRallies()) {
                            long diff = System.currentTimeMillis() - h.timestamp;
                            final Location l = h.loc;
                            JSONChatMessage jcm = new JSONChatMessage("Rally at ", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>());
                            jcm.addExtra(new JSONChatExtra(getString(l), JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to warp to the rally.");
                                    setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/tppos " + l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch());
                                }
                            });
                            jcm.addExtra(new JSONChatExtra(" was set ", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>()));
                            jcm.addExtra(new JSONChatExtra(getConvertedTime(diff / 1000) + " ago.", JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§a" + getDate(h.timestamp));
                                }
                            });
                            jcm.sendToPlayer(((Player) sender));
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Team '" + id + "' could not be found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "/h " + getName() + " <identifier>");
                }

            }
        });

        registerSubcommand(new Subcommand("warps", new String[]{"go", "warps"}) {

            @Override
            protected void syncExecute() {

                if (args.length > 1) {
                    String id = args[1];
                    UUID uuid = FrozenUUIDCache.uuid(id);

                    if (uuid == null) {
                        sender.sendMessage(ChatColor.RED + "Could not find player '" + id + "'.");
                        return;
                    }
                    sender.sendMessage(ChatColor.YELLOW + "--- Old warps for §a" + FrozenUUIDCache.name(uuid) + "§e ---");
                    if (RavenPlugin.get().getWarpManager().getOldWarpList(uuid) != null) {
                        for (final TimestampedLocation h : RavenPlugin.get().getWarpManager().getOldWarpList(uuid)) {
                            long diff = System.currentTimeMillis() - h.timestamp;
                            final Location l = h.loc;
                            JSONChatMessage jcm = new JSONChatMessage("Warp '", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>());

                            jcm.addExtra(new JSONChatExtra(h.data, JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to warp to the warp.");
                                    setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/tppos " + l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch());
                                }
                            });
                            jcm.addExtra(new JSONChatExtra("' at ", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>()));

                            jcm.addExtra(new JSONChatExtra(getString(l), JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to warp to the warp.");
                                    setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/tppos " + l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch());
                                }
                            });
                            jcm.addExtra(new JSONChatExtra(" was deleted ", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>()));
                            jcm.addExtra(new JSONChatExtra(getConvertedTime(diff / 1000) + " ago.", JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§a" + getDate(h.timestamp));
                                }
                            });
                            jcm.sendToPlayer(((Player) sender));
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player '" + id + " has no warps.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "/h " + getName() + " <identifier>");
                }

            }

        });
        registerSubcommand(new Subcommand("home", new String[]{"h"}) {

            @Override
            protected void syncExecute() {
                if (args.length > 1) {
                    String id = args[1];
                    UUID uuid = FrozenUUIDCache.uuid(id);
                    if (uuid == null) {
                        sender.sendMessage(ChatColor.RED + "Could not find player '" + name + "'.");
                        return;
                    }
                    sender.sendMessage(ChatColor.YELLOW + "--- Old homes of §a" + FrozenUUIDCache.name(uuid) + "§e ---");
                    if (RavenPlugin.get().getHomeManager().getOldHomes(uuid) != null) {
                        for (final Home h : RavenPlugin.get().getHomeManager().getOldHomes(uuid)) {
                            long diff = System.currentTimeMillis() - h.getTimestamp();
                            final Location l = h.getHome();
                            JSONChatMessage jcm = new JSONChatMessage("Home at ", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>());
                            jcm.addExtra(new JSONChatExtra(getString(l), JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to warp to the home.");
                                    setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/tppos " + l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch());
                                }
                            });
                            jcm.addExtra(new JSONChatExtra(" was set ", JSONChatColor.YELLOW, new ArrayList<JSONChatFormat>()));
                            jcm.addExtra(new JSONChatExtra(getConvertedTime(diff / 1000) + " ago.", JSONChatColor.LIGHT_PURPLE, new ArrayList<JSONChatFormat>()) {
                                {
                                    setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§a" + getDate(h.getTimestamp()));
                                }
                            });
                            jcm.sendToPlayer(((Player) sender));
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "No homes were found.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "/h " + getName() + " <identifier>");
                }
            }
        });
        registerSubcommandsToTabCompletions();
        setPermissionLevel("raven.history", "§cYou are not allowed to do this.");
    }

    @Override
    public void syncExecute() {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/h home <player> - View a player's old homes.");
            sender.sendMessage(ChatColor.YELLOW + "/h warps <player> - View a player's deleted warps.");
            sender.sendMessage(ChatColor.YELLOW + "/h hq <team> - View the last HQs of a team.");
            sender.sendMessage(ChatColor.YELLOW + "/h rally <team> - View the last rallies of a team.");
        } else {
            sender.sendMessage(ChatColor.RED + "Unrecognized subcommand.");
        }
    }

    public String getDate(long ts) {
        Date d = new Date(ts);
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm z");
        return df.format(d);
    }

    public String getConvertedTime(long i) {
        i = Math.abs(i);
        int hours = (int) Math.floor(i / 3600);
        int remainder = (int) (i % 3600), minutes = remainder / 60, seconds = remainder % 60;
        String toReturn;
        if (seconds == 0 && minutes == 0)
            return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + "0s";
        if (minutes == 0) {
            if (seconds == 1)
                return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%ss", seconds);
            return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%ss", seconds);
        }
        if (seconds == 0) {
            if (minutes == 1)
                return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm", minutes);
            return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm", minutes);
        }
        if (seconds == 1) {
            if (minutes == 1)
                return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm%ss", minutes, seconds);
            return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm%ss", minutes, seconds);
        }
        if (minutes == 1) {
            return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm%ss", minutes, seconds);
        }
        toReturn = String.format("%sm%ss", minutes, seconds);
        return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + toReturn;
    }

    public String getString(Location loc) {
        return String.format("§d{%s, %s, %s}", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

}
