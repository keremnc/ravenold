package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Chat extends Subcommand {

	public Chat(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
	}

	@Override
	public void syncExecute() {
		final Player p = (Player) sender;

		if (RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId()) == null) {
			boolean first = true;
			StringBuilder sb = new StringBuilder();
			for (String a : args) {
				if (!first)
					sb.append(a + " ");
				first = false;
			}
			p.chat("/t create " + sb.toString());
			return;
		}
		if (RavenPlugin.get().getServerManager().getTeamChatMap().get(p.getName()) == "tchat") {
			RavenPlugin.get().getServerManager().getTeamChatMap().put(p.getName(), null);
			p.sendMessage(ChatColor.DARK_AQUA + "You are now in public chat.");
		} else {
			if (RavenPlugin.get().getServerManager().getTeamChatMap().get(p.getName()) == null) {
				RavenPlugin.get().getServerManager().getTeamChatMap().put(p.getName(), "tchat");
				p.sendMessage(ChatColor.DARK_AQUA + "You are now in team chat only mode.");
			} else {
				RavenPlugin.get().getServerManager().getTeamChatMap().put(p.getName(), "tchat");
				p.sendMessage(ChatColor.DARK_AQUA + "You are now in team chat only mode.");
			}
		}

	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}

}
