package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Delete extends Subcommand {

	public Delete(String name, String... aliases) {
		super(name, null, aliases);
	}

	@Override
	protected void syncExecute() {
		if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()) != null) {
			RavenPlugin.get().getAlarmManager().removeAlarm(((Player) sender).getUniqueId());
			sender.sendMessage(ChatColor.GREEN + "Your alarm has been successfully removed!");
		} else
			sender.sendMessage(ChatColor.RED + "You have no alarm set! ");
	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}
}
