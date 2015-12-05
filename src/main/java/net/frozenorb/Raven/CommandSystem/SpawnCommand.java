package net.frozenorb.Raven.CommandSystem;

import net.frozenorb.Raven.RavenPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
	private RavenPlugin plugin;

	public static Location endSpawn;
	public static Location portalLoc;

	public SpawnCommand(RavenPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, final String[] args) {
		final Player player = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("spawn")) {
			if (args.length == 1 && sender.isOp()) {
				Location loc = ((Player) sender).getLocation();

				if (loc.getWorld().getEnvironment() == Environment.THE_END) {
					if (args[0].equalsIgnoreCase("portal")) {
						portalLoc = loc;
					} else {
						endSpawn = loc;
						loc.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					}
					return true;
				}
				loc.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
				return true;
			}

			if (player.getWorld().getEnvironment() == Environment.THE_END && player.getGameMode() != GameMode.CREATIVE) {
				player.sendMessage(ChatColor.RED + "You can only exit the End through the End Portal!");
				return true;
			}

			if (plugin.getServerManager().canWarp(player)) {
				if (!(RavenPlugin.spawnprot.contains(sender.getName())))
					RavenPlugin.spawnprot.add(sender.getName());

				Location spawn = plugin.getServerManager().getSpawn(player.getWorld());
				player.teleport(spawn);
				RavenPlugin.get().getBossBarManager().unregisterPlayer((Player) sender);

			} else {
				sender.sendMessage(ChatColor.GRAY + "Someone is nearby! Warping in 10 seconds. Don't move.");
				int taskid = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						if (RavenPlugin.tasks.containsKey(sender.getName())) {
							RavenPlugin.tasks.remove(sender.getName());
							player.teleport(plugin.getServerManager().getSpawn(player.getWorld()));
							RavenPlugin.get().getBossBarManager().unregisterPlayer((Player) sender);

							if (!(RavenPlugin.spawnprot.contains(sender.getName())))
								RavenPlugin.spawnprot.add(sender.getName());
						}
					}
				}, 200L);
				if (RavenPlugin.tasks.containsKey(sender.getName())) {
					Bukkit.getScheduler().cancelTask(RavenPlugin.tasks.remove(sender.getName()));
				}
				RavenPlugin.tasks.put(sender.getName(), taskid);
			}

			return true;
		}

		return plugin.getWarpManager().sendHelp(player, commandLabel.toLowerCase());
	}

}