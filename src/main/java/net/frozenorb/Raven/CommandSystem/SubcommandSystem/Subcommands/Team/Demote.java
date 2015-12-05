package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Demote extends Subcommand {

	public Demote(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		final Player p = (Player) sender;

		if (args.length > 1) {

			String name = args[1];

			UUID id = FrozenUUIDCache.uuid(name);

			if (id == null) {
				p.sendMessage(ChatColor.RED + "Could not find player '" + name + "'.");
				return;
			}

			if (Bukkit.getPlayer(id) != null) {
			    name = FrozenUUIDCache.name(id);
			}
			net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());

			if (team.isOwner(p.getUniqueId())) {
				if (id != null && team.isOwner(id)) {
					team.setMember(id);
					p.sendMessage(ChatColor.DARK_AQUA + sender.getName() + " demoted " + team.getActualPlayerName(name) + " to member.");
				} else {
					p.sendMessage(ChatColor.RED + "That player is already a member on your team!");
				}
			} else
				sender.sendMessage(ChatColor.DARK_AQUA + "Only team managers can do this.");

		} else {
			sendErrorMessage();
		}

	}

	@Override
	public List<String> tabComplete() {
		net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(((Player) sender).getUniqueId());
		if (team == null) {
			return super.tabComplete();
		}
		// you can only really demote owners.

		List<String> options = new ArrayList<>();

		for (UUID id : team.getOwners()) {
			options.add(FrozenUUIDCache.name(id));
		}
		return options;
	}
}
