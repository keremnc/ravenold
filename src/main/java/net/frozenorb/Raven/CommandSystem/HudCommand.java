package net.frozenorb.Raven.CommandSystem;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.mCommon.Common;
import net.frozenorb.mCommon.Types.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HudCommand extends BaseCommand {
	private static final String[] USAGE_STRINGS = new String[] {
			ChatColor.RED + "/hud [on | off]" };

	@Override
	public void syncExecute() {
		for (String s : USAGE_STRINGS) {
			sender.sendMessage(s);
		}
	}

	public HudCommand() {
		super("hud", new String[] { "hudcmd" });
		registerSubcommand(new Subcommand("on") {

			@Override
			protected void syncExecute() {
				User profile = Common.get().getUserManager().getUser((Player) sender);
				sender.sendMessage(ChatColor.YELLOW + "You have toggled your HUD on.");
				set(profile, "hud", true);
			}
		});
		registerSubcommand(new Subcommand("off") {

			@Override
			protected void syncExecute() {
				User profile = Common.get().getUserManager().getUser((Player) sender);
				sender.sendMessage(ChatColor.YELLOW + "You have toggled your HUD off.");
				set(profile, "hud", false);
			}
		});
		registerSubcommandsToTabCompletions();
	}

	public void set(User profile, String name, boolean state) {
		profile.getServerData().put(name, state);

	}

}
