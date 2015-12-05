package net.frozenorb.Raven.CommandSystem.Commands;

import org.bukkit.ChatColor;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.WarpCommand;

public class Cancel extends BaseCommand {

	public Cancel() {
		super("cancel");
	}

	@Override
	public void syncExecute() {

		if (WarpCommand.confirms.containsKey(sender.getName())) {
			WarpCommand.confirms.remove(sender.getName());
			sender.sendMessage(ChatColor.GRAY + "You have not overrided your warp.");

		} else {
			sender.sendMessage(ChatColor.RED + "There is nothing to cancel!");
		}

	}

}
