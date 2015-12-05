package net.frozenorb.Raven.CommandSystem.Commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team.*;

public class Team extends BaseCommand {

	public Team() {
		super("team", "t");
		registerSubcommand(new Chat("chat", "§c/t chat", "c"));
		registerSubcommand(new Create("create", "§c/t create <name> [password]"));
		registerSubcommand(new Demote("demote", "§c/t demote <player>"));
		registerSubcommand(new FF("ff", "§c/t ff <on|off>", "friendlyfire"));
		registerSubcommand(new HQ("hq", "§c/t hq", "home"));
		registerSubcommand(new Info("info", "§c/t i", "i"));
		registerSubcommand(new Join("join", "§c/t join <team> [password]", "j"));
		registerSubcommand(new Kick("kick", "§c/t kick <player>", "k"));
		registerSubcommand(new Leave("leave", "§c/t leave", "l"));
		registerSubcommand(new Pass("pass", "§c/t pass [password]", new String[] {
				"password", "setpass" }));
		registerSubcommand(new Promote("promote", "§c/t promote <player>", "p"));
		registerSubcommand(new Rally("rally", "§c/t rally", "base"));
		registerSubcommand(new SetHQ("sethq", "§c/t sethq"));
		registerSubcommand(new SetRally("setrally", "§c/t setrally"));
		registerSubcommand(new Msg("msg", "§c/t msg <message>", new String[] {
				"m", "message" }));
		registerSubcommand(new Roster("roster", "§c/t roster <message>", "r"));
		registerSubcommand(new Disband("disband", "§c/t disband <team>"));
		registerSubcommandsToTabCompletions();
	}

	@Override
	public void syncExecute() {
		if (args.length == 0) {
			Player p = (Player) sender;
			p.sendMessage(ChatColor.DARK_AQUA + "***Anyone***");
			p.sendMessage(ChatColor.GRAY + "/team create [teamName] [password] - Create a team.");
			p.sendMessage(ChatColor.GRAY + "/team join [teamName] [password] - Join a team.");
			p.sendMessage(ChatColor.GRAY + "/team leave - Leave your current team.");
			p.sendMessage(ChatColor.GRAY + "/team roster <team> - Get details about the team.");
			p.sendMessage(ChatColor.GRAY + "/team tag <tag> - Gets the team owning the tag.");
			p.sendMessage(ChatColor.GRAY + "/team info - Get details about your current team.");
			p.sendMessage(ChatColor.GRAY + "/team info [playerName] - Get details about a team..");
			p.sendMessage(ChatColor.GRAY + "/team chat - Toggle team chat only mode on or off.");
			p.sendMessage(ChatColor.GRAY + "/team msg <message> - Sends a message to your team.");
			p.sendMessage(ChatColor.GRAY + "/team hq - Teleport to the team headquarters.");
			p.sendMessage(ChatColor.GRAY + "/team rally - Teleport to the team rally point.");
			p.sendMessage(ChatColor.DARK_AQUA + "***Team Managers Only***");
			p.sendMessage(ChatColor.GRAY + "/team kick [player] - Kick a player from the team.");
			p.sendMessage(ChatColor.GRAY + "/team sethq [password] - Set the team headquarters warp location.");
			p.sendMessage(ChatColor.GRAY + "/team setrally [password] - Set the team rally point warp location.");
			p.sendMessage(ChatColor.GRAY + "/team pass - Set the team password.");
			p.sendMessage(ChatColor.GRAY + "/team demote [playerName] - Demote a player to a member.");
			p.sendMessage(ChatColor.GRAY + "/team promote [playerName] - Make a player an owner on your team.");
		} else
			sender.sendMessage(new String[] { "§cUnrecognized team command.",
					"§7Type §3'/t'§7 for details." });
	}
}
