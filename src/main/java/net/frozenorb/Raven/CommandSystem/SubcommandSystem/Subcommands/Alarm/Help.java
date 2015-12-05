package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;

public class Help extends Subcommand {

	public Help(String name, String... aliases) {
		super(name, null, aliases);
	}

	@Override
	protected void syncExecute() {
		CommandSender p = sender;
		p.sendMessage("");
		p.sendMessage("");
		p.sendMessage(ChatColor.GOLD + "================ Alarm Help ===============");
		p.sendMessage(ChatColor.GRAY + "/alarm - Show your alarm info");
		p.sendMessage(ChatColor.GRAY + "/alarm set - Set your alarm location");
		p.sendMessage(ChatColor.GRAY + "/alarm delete - Remove your alarm");
		p.sendMessage(ChatColor.GRAY + "/alarm friend <player> - Exempt a player");
		p.sendMessage(ChatColor.GRAY + "/alarm unfriend <player> - Unexempt a player");
		p.sendMessage(ChatColor.GRAY + "/alarm team [on|off] - Set team alarm exemption");
		p.sendMessage(ChatColor.GRAY + "/alarm link - Link your phone to your alarm!");
		p.sendMessage(ChatColor.GOLD + "==========================================");
	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}

}
