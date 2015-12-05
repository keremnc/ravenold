package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import java.util.ArrayList;
import java.util.List;

import net.frozenorb.Raven.Events.TeamEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.Team.Team;

public class Join extends Subcommand {

	public Join(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		Player p = (Player) sender;
		if (args.length > 1) {
			String passw = args.length == 3 ? args[2] : "INTEGER.MAX_VALUE";
			if (RavenPlugin.get().getTeamManager().getTeam(args[1]) == null) {
				p.sendMessage(ChatColor.RED + "That team does not exist!");
			} else if (RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId()) == null) {
				net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getTeam(args[1]);

				if (passw.equals(team.getPassword())) {
                    // cap team count at 20.
                    if (team.getMemberAmount() >= 20) {
                        p.sendMessage(ChatColor.RED + "This team is full.");
                        return;
                    }

					for (Player pl : Bukkit.getOnlinePlayers()) {
						if (team.getMembers().contains(pl.getUniqueId())) {
							pl.sendMessage(ChatColor.DARK_AQUA + p.getName() + " has joined your team.");
						}
					}
					team.addMember(p.getUniqueId());
					RavenPlugin.get().getTeamManager().setTeam(p.getUniqueId(), team);

					p.sendMessage(ChatColor.DARK_AQUA + "Successfully joined team " + args[1] + "!");
                    Bukkit.getPluginManager().callEvent(new TeamEvent(p, team));
				} else {
					p.sendMessage(ChatColor.DARK_AQUA + "Incorrect password.");
				}
			} else {
				p.sendMessage(ChatColor.DARK_AQUA + "Please leave your current team!");
			}
		} else {
			sendErrorMessage();
		}

	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}

}
