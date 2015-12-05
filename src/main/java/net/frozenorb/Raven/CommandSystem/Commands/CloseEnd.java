package net.frozenorb.Raven.CommandSystem.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.BaseCommand;

public class CloseEnd extends BaseCommand {

	public CloseEnd() {
		super("closeend");
		setPermissionLevel("op", "§cYou are not allowed to do this.");
	}

	@Override
	public void syncExecute() {

		for (Player p : Bukkit.getWorld("world_the_end").getPlayers()) {
			p.teleport(RavenPlugin.get().getServerManager().getSpawn(p.getWorld()));
			p.sendMessage(ChatColor.YELLOW + "You have been teleported to spawn.");
		}
	}
}
