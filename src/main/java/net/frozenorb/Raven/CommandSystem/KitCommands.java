package net.frozenorb.Raven.CommandSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Types.Kit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class KitCommands extends RavenCommand implements TabExecutor {
	public KitCommands(RavenPlugin plugin) {
		super(plugin);
	}

	private void sendInvalidUsage(CommandSender sender) {
		sender.sendMessage(new String[] { ChatColor.RED + "Invalid usage.",
				ChatColor.RED + "/kit <save> <kitName>",
				ChatColor.RED + "/kit <del> <kitName>",
				ChatColor.RED + "/kit <apply> <kitName> [<player>|ALL]" });

	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] params) {

		LinkedList<String> args = new LinkedList<String>(Arrays.asList(params));
		LinkedList<String> results = new LinkedList<String>();
		String action = null;
		if (args.size() >= 1) {
			action = args.pop().toLowerCase();
		} else {
			return results;
		}
		if (args.size() == 0) {
			for (String n : new String[] { "save", "apply", "del" }) {
				if (n.toLowerCase().startsWith(action.toLowerCase())) {
					results.add(n);
				}
			}
		}
		if (args.size() == 1 && (action.startsWith("del") || (action.startsWith("apply") || action.startsWith("give")))) {
			String ac = args.pop().toLowerCase();

			for (Kit k : getPlugin().getServerManager().getKits()) {
				if (k.getName().toLowerCase().startsWith(ac)) {
					results.add(k.getName());
				}
			}
		}
		return results;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("raven.kit")) {
			if (cmd.getName().equalsIgnoreCase("kit")) {
				if (args.length > 1) {
					if (args[0].equalsIgnoreCase("save")) {
						String kitname = args[1];
						Kit k = new Kit((Player) sender, kitname);
						Iterator<Kit> kiter = getPlugin().getServerManager().getKits().iterator();
						while (kiter.hasNext()) {
							Kit kite = kiter.next();
							if (kite.getName().equalsIgnoreCase(kitname))
								kiter.remove();
						}
						getPlugin().getServerManager().getKits().add(k);
						sender.sendMessage(ChatColor.GRAY + "Kit '" + kitname + "' has been saved.");
					} else if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete")) {

						String kitname = args[1];
						Iterator<Kit> kiter = getPlugin().getServerManager().getKits().iterator();
						while (kiter.hasNext()) {
							Kit kite = kiter.next();
							if (kite.getName().equalsIgnoreCase(kitname)) {
								sender.sendMessage(ChatColor.GRAY + "Kit '" + kitname + "' has been deleted.");
								kiter.remove();
								return true;
							}
						}
						sender.sendMessage(ChatColor.GRAY + "Kit '" + kitname + "' not found!");
					} else if (args[0].equalsIgnoreCase("apply") || args[0].equalsIgnoreCase("give")) {
						ArrayList<Player> applicable = new ArrayList<Player>();
						if (args.length > 2) {
							if (args[2].equalsIgnoreCase("all")) {
								applicable.addAll(Arrays.asList(Bukkit.getOnlinePlayers()));
							} else {
								applicable.addAll(Bukkit.matchPlayer(args[2]));
							}
						} else
							applicable.add((Player) sender);

						for (Kit k : getPlugin().getServerManager().getKits()) {
							if (k.getName().equalsIgnoreCase(args[1])) {
								for (Player p : applicable) {
									k.equip(p);
								}
								ArrayList<String> pls = new ArrayList<String>();
								for (Player p : applicable) {
									pls.add(p.getName());

								}
								sender.sendMessage(ChatColor.GRAY + "Kit '" + args[1] + "' has been equipped to : " + pls.toString() + ".");
								break;

							}
						}
						sender.sendMessage(ChatColor.GRAY + "Kit '" + args[1] + "' not found!");

					}
				} else {
					sendInvalidUsage(sender);
				}
			} else if (cmd.getName().equalsIgnoreCase("kits")) {
				sender.sendMessage(ChatColor.GRAY + "Kits: " + getPlugin().getServerManager().getKits().toString());
			}
		}
		return true;
	}
}
