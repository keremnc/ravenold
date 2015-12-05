package net.frozenorb.Raven.CommandSystem;

import java.util.ArrayList;

import net.frozenorb.Raven.RavenPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class WhoCommand extends RavenCommand {
	public WhoCommand(RavenPlugin plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		StringBuilder owner = new StringBuilder();
		StringBuilder admins = new StringBuilder();
		StringBuilder plus = new StringBuilder();
		StringBuilder mods = new StringBuilder();
		StringBuilder tmods = new StringBuilder();
		StringBuilder minis = new StringBuilder();
		StringBuilder u = new StringBuilder();
		StringBuilder m = new StringBuilder();
		StringBuilder s = new StringBuilder();
		StringBuilder d = new StringBuilder();
		StringBuilder def = new StringBuilder();
		Player[] players = Bukkit.getOnlinePlayers();
		ArrayList<String> names = new ArrayList<String>();
		for (Player player : players)
			names.add(player.getName());
		String[] ne = (String[]) names.toArray(new String[names.size()]);
		java.util.Arrays.sort(ne);
		for (String name : (ne)) {
			Player player = Bukkit.getServer().getPlayerExact(name);
			if (sender instanceof ConsoleCommandSender) {
				if (player.isOp()) {
					owner.append(ChatColor.DARK_RED + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("admin")) {
					admins.append(ChatColor.RED + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("modplus")) {
					plus.append(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("mod")) {
					mods.append(ChatColor.DARK_PURPLE + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("pro")) {
					m.append(ChatColor.GOLD + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("mvp")) {
					s.append(ChatColor.BLUE + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("vip")) {
					d.append(ChatColor.GREEN + ChatColor.stripColor(player.getName()) + "§f, ");
				} else {
					def.append(ChatColor.WHITE + player.getName() + "§f, ");
				}

			} else if (((Player) sender).canSee(player) && sender instanceof Player) {
				if (player.isOp()) {
					owner.append(ChatColor.DARK_RED + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("admin")) {
					admins.append(ChatColor.RED + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("modplus")) {
					plus.append(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("mod")) {
					mods.append(ChatColor.DARK_PURPLE + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("pro")) {
					m.append(ChatColor.GOLD + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("mvp")) {
					s.append(ChatColor.BLUE + ChatColor.stripColor(player.getName()) + "§f, ");
				} else if (player.hasPermission("vip")) {
					d.append(ChatColor.GREEN + ChatColor.stripColor(player.getName()) + "§f, ");
				} else {

					def.append(ChatColor.WHITE + player.getName() + "§f, ");
				}
			}
		}
		String msg = ChatColor.WHITE + "(" + "" + sender.getServer().getOnlinePlayers().length + "/" + sender.getServer().getMaxPlayers() + ") [" + owner.toString() + admins.toString() + plus.toString() + mods.toString() + tmods.toString() + minis.toString() + u.toString() + m.toString() + s.toString() + d.toString() + def.toString() + "]";
		sender.sendMessage("§4OP§r, §cADMIN§r, §9MOD+§r, §5MOD§r | §6PRO§r, §9MVP§r, §aVIP§r, §fDefault");
		sender.sendMessage(msg.replace(", ]", "]"));
		return true;
	}
}
