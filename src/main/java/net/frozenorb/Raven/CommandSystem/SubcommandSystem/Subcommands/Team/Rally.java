package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Rally extends Subcommand {

	public Rally(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		final Player p = (Player) sender;

		if (RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId()) == null) {
			p.sendMessage(ChatColor.DARK_AQUA + "You are not on a team!");
			return;
		}

		final net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());

		if (team.getRally() == null) {
			sender.sendMessage(ChatColor.RED + "Rally point not set.");
			return;
		}

		if (RavenPlugin.get().getServerManager().isSpawn(((Player) sender).getLocation())) {
			sender.sendMessage(ChatColor.RED + "You cannot warp in spawn!");
			return;
		}
		if (p.getWorld().getEnvironment() == Environment.THE_END) {
			p.sendMessage(ChatColor.RED + "You can only exit the End through the End Portal!");
			return;
		}


		if (RavenPlugin.get().getServerManager().canWarp(p)) {
			p.teleport(team.getRally().getParent());
			RavenPlugin.get().getServerManager().disablePlayerAttacking(p);

		} else {
			final String name = sender.getName();
			sender.sendMessage(ChatColor.GRAY + "Someone is nearby! Warping in 10 seconds. Don't move.");
			Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Raven"), new Runnable() {

				@Override
				public void run() {
					if (RavenPlugin.get().getServerManager().getWarpmove().contains(name)) {
						RavenPlugin.get().getServerManager().getWarpmove().remove(name);
						p.teleport(team.getRally().getParent());
						RavenPlugin.get().getServerManager().disablePlayerAttacking(p);

					}
				}
			}, 200L);
			RavenPlugin.get().getServerManager().getWarpmove().remove(name);
			RavenPlugin.get().getServerManager().getWarpmove().add(name);
		}

	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}

}
