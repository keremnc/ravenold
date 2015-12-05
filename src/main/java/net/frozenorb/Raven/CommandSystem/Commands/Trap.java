package net.frozenorb.Raven.CommandSystem.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Utilities.Message.JSONChatClickEventType;
import net.frozenorb.Utilities.Message.JSONChatColor;
import net.frozenorb.Utilities.Message.JSONChatExtra;
import net.frozenorb.Utilities.Message.JSONChatFormat;
import net.frozenorb.Utilities.Message.JSONChatHoverEventType;
import net.frozenorb.Utilities.Message.JSONChatMessage;

public class Trap extends BaseCommand {

	public Trap() {
		super("trap");
		setPermissionLevel("raven.trap", "§cYou are not allowed to do this!");

		registerSubcommand(new Subcommand("list") {

			@Override
			protected void syncExecute() {

				JSONChatMessage msg = new JSONChatMessage("", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>());
				msg.addExtra(new JSONChatExtra("***Trap List (" + RavenPlugin.get().getTrapManager().getTraps().size() + ")***", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()));
				JSONChatMessage list = new JSONChatMessage("[", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>());
				boolean first = true;
				for (final net.frozenorb.Raven.Traps.Trap tr : RavenPlugin.get().getTrapManager().getTraps()) {
					if (!first)
						list.addExtra(new JSONChatExtra(", ", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()));
					list.addExtra(new JSONChatExtra(tr.getName(), JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()) {
						{
							setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to teleport to §f'" + tr.getName() + "'§a.");
							setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/trap " + tr.getName());
						}
					});
					first = false;
				}
				list.addExtra(new JSONChatExtra("]", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()));
				msg.sendToPlayer((Player) sender);
				list.sendToPlayer((Player) sender);

			}
		});

		registerSubcommand(new Subcommand("set") {

			@Override
			protected void syncExecute() {
				if (!(args.length > 1)) {
					sender.sendMessage(ChatColor.RED + "/trap set <name>");
					return;
				}
				Location l = ((Player) sender).getLocation();

				RavenPlugin.get().getTrapManager().setTrap(args[1], new net.frozenorb.Raven.Traps.Trap(args[1], l));

				sender.sendMessage(ChatColor.GRAY + "You have created a trap here with the name '" + args[1] + "'.");
			}

			@Override
			public List<String> tabComplete() {
				ArrayList<String> strs = new ArrayList<>();

				for (net.frozenorb.Raven.Traps.Trap t : RavenPlugin.get().getTrapManager().getTraps()) {
					strs.add(t.getName());
				}
				return strs;
			}
		});

		registerSubcommand(new Subcommand("del") {

			@Override
			protected void syncExecute() {
				if (!(args.length > 1)) {
					sender.sendMessage(ChatColor.RED + "/trap del <name>");
					return;
				}
				if (RavenPlugin.get().getTrapManager().getTrap(args[1]) != null) {
					RavenPlugin.get().getTrapManager().removeTrap(args[1]);

					sender.sendMessage(ChatColor.GRAY + "You have deleted the trap '" + args[1] + "'.");
				} else {
					sender.sendMessage(ChatColor.RED + "You have no trap with that name!");
				}
			}

			@Override
			public List<String> tabComplete() {
				ArrayList<String> strs = new ArrayList<>();

				for (net.frozenorb.Raven.Traps.Trap t : RavenPlugin.get().getTrapManager().getTraps()) {
					strs.add(t.getName());
				}
				return strs;
			}
		});

		registerSubcommand(new Subcommand("check") {

			@Override
			protected void syncExecute() {
				for (JSONChatMessage jcm : RavenPlugin.get().getTrapManager().getData(1)) {
					jcm.sendToPlayer((Player) sender);
				}
			}

		});

		registerSubcommandsToTabCompletions();

	}

	@Override
	public void syncExecute() {
		if (args.length > 0) {
			String name = args[0];

			if (RavenPlugin.get().getTrapManager().getTrap(name) != null) {
				sender.sendMessage(ChatColor.GRAY + "Teleporting you to the trap '" + args[0] + "'.");
				((Player) sender).teleport(RavenPlugin.get().getTrapManager().getTrap(name).getLocation());
			} else {
				sender.sendMessage(ChatColor.RED + "You have no trap with that name!");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "/trap <id>");
		}
	}
}
