package net.frozenorb.Raven.CommandSystem.Commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.Team.Team;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

public class TeamTag extends BaseCommand {

	public TeamTag() {
		super("teamtag", new String[] { "tagteam", "ttag" });
		setPermissionLevel("raven.teamtag.", "§cYou are not allowed to do this!");
	}

	@Override
	public List<String> tabComplete() {
		LinkedList<String> params = new LinkedList<String>(Arrays.asList(args));
		LinkedList<String> results = new LinkedList<String>();
		String action = null;
		if (params.size() >= 1) {
			action = params.pop().toLowerCase();
		} else {
			return results;
		}
		if (params.size() == 0) {
			for (net.frozenorb.Raven.Team.Team t : RavenPlugin.get().getTeamManager().getTeams()) {
				if (t.getName().toLowerCase().startsWith(action.toLowerCase())) {
					results.add(t.getName());
				}
			}
		}
		return results;

	}

	@Override
	public void syncExecute() {
		if (args.length >= 1) {
			String team = args[0];
			if (RavenPlugin.get().getTeamManager().teamExists(team)) {
				if (args.length == 2) {
					String tag = args[1];
					if (tag.length() > 4) {
						sender.sendMessage(ChatColor.RED + "That tag is too long.");
						return;
					}
					for (Team t : RavenPlugin.get().getTeamManager().getTeams()) {
						if (t.getTag() != null) {
							if (t.getTag().equalsIgnoreCase(args[1])) {
								sender.sendMessage(ChatColor.RED + t.getFriendlyName() + " already has that tag!");
								return;
							}
						}
					}
					if (StringUtils.isAlphanumeric(tag)) {
						RavenPlugin.get().getTeamManager().getTeam(team).setTag(tag);
						sender.sendMessage(ChatColor.GREEN + "Tag of " + team + " set to " + tag + ".");
					} else {
						sender.sendMessage(ChatColor.RED + "Tags can only be alphanumeric.");
					}
				} else {
					RavenPlugin.get().getTeamManager().getTeam(team).setTag(null);
					sender.sendMessage(ChatColor.GREEN + "Tag of " + team + " removed.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "That team does not exist.");
			}
		} else {
			sender.sendMessage("§c/ttag <team> <tag>");
		}
	}
}
