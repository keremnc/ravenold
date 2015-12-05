package net.frozenorb.Raven.CommandSystem.Commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.BaseCommand;

public class ClearDeathBans extends BaseCommand {

	public ClearDeathBans() {
		super("cleardeathbans", new String[] {});
		setPermissionLevel("op", "§cYou're not allowed to do this.");
	}

	@Override
	public void syncExecute() {
		if (args.length == 1) {

			String name = args[0];
			if (name.equals("all")) {

				int size = RavenPlugin.get().getServerManager().getDeathbans().size();
				RavenPlugin.get().getServerManager().getDeathbans().clear();
				sender.sendMessage(ChatColor.RED + "Cleared " + size + " deathbans!");
			} else {
				sender.sendMessage(ChatColor.YELLOW + name + " removed: " + RavenPlugin.get().getServerManager().getDeathbans().remove(name));
			}
		} else {
			sender.sendMessage(ChatColor.RED + "/cleardeathbans all|<name>");
		}
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
			for (String entry : RavenPlugin.get().getServerManager().getDeathbans().keySet()) {
				if (entry.toLowerCase().startsWith(action.toLowerCase())) {
					results.add(entry);
				}
			}
			if ("all".startsWith(action.toLowerCase())) {
				results.add("all");
			}
		}
		return results;

	}

}
