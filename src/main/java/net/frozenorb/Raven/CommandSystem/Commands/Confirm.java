package net.frozenorb.Raven.CommandSystem.Commands;

import org.bukkit.ChatColor;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.WarpCommand;

public class Confirm extends BaseCommand {

	public Confirm() {
		super("confirm", "confrim");
	}

	@Override
	public void syncExecute() {
		if (WarpCommand.confirms.containsKey(sender.getName())) {
			WarpCommand.confirms.remove(sender.getName()).run();
		} else {
			sender.sendMessage(ChatColor.RED + "There is nothing to confirm!");
		}
	}

}
