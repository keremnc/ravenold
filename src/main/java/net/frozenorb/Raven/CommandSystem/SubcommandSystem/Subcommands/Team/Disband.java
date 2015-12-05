package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.Managers.TeamManager;
import net.frozenorb.Raven.Team.Team;

public class Disband extends Subcommand {

	public Disband(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
		setCompleteSecondHalf(true);
	}

	@Override
	public void syncExecute() {
		Player p = (Player) sender;
		if (!p.isOp()) {
			p.sendMessage(ChatColor.RED + "You aren't allowed to disband teams.");
			return;
		}
		TeamManager teamManager = RavenPlugin.get().getTeamManager();
		if (args.length > 1) {
			if (teamManager.teamExists(args[1])) {
				Team team = teamManager.getTeam(args[1]);
				RavenPlugin.get().getTeamManager().removeTeam(team.getName());
				p.sendMessage(ChatColor.DARK_AQUA + "Team §7'" + args[1] + "'§3 has been removed.");
			} else {
				ArrayList<String> teamNames = new ArrayList<String>();
				for (Team tem : teamManager.getTeams())
					teamNames.add(tem.getFriendlyName());
				p.sendMessage(ChatColor.RED + "Team '" + args[1] + "' could not be found.");
			}
		} else
			sender.sendMessage(ChatColor.RED + "/t disband <tName>");
	}

	@Override
	public List<String> tabComplete() {
		ArrayList<String> teamNames = new ArrayList<String>();
		for (Team tem : RavenPlugin.get().getTeamManager().getTeams())
			teamNames.add(tem.getFriendlyName());
		return teamNames;
	}
}
