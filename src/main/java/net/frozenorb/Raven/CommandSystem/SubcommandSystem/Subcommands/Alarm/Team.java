package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Team extends Subcommand {

	public Team(String name, String... aliases) {
		super(name, null, aliases);
	}

	@Override
	protected void syncExecute() {

		if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()) != null) {
			boolean setTo = !RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).isTeamFriendly();
			if (args.length > 1) {
				if (args[1].equalsIgnoreCase("off"))
					setTo = false;
				if (args[1].equalsIgnoreCase("on"))
					setTo = true;
			}
			RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).setTeamFriendly(setTo);
			if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).isTeamFriendly()) {
				sender.sendMessage(ChatColor.GREEN + "Your team will no longer set off your alarm!");
			} else
				sender.sendMessage(ChatColor.RED + "Your team will now set off your alarm!");

		} else
			sender.sendMessage(ChatColor.RED + "You have no alarm set! ");

	}

	@Override
	public List<String> tabComplete() {
		if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()) != null) {
			return Arrays.asList("on", "off");
		}
		return new ArrayList<String>();
	}
}
