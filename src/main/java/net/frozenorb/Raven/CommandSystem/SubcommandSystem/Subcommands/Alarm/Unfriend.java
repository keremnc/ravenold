package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Unfriend extends Subcommand {

	public Unfriend(String name, String... aliases) {
		super(name, null, aliases);
	}

	@Override
	protected void syncExecute() {
		if (args.length > 1) {
			if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()) != null) {
				UUID id = FrozenUUIDCache.uuid(args[1]);
				if (id == null) {
					sender.sendMessage(ChatColor.RED + "Could not find player '" + args[1] + "'.");
					return;
				}
				if (!RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).isFriend(id)) {
					sender.sendMessage(ChatColor.RED + "Player " + args[1] + " is not your friend!");
					return;
				}
				RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).removeFriend(id);
				sender.sendMessage(ChatColor.RED + "" + args[1] + " is no longer exempt from your alarm!");
				if (args[0].equals("Unfriend")) {
					Bukkit.dispatchCommand(sender, "a");
				}
			} else
				sender.sendMessage(ChatColor.RED + "You have no alarm set! ");
		} else
			sender.sendMessage(ChatColor.RED + "/alarm unfriend <name>");
	}

	@Override
	public List<String> tabComplete() {
		if (RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()) != null) {
			UUID[] friends = RavenPlugin.get().getAlarmManager().getAlarm(((Player) sender).getUniqueId()).getFriends();
			List<String> names = new ArrayList<>();
			for (UUID id : friends) {
				names.add(FrozenUUIDCache.name(id));
			}
			return names;
		}
		return new ArrayList<String>();
	}
}
