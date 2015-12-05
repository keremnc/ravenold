package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import java.util.*;

import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.Managers.TeamManager;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.Utilities.Message.JSONChatClickEventType;
import net.frozenorb.Utilities.Message.JSONChatColor;
import net.frozenorb.Utilities.Message.JSONChatExtra;
import net.frozenorb.Utilities.Message.JSONChatFormat;
import net.frozenorb.Utilities.Message.JSONChatHoverEventType;
import net.frozenorb.Utilities.Message.JSONChatMessage;

@SuppressWarnings("unchecked")
public class Roster extends Subcommand {

	public Roster(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
		setCompleteSecondHalf(true);
	}

	private String getInfo(UUID s) {

		String msg = ChatColor.GRAY + " - Offline";
		if (Bukkit.getPlayer(s) != null && !Bukkit.getPlayer(s).hasMetadata("invisible")) {
			Player p = Bukkit.getPlayer(s);
			double h = ((Damageable) p).getHealth() / 2;
			double d = Math.ceil(h * 2) / 2;
			double perc = (double) (d / 10);
			msg = ChatColor.GRAY + " - " + perc * 100 + "% health";
			msg = msg.replace(".00000000000001", ".0");
		}
		return msg;
	}

	@Override
	public void syncExecute() {
		Player p = (Player) sender;
		TeamManager teamManager = RavenPlugin.get().getTeamManager();
		if (args.length > 1) {
			if (teamManager.teamExists(args[1])) {
				Team team = teamManager.getTeam(args[1]);
				p.sendMessage(ChatColor.GRAY + "***" + ChatColor.DARK_AQUA + team.getFriendlyName() + ChatColor.GRAY + "***");
				if (team.getTag() != null) {
					p.sendMessage(ChatColor.GRAY + "Tag: " + team.getTag());
				}
				p.sendMessage(ChatColor.GRAY + "Gold balance: " + team.getTotalBalance());

				p.sendMessage(ChatColor.GRAY + "Members (" + team.getOnlineMemberAmount() + "/" + team.getSize() + "):");
				for (UUID ow : team.getOwners())
					p.sendMessage(" " + ChatColor.DARK_AQUA + FrozenUUIDCache.name(ow) + ChatColor.GRAY + getInfo(ow));

				for (UUID ow : team.getMembers())
					if (!team.getOwners().contains(ow))
						p.sendMessage(" " + ChatColor.GRAY + FrozenUUIDCache.name(ow) + ChatColor.GRAY + getInfo(ow));

				if (p.isOp())
					p.sendMessage(ChatColor.DARK_AQUA + "Password: " + team.getPassword().replace("INTEGER.MAX_VALUE", "No password"));

			} else {
				ArrayList<String> teamNames = new ArrayList<String>();
				for (Team tem : teamManager.getTeams())
					teamNames.add(tem.getFriendlyName());
				p.sendMessage(ChatColor.RED + "Team '" + args[1] + "' could not be found.");
				JSONChatMessage msg = new JSONChatMessage("Did you mean: ", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>());
				boolean first = true;
				for (final String t : sortListBySearching(teamNames, args[1])) {
					if (!first)
						msg.addExtra(new JSONChatExtra(", ", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()));
					first = false;
					msg.addExtra(new JSONChatExtra(t, JSONChatColor.DARK_AQUA, new ArrayList<JSONChatFormat>()) {
						{
							Team te = RavenPlugin.get().getTeamManager().getTeam(t);
							String data = "§7Click here to view the roster of §3" + te.getFriendlyName() + "§7.";
							setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, data);
							setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/t roster " + t);
						}
					});
				}
				msg.sendToPlayer((Player) sender);
			}
		} else
			sender.sendMessage(ChatColor.RED + "/t roster <tName>");
	}

	/**
	 * Sorts a list by searching a arraylist for a string
	 * 
	 * @param l
	 *            the list to search
	 * @param s
	 *            the string to use to search
	 * @return the top 5 best matches
	 */
	private String[] sortListBySearching(ArrayList<String> search, String s) {
		List<String> l = (List<String>) search.clone();
		final Set<String> matches = new HashSet<String>();
		for (String tokens : s.split("\\s")) {
			matches.add(tokens.toLowerCase());
		}

		Comparator<String> c = new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int scoreDiff = getScore(o1) - getScore(o2);
				if ((getScore(o1) == 0 && getScore(o2) == 0) || scoreDiff == 0) {
					return o1.compareTo(o2);
				}
				return -(getScore(o1) - getScore(o2));
			}

			private int getScore(String s) {
				int score = 0;
				for (String match : matches) {
					if (s.toLowerCase().contains(match)) {
						score++;
					}
				}
				return score;
			}
		};
		Collections.sort(l, c);
		Iterator<String> iter = l.iterator();
		while (iter.hasNext())
			if (!iter.next().toLowerCase().substring(0, 1).equalsIgnoreCase(s.toLowerCase().substring(0, 1)))
				iter.remove();
		return l.subList(0, Math.min(5, l.size())).toArray(new String[] {});
	}

	@Override
	public List<String> tabComplete() {
		ArrayList<String> teamNames = new ArrayList<String>();
		for (Team tem : RavenPlugin.get().getTeamManager().getTeams()) {
			teamNames.add(tem.getFriendlyName());
		}
		return teamNames;
	}
}
