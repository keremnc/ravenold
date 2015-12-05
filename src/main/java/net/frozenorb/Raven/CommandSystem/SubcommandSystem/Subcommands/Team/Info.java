package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import net.frozenorb.qlib.uuid.impl.RedisUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;

import java.util.UUID;

public class Info extends Subcommand {

	public Info(String name, String errorMessage, String... aliases) {
		super(name, errorMessage, aliases);
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
		if (args.length > 1) {
			Player target = Bukkit.getServer().getPlayer(args[1]);

			if (target == null || !((Player) sender).canSee(target)) {
				String n = args[1];
				UUID id = FrozenUUIDCache.uuid(n);

				if (id == null) {
					p.sendMessage(ChatColor.RED + "Could not find player '" + n + "'.");
					return;
				}

				if (RavenPlugin.get().getTeamManager().getPlayerTeam(id) == null) {
					p.sendMessage(ChatColor.GRAY + "That player is not on a team!");
				} else {
					net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(id);

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

				}
			} else {
				if (RavenPlugin.get().getTeamManager().getPlayerTeam(target.getUniqueId()) == null) {
					p.sendMessage(ChatColor.GRAY + "That player is online, but not on a team!");
				} else {
					net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(target.getUniqueId());

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
							p.sendMessage(" " + ChatColor.GRAY + FrozenUUIDCache.name(ow)+ ChatColor.GRAY + getInfo(ow));

					if (p.isOp())
						p.sendMessage(ChatColor.DARK_AQUA + "Password: " + team.getPassword().replace("INTEGER.MAX_VALUE", "No password"));

				}
			}
		} else {
			if (RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId()) == null)
				p.sendMessage(ChatColor.GRAY + "You are not on a team!");
			else {
				net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());

				p.sendMessage(ChatColor.GRAY + "***" + ChatColor.DARK_AQUA + team.getFriendlyName() + ChatColor.GRAY + "***");
				if (team.getTag() != null) {
					p.sendMessage(ChatColor.GRAY + "Tag: " + team.getTag());
				}
				p.sendMessage(ChatColor.GRAY + "Password: " + team.getPassword().replace("INTEGER.MAX_VALUE", "No password"));
				p.sendMessage(ChatColor.GRAY + "Headquarters: " + (team.getHQ() == null ? "Not set" : "Set"));
				if (team.getHQ() != null && team.getHQ().isProtected()) {
					p.sendMessage(ChatColor.GRAY + "Headquarters password: " + team.getHQ().getPassword());
				}
				p.sendMessage(ChatColor.GRAY + "Rally point: " + (team.getRally() == null ? "Not set" : "Set"));
				if (team.getRally() != null && team.getRally().isProtected()) {
					p.sendMessage(ChatColor.GRAY + "Rally point password: " + team.getRally().getPassword());
				}
				p.sendMessage(ChatColor.GRAY + "Friendly fire is " + (team.isFriendlyFire() ? "on" : "off"));
				p.sendMessage(ChatColor.GRAY + "Gold balance: " + team.getTotalBalance());
				p.sendMessage(ChatColor.GRAY + "Members (" + team.getOnlineMemberAmount() + "/" + team.getSize() + "):");
				for (UUID ow : team.getOwners())
					p.sendMessage(" " + ChatColor.DARK_AQUA + FrozenUUIDCache.name(ow) + ChatColor.GRAY + getInfo(ow));

				for (UUID ow : team.getMembers())
					if (!team.getOwners().contains(ow))
						p.sendMessage(" " + ChatColor.GRAY + FrozenUUIDCache.name(ow) + ChatColor.GRAY + getInfo(ow));

			}
		}
	}
}
