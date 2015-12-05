package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm;

import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.ChatColor;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Friend extends Subcommand {

	public Friend(String name, String... aliases) {
		super(name, null, aliases);
	}

	@Override
	protected void syncExecute() {
		if (args.length > 1) {
			if (!args[1].matches("^[a-zA-Z0-9_]*$") || args[1].contains(",") || args[1].contains(":")) {
				sender.sendMessage(ChatColor.RED + "That's an odd name!");
				return;
			}
			if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()) != null) {
				UUID id = FrozenUUIDCache.uuid(args[1]);
				if (id == null) {
					sender.sendMessage(ChatColor.RED + "Could not find player '" + args[1] + "'.");
					return;
				}
				if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).isFriend(id)) {
					sender.sendMessage(ChatColor.RED + "Player " + args[1] + " is already your friend!");
					return;
				}
				RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).addFriend(id);
				sender.sendMessage(ChatColor.GREEN + "" + args[1] + " is now exempt from your alarm!");
			} else
				sender.sendMessage(ChatColor.RED + "You have no alarm set! ");
		} else
			sender.sendMessage(ChatColor.RED + "/alarm friend <name>");

	}

}
