package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Location extends Subcommand {

	public Location(String name, String... aliases) {
		super(name, null, aliases);
	}

	@Override
	protected void syncExecute() {
		if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()) != null) {
			org.bukkit.Location l = RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).getLocation();
			String loc = l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ();
			sender.sendMessage(ChatColor.GRAY + "Your alarm is §aset" + ChatColor.GRAY + " to §f(" + loc + ") " + ChatColor.GRAY + "in the §f" + (l.getWorld().getName().equalsIgnoreCase("world") ? "Overworld" : "Nether") + "§7!");

		} else
			sender.sendMessage(ChatColor.RED + "You have no alarm set! ");
	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}
}
