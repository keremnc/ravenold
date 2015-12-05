package net.frozenorb.Raven.Tasks;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Managers.HomeManager.Home;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.Raven.Types.Warp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class SearcherThread extends Thread {
	private Location loc;
	private RavenPlugin plugin;
	private CommandSender searcher;
	private int radius;

	public SearcherThread(Location loc, CommandSender searcher, int radius) {
		this.loc = loc;
		this.radius = radius;
		this.searcher = searcher;
		this.plugin = (RavenPlugin) Bukkit.getPluginManager().getPlugin("Raven");
	}

	public boolean isSameWorld(Location loc, Location loc2) {
		return loc.getWorld().getName().equals(loc2.getWorld().getName());
	}

	public int distance(Location loc1, Location o) {
		if (o == null) {
			throw new IllegalArgumentException("Cannot measure distance to a null location");
		} else if (o.getWorld() == null || loc1.getWorld() == null) {
			throw new IllegalArgumentException("Cannot measure distance to a null world");
		} else if (o.getWorld() != loc1.getWorld()) {
			throw new IllegalArgumentException("Cannot measure distance between " + loc1.getWorld().getName() + " and " + o.getWorld().getName());
		}

		double ret = Math.pow(loc1.getX() - o.getX(), 2) + Math.pow(loc1.getZ() - o.getZ(), 2);
		return (int) Math.sqrt(ret);
	}

	public String getString(Location loc) {
		return String.format("§d{%s, %s, %s}", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	@Override
	public void run() {
		boolean done = false;
		int teamCounter = 0;
		int homeCounter = 0;
		int warpCounter = 0;
		for (Team team : plugin.getTeamManager().getTeams()) {
			teamCounter++;
			if (team.getHQ() != null && isSameWorld(team.getHQ().getParent(), loc))
				if (distance(team.getHQ().getParent(), loc) < radius) {
					done = true;

					searcher.sendMessage(ChatColor.RED + "Team§a " + team.getFriendlyName() + "§c has§a HQ§c near here. " + getString(team.getHQ().getParent()));
				}
			if (team.getRally() != null && isSameWorld(team.getRally().getParent(), loc))
				if (distance(team.getRally().getParent(), loc) < radius) {
					done = true;

					searcher.sendMessage(ChatColor.RED + "Team§a " + team.getFriendlyName() + "§c has§a RALLY§c near here. " + getString(team.getRally().getParent()));
				}
		}
		for (Home home : plugin.getHomeManager().getHomes()) {
			homeCounter++;
			if (isSameWorld(home.getHome(), loc) && distance(home.getHome(), loc) < radius) {
				done = true;

				searcher.sendMessage(ChatColor.RED + "Player§a " + home.getPlayer() + "§c has§a HOME§c near here. " + getString(home.getHome()));
			}
		}
		for (Warp warp : plugin.getWarpManager().getWarps()) {
			warpCounter++;
			if (isSameWorld(warp.getLocation(), loc) && distance(warp.getLocation(), loc) < radius) {
				done = true;
				searcher.sendMessage(ChatColor.RED + "Player§a " + warp.getPlayerName() + "§c has§a WARP{" + warp.getName() + "}§c near here. " + getString(warp.getLocation()));
			}

		}
		if (!done)
			searcher.sendMessage(ChatColor.RED + "No results found.");
		searcher.sendMessage(ChatColor.AQUA + "Scanned " + teamCounter + " teams, " + warpCounter + " warps, and " + homeCounter + " homes, radius: " + radius + ".");

		interrupt();

	}
}
