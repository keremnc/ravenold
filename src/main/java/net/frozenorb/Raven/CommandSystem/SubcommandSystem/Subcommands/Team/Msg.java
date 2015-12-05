package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Msg extends Subcommand {

	public Msg(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		Player p = (Player) sender;

		net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());

		if (args.length > 1) {
			if (team != null) {
				LinkedList<String> params = new LinkedList<String>(Arrays.asList(args));
				params.removeFirst();
				String msg = StringUtils.join(params, " ");
				for (UUID name : team.getMembers()) {
					Player pl = Bukkit.getPlayer(name);
					if (pl != null) {
						if (team.isOwner(p.getUniqueId()))
							pl.sendMessage(ChatColor.WHITE + "<" + ChatColor.DARK_AQUA + p.getName() + ChatColor.WHITE + ">" + ChatColor.GRAY + "[" + ChatColor.GRAY + team.getFriendlyName() + "]" + ChatColor.WHITE + " " + msg);
						else
							pl.sendMessage(ChatColor.WHITE + "<" + ChatColor.GRAY + p.getName() + ChatColor.WHITE + ">" + ChatColor.GRAY + "[" + ChatColor.GRAY + team.getFriendlyName() + "]" + ChatColor.WHITE + " " + msg);

					}
				}
			} else
				sender.sendMessage(ChatColor.GRAY + "You are not on a team!");
		} else
			sender.sendMessage(ChatColor.RED + "/t msg <message>");
	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}
}
