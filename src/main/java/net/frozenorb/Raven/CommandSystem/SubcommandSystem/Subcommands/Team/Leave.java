package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import java.util.ArrayList;
import java.util.List;

import net.frozenorb.Raven.Events.TeamEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;

public class Leave extends Subcommand {

	public Leave(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		final Player p = (Player) sender;
		net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());

		if (team == null) {
			p.sendMessage(ChatColor.GRAY + "You are not on a team!");
		} else {
			RavenPlugin.get().getServerManager().getTeamChatMap().put(p.getName(), null);

			if (team.remove(p.getUniqueId())) {
				RavenPlugin.get().getTeamManager().removePlayerFromTeam(p.getUniqueId());
				RavenPlugin.get().getTeamManager().removeTeam(team.getName());
				p.sendMessage(ChatColor.DARK_AQUA + "Successfully left and disbanded team!");
                Bukkit.getPluginManager().callEvent(new TeamEvent(p, team));
			} else {
				RavenPlugin.get().getTeamManager().removePlayerFromTeam(p.getUniqueId());
				team.setChanged(true);
				for (Player pl : Bukkit.getOnlinePlayers()) {
					if (team.isOnTeam(pl)) {
						pl.sendMessage(ChatColor.DARK_AQUA + p.getName() + " has left the team.");
					}
				}
				p.sendMessage(ChatColor.DARK_AQUA + "Successfully left the team!");
                Bukkit.getPluginManager().callEvent(new TeamEvent(p, team));
			}
		}

	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}
}
