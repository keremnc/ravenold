package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm.*;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Alarm.Team;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Utilities.Message.*;
import net.frozenorb.mShared.Shared;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Alarm extends BaseCommand {
	private static String COLOR = "§7";

	public Alarm() {
		super("alarm", "a");
		registerSubcommand(new Delete("delete", new String[] { "del", "remove", "d" }));
		registerSubcommand(new Friend("friend", "f"));
		registerSubcommand(new Unfriend("unfriend", "u"));
		registerSubcommand(new Help("help", "?"));
		registerSubcommand(new Set("set", "here"));
		registerSubcommand(new Team("team", "t"));
		registerSubcommand(new Location("location", "l"));
		registerSubcommand(new Link("link", "phone"));
		registerSubcommandsToTabCompletions();
		setPermissionLevel("raven.alarm", ChatColor.RED + "Only the §9MVP§c rank and above can use this command!");
	}

	@Override
	public void syncExecute() {

		if (args.length == 0) {
			Player p = (Player) sender;
			p.sendMessage("");
			p.sendMessage("");
			p.sendMessage(ChatColor.GOLD + "============= Alarm Information =============");
			net.frozenorb.Raven.Types.Alarm a = RavenPlugin.get().getAlarmManager().getAlarm(p.getUniqueId());
			if (a == null) {
				p.sendMessage(COLOR + "Your alarm is §cnot set" + COLOR + ".");
				p.sendMessage(COLOR + "Type '§c/a help§7' for more help with alarms.");
				p.sendMessage(ChatColor.GOLD + "==========================================");
				return;
			}
			JSONChatMessage msg = new JSONChatMessage("Your alarm is set! Click ", JSONChatColor.GRAY, Arrays.asList(new ArrayList<JSONChatFormat>().toArray(new JSONChatFormat[] {})));
			msg.addExtra(new JSONChatExtra("here", JSONChatColor.WHITE, Arrays.asList(new ArrayList<JSONChatFormat>() {
				private static final long serialVersionUID = 1L;
				{
					add(JSONChatFormat.BOLD);
				}
			}.toArray(new JSONChatFormat[] {}))) {
				{
					setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to see the location of your alarm!");
					setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/a location");
				}
			});
			msg.addExtra(new JSONChatExtra(" to see it's location", JSONChatColor.GRAY, Arrays.asList(new ArrayList<JSONChatFormat>().toArray(new JSONChatFormat[] {}))));
			msg.sendToPlayer(((Player) sender));
			p.sendMessage(COLOR + "Your team " + (a.isTeamFriendly() ? "§a§lwill not" : "§c§lwill") + COLOR + " trigger your alarm.");
			if (Shared.get().getProfileManager().getProfile(sender.getName()) != null) {
				if (Shared.get().getProfileManager().getProfile(sender.getName()).hasPhoneNumber()) {
					p.sendMessage(COLOR + "Your phone number §a§lis" + COLOR + " linked to your account!");
				} else
					p.sendMessage(COLOR + "Your phone number §c§lis not" + COLOR + " linked to your account! ");

			}
			p.sendMessage(COLOR + "The following players will not trigger your alarm: ");
			if (a.getFriendString().length() > 0) {
				JSONChatMessage friends = new JSONChatMessage("    ", JSONChatColor.WHITE, Arrays.asList(new ArrayList<JSONChatFormat>().toArray(new JSONChatFormat[] {})));
				boolean first = true;
				for (final UUID fr : a.getFriends()) {
					if (!first)
						friends.addExtra(new JSONChatExtra("§7,§f ", JSONChatColor.WHITE, Arrays.asList(new ArrayList<JSONChatFormat>().toArray(new JSONChatFormat[] {}))));
					friends.addExtra(new JSONChatExtra(FrozenUUIDCache.name(fr), JSONChatColor.WHITE, Arrays.asList(new ArrayList<JSONChatFormat>() {
						private static final long serialVersionUID = 1L;
						{
						}
					}.toArray(new JSONChatFormat[] {}))) {
						{
							setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§cClick here to remove §f" + FrozenUUIDCache.name(fr) + "§c as a friend.");
							setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/Alarm Unfriend " + FrozenUUIDCache.name(fr));

						}
					});

					first = false;
				}
				friends.sendToPlayer(((Player) sender));
			}
			p.sendMessage(COLOR + "Type '§c/a help§7' for more help with alarms.");
			p.sendMessage(ChatColor.GOLD + "==========================================");

		} else
			sender.sendMessage(new String[] { "§cUnrecognized alarm command.", "§7Type §3'/alarm help'§7 for command help." });

	}

	@Override
	public List<String> tabComplete() {
		return super.tabComplete();
	}
}
