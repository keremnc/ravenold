package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.Tasks.SearcherThread;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CheckNear extends BaseCommand {
	public CheckNear() {
		super("checknear", new String[] {});
		setPermissionLevel("raven.checknear", "§cYou are not allowed to do this.");
	}

	public boolean isInt(String str) {

		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	@Override
	public void syncExecute() {
		if (args.length > 0) {
			if (isInt(args[0])) {
				new SearcherThread(((Player) sender).getLocation(), sender, Integer.parseInt(args[0])).start();
			} else {
				sender.sendMessage(ChatColor.RED + "Radius not accurate.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "/checknear <radius>");
		}
	}
}
