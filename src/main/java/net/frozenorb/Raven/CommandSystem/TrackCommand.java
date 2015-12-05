package net.frozenorb.Raven.CommandSystem;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.frozenorb.Raven.Tracking.Tracker;
import net.frozenorb.Raven.Tracking.TrackerFactory;
import net.frozenorb.Raven.Tracking.TrackerHelper;
import net.frozenorb.mCommon.Common;
import net.frozenorb.mCommon.Types.User;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class TrackCommand implements CommandExecutor, TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		LinkedList<String> params = new LinkedList<String>(Arrays.asList(args));
		LinkedList<String> results = new LinkedList<String>();
		String action = null;
		if (params.size() >= 1) {
			action = params.pop().toLowerCase();
		} else {
			return results;
		}
		if (params.size() == 0) {
			if ("all".toLowerCase().startsWith(action)) {
				results.add("all");
			}
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(action.toLowerCase()) && !p.hasMetadata("invisible")) {
					results.add(p.getName());
				}
			}
		}
		return results;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		if (player.getWorld().getEnvironment() == Environment.NETHER) {
			sender.sendMessage(ChatColor.RED + "You cannot track in the nether.");
			return true;
		}
		if (label.equalsIgnoreCase("track"))
			if (args.length == 1) {

				User u = Common.get().getUserManager().getUser(sender.getName());

				u.getServerData().append("lastTrackCommand", "/track " + args[0]);
				u.getServerData().append("right-click-track", args[0]);

				if (args[0].equalsIgnoreCase("all")) {
					Block block = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()).getBlock();
					if (block.getType() == Material.DIAMOND_BLOCK) {
						Tracker tracker = TrackerFactory.createTracker(new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()));
						tracker.trackAll(player);
					} else {
						player.sendMessage("Not a valid tracking compass.");
					}
				} else {
					Player oplayer = TrackerHelper.matchPlayer(args[0]);
					if (oplayer != null && !oplayer.hasMetadata("invisible")) {
						Tracker tracker = TrackerFactory.createTracker(new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ()));
						tracker.track(player, oplayer);
					} else {
						player.sendMessage(String.format("§6Player§f '%s' §6not found!", args[0]));
					}

				}
			} else {
				sender.sendMessage("Use /track <playerName> or /track all");
			}
		return false;
	}
}