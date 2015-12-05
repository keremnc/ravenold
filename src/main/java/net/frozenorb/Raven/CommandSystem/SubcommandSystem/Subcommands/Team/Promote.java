package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;

public class Promote extends Subcommand {

	public Promote(String name, String errorMessage, String... aliases) {
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
				name = Bukkit.getPlayer(id).getName();
			}
			net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());

			if (team == null) {
				p.sendMessage(ChatColor.GRAY + "You are not on a team!");
			} else {
				if (team.isOwner(p.getUniqueId()) || sender.isOp()) {
					if (team.isOwner(id)) {
						p.sendMessage(ChatColor.RED + "That person is already a manager.");
					} else if (team.isOnTeam(id)) {
						team.addOwner(id);

						for (Player pl : Bukkit.getOnlinePlayers()) {
							if (team.isOnTeam(pl))
								pl.sendMessage(ChatColor.DARK_AQUA + "" + team.getActualPlayerName(name) + " was promoted by " + p.getName() + ".");
						}
					} else
						sender.sendMessage(ChatColor.DARK_AQUA + "Player is not on your team.");
				} else
					sender.sendMessage(ChatColor.DARK_AQUA + "Only team managers can do this.");
			}
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
		List<String> options = new ArrayList<>();

		for (UUID id : team.getMembers()) {
			options.add(FrozenUUIDCache.name(id));
		}
		return options;
	}

}
