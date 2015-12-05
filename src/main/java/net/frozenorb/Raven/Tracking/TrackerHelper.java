package net.frozenorb.Raven.Tracking;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Contains methods for finding players and assisting with tracking.
 * 
 * @author Kerem Celik
 * 
 */
public class TrackerHelper {

	/**
	 * Gets the player with the closest name, lexicographically, as the given
	 * name
	 * 
	 * @param player
	 *            the name to find
	 * @return player instance if found, null if not
	 */
	public static Player matchPlayer(String player) {
		List<Player> players = Bukkit.matchPlayer(player);

		if (players.size() == 1) {
			return (Player) players.get(0);
		}
		return null;
	}

	/**
	 * Gets a player by their name from the list of trackable players.
	 * 
	 * @param str
	 *            the name to get by
	 * @return player instance found
	 */
	public static Player findPlayer(String str) {
		List<Player> plist = getTrackablePlayers();
		for (int i = 0; i < plist.size(); i++) {
			if (((Player) plist.get(i)).getName().equals(str)) {
				return (Player) plist.get(i);
			}
		}
		return null;
	}

	/**
	 * Gets a list of trackable players.
	 * 
	 * @return trackable players
	 */
	public static List<Player> getTrackablePlayers() {
		Player[] players = Bukkit.getOnlinePlayers();
		List<Player> players1 = new ArrayList<Player>();
		for (int i = 0; i < players.length; i++) {
			if (!players[i].hasMetadata("invisible")) {
				players1.add(players[i]);
			}

		}
		return players1;
	}
}