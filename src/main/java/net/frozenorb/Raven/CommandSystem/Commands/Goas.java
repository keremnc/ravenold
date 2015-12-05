package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Types.Warp;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Goas extends BaseCommand {

	public Goas() {
		super("goas", new String[] {});
		setPermissionLevel("raven.teamas", "§cYou are not allowed to do this.");
	}

	@Override
	public void syncExecute() {
		RavenPlugin plugin = RavenPlugin.get();
		if (args.length == 2) {
			String playerName = args[0];
			String warpName = args[1];
			UUID id = FrozenUUIDCache.uuid(playerName);
			if (id == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player '" + playerName + "'.");
				return;
			}

			if (plugin.getWarpManager().warpExists(id, warpName)) {
				((Player) sender).teleport(plugin.getWarpManager().getWarp(id, warpName).getLocation());

			} else {
				sender.sendMessage(ChatColor.RED + "Warp does not exist.");
				sender.sendMessage(ChatColor.GRAY + FrozenUUIDCache.name(id) + "'s warps: " + plugin.getWarpManager().getList(id));
			}
		} else if (args.length == 1) {
			UUID id = FrozenUUIDCache.uuid(args[0]);
			if (id == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player '" + args[0] + "'.");
				return;
			}
			sender.sendMessage(ChatColor.GRAY + FrozenUUIDCache.name(id) + "'s warps: " + plugin.getWarpManager().getList(id));

		} else {
			sender.sendMessage(ChatColor.RED + "/goas <playerName> [warpName]");
		}
	}

	@Override
	public List<String> tabComplete() {
		if (sender.hasPermission("raven.teamas")) {
			LinkedList<String> params = new LinkedList<String>(Arrays.asList(args));
			LinkedList<String> results = new LinkedList<String>();
			String action = null;
			if (params.size() >= 1) {
				action = params.pop().toLowerCase();
			} else {
				return results;
			}

			if (params.size() == 0) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.getName().toLowerCase().startsWith(action.toLowerCase())) {
						results.add(p.getName());
					}
				}
			} else if (params.size() == 1) {
				UUID id = FrozenUUIDCache.uuid(action);
				if (id == null) {
					// sender.sendMessage(ChatColor.RED + "Could not find player '" + action + "'.");
					return super.tabComplete();
				}
				if (RavenPlugin.get().getWarpManager().getWarpList(id) != null) {
					for (Warp w : RavenPlugin.get().getWarpManager().getWarpList(id)) {
						if (w.getName().toLowerCase().startsWith(params.getFirst().toLowerCase())) {
							results.add(w.getName());
						}
					}
				}
			} else {
				for (Player p : Bukkit.getOnlinePlayers()) {
					results.add(p.getName());
				}
			}
			return results;
		} else {
			return super.tabComplete();
		}
	}
}
