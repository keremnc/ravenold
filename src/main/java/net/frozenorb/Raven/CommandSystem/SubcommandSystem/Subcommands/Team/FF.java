package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class FF extends Subcommand {

	public FF(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		final Player p = (Player) sender;

		if (args.length > 1) {

			net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());

			if (team == null) {
				sender.sendMessage(ChatColor.GRAY + "You are not on a team!");
				return;
			}
			if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true")) {
				if (team.isOwner(p.getUniqueId())) {
					team.setFriendlyFire(true);
					p.sendMessage(ChatColor.DARK_AQUA + "You have turned team friendly fire on.");

					for (Player pl : Bukkit.getOnlinePlayers()) {
						if (team.isOnTeam(pl) && pl != p) {
							pl.sendMessage(ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + "Friendly fire was turned on by " + sender.getName() + ".");
						}
					}
				} else {
					p.sendMessage(ChatColor.DARK_AQUA + "Only team managers can change this.");
				}
			} else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false")) {
				if (team.isOwner(p.getUniqueId())) {
					team.setFriendlyFire(false);
					p.sendMessage(ChatColor.DARK_AQUA + "You have turned team friendly fire off.");
					for (Player pl : Bukkit.getOnlinePlayers()) {
						if (team.isOnTeam(pl) && pl != p) {
							pl.sendMessage(ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + "Friendly fire was turned off by " + sender.getName() + ".");
						}
					}

				} else {
					p.sendMessage(ChatColor.DARK_AQUA + "Only team managers can change this.");
				}
			} else
				sender.sendMessage(ChatColor.DARK_AQUA + "/team ff on|off");

		} else {
			sendErrorMessage();
		}

	}

	@Override
	public List<String> tabComplete() {
		return Arrays.asList("on", "off", "true", "false");
	}

}
