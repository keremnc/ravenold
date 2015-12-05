package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;

public class Pass extends Subcommand {

	public Pass(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		final Player p = (Player) sender;
		if (args.length > 1) {
			net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());
			if (team == null)
				p.sendMessage(ChatColor.GRAY + "You are not on a team!");
			else {
				if (args[1].contains(",")) {
					p.sendMessage(ChatColor.GRAY + "You can't have commas in your teamname!");
					return;
				}

				if (team.isOwner(p.getUniqueId())) {
					team.setPassword(args[1]);

					p.sendMessage(ChatColor.GRAY + "Team password changed to " + args[1]);

					for (Player pl : Bukkit.getOnlinePlayers()) {
						if (team.isOnTeam(pl) && pl != p) {
							pl.sendMessage(ChatColor.DARK_AQUA + sender.getName() + " changed the team password.");
						}
					}
				} else {
					sender.sendMessage(ChatColor.DARK_AQUA + "Only team managers can do this.");
					return;
				}
			}

		} else
			sendErrorMessage();
	}

}
