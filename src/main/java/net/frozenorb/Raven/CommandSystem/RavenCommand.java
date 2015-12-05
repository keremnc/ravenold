package net.frozenorb.Raven.CommandSystem;

import net.frozenorb.Raven.RavenPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class RavenCommand implements CommandExecutor {
	private RavenPlugin plugin;

	public RavenPlugin getPlugin() {
		return plugin;
	}

	public RavenCommand(RavenPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return false;
	}
}
