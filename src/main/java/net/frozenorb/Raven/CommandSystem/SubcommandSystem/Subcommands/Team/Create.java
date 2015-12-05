package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.Events.TeamEvent;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Create extends Subcommand {

	public Create(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		Player p = (Player) sender;
		if (args.length > 1) {
			String pass = args.length > 2 ? args[2] : "INTEGER.MAX_VALUE";
			if (RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId()) == null) {
				if (args[1].contains(",") || args[1].contains(":") || args[1].contains(".")) {
					p.sendMessage(ChatColor.GRAY + "You can't have commas(,), colons(:), or periods(.), in your team name!");
					return;
				}

				if (args[1].toCharArray().length > 16) {
					p.sendMessage(ChatColor.RED + "Team names may not be longer than 16 characters");
					return;
				}

				if (!RavenPlugin.get().getTeamManager().teamExists(args[1])) {
					net.frozenorb.Raven.Team.Team team = new net.frozenorb.Raven.Team.Team(args[1]);
					team.addOwner(p.getUniqueId());//
					team.setPassword(pass);
					team.setFriendlyFire(false);
					team.setFriendlyName(args[1]);
					RavenPlugin.get().getTeamManager().addTeam(team);
					RavenPlugin.get().getTeamManager().setTeam(p.getUniqueId(), team);
					p.sendMessage(ChatColor.DARK_AQUA + "Team Created!");
					p.sendMessage(ChatColor.GRAY + "To learn more about teams, do /team");
                    Bukkit.getPluginManager().callEvent(new TeamEvent(p, team));
                } else {
					p.sendMessage(ChatColor.GRAY + "That team already exists!");
				}
			} else {
				p.sendMessage(ChatColor.GRAY + "You're already in a team!");
			}
			return;
		} else {
			sendErrorMessage();
		}
	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<>();
	}
}
