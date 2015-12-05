package net.frozenorb.Raven.Tracking;

import java.util.HashMap;

import org.bukkit.Location;

/**
 * Factory class used to create and access existing instances for
 * {@link Tracker} objects.
 * 
 * @author Kerem Celik
 * 
 */
public class TrackerFactory {

	private static HashMap<Location, Tracker> trackerCache = new HashMap<Location, Tracker>();

	/**
	 * Gets a new tracker instance from the existing cache if an identical
	 * instance exists, otherwise creates a new ones.
	 * 
	 * @param loc
	 *            the location to get the tracker for
	 * @return tracker
	 */
	public static Tracker createTracker(Location loc) {
		Location valid = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

		if (trackerCache.containsKey(valid)) {
			return trackerCache.get(valid);
		}

		return new Tracker(valid.getBlockX(), valid.getBlockY(), valid.getBlockZ());
	}
}
