package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.Listeners.GeneralListener;

import org.bukkit.ChatColor;

public class ToggleCap extends BaseCommand {

	public ToggleCap() {
		super("togglecap", new String[] {});
		setPermissionLevel("raven.togglecap.", "§cYou are not allowed to do this!");
	}

	@Override
	public void syncExecute() {
		GeneralListener.CAP_ENABLED = !GeneralListener.CAP_ENABLED;
		sender.sendMessage(ChatColor.YELLOW + "Cap enabled: " + GeneralListener.CAP_ENABLED + ".");
	}
}
