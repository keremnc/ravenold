package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.mShared.Shared;
import net.frozenorb.mShared.API.Profile.PlayerProfile;

public class Link extends Subcommand {

	public Link(String name, String... aliases) {
		super(name, null, aliases);
	}

	@Override
	protected void syncExecute() {
		PlayerProfile pp = Shared.get().getProfileManager().getProfile(sender.getName());
		if (pp == null) {
			sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "We couldn't reach our database to fulfill your request, please try again at a later time!");
			return;
		}
		sender.sendMessage(ChatColor.GOLD + "============= Link a Phone =============");
		sender.sendMessage(ChatColor.GRAY + "To link your phone to your alarm, please text");
		sender.sendMessage(ChatColor.YELLOW + "REGISTER " + sender.getName() + " " + pp.getPhoneCode());
		sender.sendMessage(ChatColor.GRAY + "to (310) 879-5180");
		sender.sendMessage(ChatColor.GOLD + "==========================================");
	}

	@Override
	public List<String> tabComplete() {
		return new ArrayList<String>();
	}
}
