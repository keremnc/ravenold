package net.frozenorb.Raven.CommandSystem;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Utilities.DataSystem.Regioning.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class WarpCommand implements CommandExecutor, TabExecutor {
	private RavenPlugin plugin;

	public static HashMap<String, Runnable> confirms = new HashMap<String, Runnable>();

	public WarpCommand(RavenPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] params) {
      if(sender instanceof  Player) {
		  LinkedList<String> args = new LinkedList<String>(Arrays.asList(params));
		  LinkedList<String> results = new LinkedList<String>();
		  String action = null;
		  if (args.size() >= 1) {
			  action = args.pop().toLowerCase();
		  } else {
			  return results;
		  }
		  if (args.size() == 0) {

			  for (String n : plugin.getWarpManager().getWarpList(((Player) sender).getUniqueId(), true)) {
				  if (n.toLowerCase().startsWith(action.toLowerCase())) {
					  results.add(n);
				  }
			  }
		  }
		  if (args.size() == 1) {
			  String ac = args.pop().toLowerCase();

			  for (String n : plugin.getWarpManager().getWarpList(((Player)sender).getUniqueId(), false)) {
				  if (n.toLowerCase().startsWith(ac)) {
					  results.add(n);
				  }
			  }
		  }
		  if (cmd.getLabel().equalsIgnoreCase("go") || cmd.getLabel().equalsIgnoreCase("warp"))
			  return results;
		  else
			  return null;
	  }
		return null;
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, final String[] args) {
		int length = args.length;
		final Player player = (Player) sender;

		if (player.getWorld().getEnvironment() == Environment.THE_END && player.getGameMode() != GameMode.CREATIVE) {
			player.sendMessage(ChatColor.RED + "You can only exit the End through the End Portal!");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("warp")) {
			if (length == 0)
				return plugin.getWarpManager().sendHelp(player, commandLabel.toLowerCase());

			if (args[0].equalsIgnoreCase("list")) {
				plugin.getWarpManager().printList(player);
				return true;
			}

			if (length == 1) {
				if (args[0].equalsIgnoreCase("set")) {
					sender.sendMessage(ChatColor.GRAY + "/" + commandLabel + " " + args[0] + " [warpName]");
					return true;
				}

				if ((args[0].equalsIgnoreCase("delete")) || (args[0].equalsIgnoreCase("remove")) || (args[0].equalsIgnoreCase("del"))) {
					sender.sendMessage(ChatColor.GRAY + "/" + commandLabel + " " + args[0] + " [warpName]");
					return true;
				}

				if (plugin.getServerManager().isSpawn(((Player) sender).getLocation())) {
					sender.sendMessage(ChatColor.RED + "You cannot warp in spawn!");
					return true;
				}

				if (plugin.getWarpManager().warpExists(player.getUniqueId(), args[0])) {
					if (RegionManager.get().hasTag(plugin.getWarpManager().getWarp(player.getUniqueId(), args[0]).getLocation(), "no-warp")) {
						sender.sendMessage(ChatColor.RED + "You are not able to warp to this area!");
						return true;
					}
				}
				if (plugin.getServerManager().canWarp(player)) {
					if (plugin.getWarpManager().warpExists(player.getUniqueId(), args[0])) {
						if (plugin.getWarpManager().getWarp(player.getUniqueId(), args[0]).getLocation().getWorld().getEnvironment() == Environment.THE_END) {
							sender.sendMessage(ChatColor.RED + "You cannot warp to warps in the end!");
							return true;
						}
						RavenPlugin.get().getServerManager().disablePlayerAttacking(player);
						return plugin.getWarpManager().warpPlayer(player.getUniqueId(), args[0]);
					} else
						sender.sendMessage(ChatColor.RED + "Warp '" + args[0] + "' doesn't exist!");
				} else {
					if (plugin.getWarpManager().warpExists(player.getUniqueId(), args[0])) {
						if (plugin.getWarpManager().getWarp(player.getUniqueId(), args[0]).getLocation().getWorld().getEnvironment() == Environment.THE_END) {
							sender.sendMessage(ChatColor.RED + "You cannot warp to warps in the end!");
							return true;
						}
						sender.sendMessage(ChatColor.GRAY + "Someone is nearby! Warping in 10 seconds. Don't move.");

						int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								if (RavenPlugin.tasks.containsKey(sender.getName())) {
									RavenPlugin.tasks.remove(sender.getName());
									Bukkit.getPlayer(player.getUniqueId()).teleport(plugin.getWarpManager().getWarp(player.getUniqueId(), args[0]).getLocation());
									RavenPlugin.get().getServerManager().disablePlayerAttacking(Bukkit.getPlayer(player.getUniqueId()));

								}
							}
						}, 200L);
						if (RavenPlugin.tasks.containsKey(sender.getName())) {
							Bukkit.getScheduler().cancelTask(RavenPlugin.tasks.remove(sender.getName()));
						}
						RavenPlugin.tasks.put(sender.getName(), taskid);
					} else
						sender.sendMessage(ChatColor.RED + "Warp '" + args[0] + "' doesn't exist!");
				}
				return true;
			}

			if (length == 2) {
				if ((args[0].equalsIgnoreCase("delete")) || (args[0].equalsIgnoreCase("remove")) || (args[0].equalsIgnoreCase("del"))) {
					if (true) {
						if (plugin.getWarpManager().warpExists(player.getUniqueId(), args[1])) {
							plugin.getWarpManager().deleteWarp(player.getUniqueId(), args[1]);

							player.sendMessage(ChatColor.GRAY + "Warp '" + args[1] + "' has been deleted.");
						} else {
							player.sendMessage(ChatColor.GRAY + "Warp '" + args[1] + "' doesn't exist!");
						}
						return true;
					}

				}

				if (args[0].equalsIgnoreCase("set")) {
					Location loc = ((Player) sender).getLocation();
					if (!(loc.getWorld().getEnvironment() == Environment.NETHER)) {
						if (Math.abs(loc.getX()) < 512 && Math.abs(loc.getZ()) < 512) {
							sender.sendMessage(ChatColor.RED + "You cannot set warps within 512 blocks of spawn.");
							return true;
						}
					}
					if (loc.getWorld().getEnvironment() == Environment.THE_END) {
						sender.sendMessage(ChatColor.RED + "You cannot set warps in the end!");
						return true;
					}

					if (args[1].contains(",") || args[1].contains(":") || args[1].contains("&")) {
						sender.sendMessage(ChatColor.GRAY + "You can't have commas(,) or colons(:) or ampersands(&) in your warp name!");
						return true;
					}
					if (!args[1].matches("^[a-zA-Z0-9\\+\\-]*$")) {
						sender.sendMessage(ChatColor.RED + "Invalid warp name!");
						return true;
					}

					if (plugin.getWarpManager().warpExists(player.getUniqueId(), args[1])) {

						final Location location = player.getLocation();
						final String arg = args[1];

						sender.sendMessage(ChatColor.YELLOW + "Are you sure you want to overwrite your '§d" + arg + "§e' warp?");
						sender.sendMessage(ChatColor.YELLOW + "Type §a'/confirm' §eto continue or §c'/cancel' §eto cancel.");

						confirms.put(sender.getName(), new Runnable() {

							@Override
							public void run() {

								plugin.getWarpManager().setWarp(player.getUniqueId(), args[1], location);

								sender.sendMessage(ChatColor.GRAY + "Warp '" + arg + "' has been overwritten!");

							}
						});

						return true;
					}
					if (plugin.getWarpManager().canSetWarp(player)) {
						plugin.getWarpManager().setWarp(player.getUniqueId(), args[1], player.getLocation());
						player.sendMessage(ChatColor.GRAY + "Warp '" + args[1] + "' has been set.");
						return true;
					}

					player.sendMessage(ChatColor.GRAY + "You have the maximum amount of warps you can own!");
					return true;
				}

				return plugin.getWarpManager().sendHelp(player, commandLabel.toLowerCase());
			}

			return plugin.getWarpManager().sendHelp(player, commandLabel.toLowerCase());
		}
		return true;
	}
}