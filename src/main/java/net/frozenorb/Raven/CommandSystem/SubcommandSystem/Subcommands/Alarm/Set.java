package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Set extends Subcommand {

	public Set(String name, String... aliases) {
		super(name, null, aliases);
	}

	@Override
	protected void syncExecute() {
		Location losc = ((Player) sender).getLocation();
		if (!(losc.getWorld().getEnvironment() == Environment.NETHER)) {
			if (Math.abs(losc.getX()) < 512 && Math.abs(losc.getZ()) < 512) {
				sender.sendMessage(ChatColor.RED + "You cannot set alarms within 512 blocks of spawn.");
				return;
			}
		}
		if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()) != null) {
			RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).setLocation(((Player) sender).getLocation());
		} else {
			net.frozenorb.Raven.Types.Alarm a = new net.frozenorb.Raven.Types.Alarm(((Player) sender).getUniqueId(), true, ((Player) sender).getLocation());
			RavenPlugin.get().getAlarmManager().setAlarm(((Player) sender).getUniqueId(), a);
		}
		org.bukkit.Location l = ((Player) sender).getLocation();
		String loc = l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ();
		sender.sendMessage(ChatColor.GRAY + "Your alarm has been set" + ChatColor.GRAY + " to §f(" + loc + ") " + ChatColor.GRAY + "in the §f" + (l.getWorld().getName().equalsIgnoreCase("world") ? "Overworld" : "Nether") + "§7!");

	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}
}
