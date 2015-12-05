package net.frozenorb.Raven.Types;

import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

public class Alarm {
	private UUID owner;
	private UUID[] friends = new UUID[] {};
	private boolean teamFriendly;
	private boolean changed = false;
	private Location location;

	public Alarm(UUID owner, boolean teamFriendy, Location location, UUID... friends) {
		this.location = location;
		this.friends = friends;
		this.teamFriendly = teamFriendy;
		this.owner = owner;
	}

	Alarm() {}

	/**
	 * Gets the location that the alarm is set to
	 * 
	 * @return location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Gets the list of players who will not set off the alarms
	 * 
	 * @return friends
	 */
	public UUID[] getFriends() {
		return friends;
	}

	/**
	 * Gets the player who owns the warp
	 * 
	 * @return owner
	 */
	public UUID getOwner() {
		return owner;
	}

	/**
	 * Whether teams will set off the alarms or not
	 * <p>
	 * true if teams will <b>NOT</b> set it off
	 * 
	 * @return team friendly
	 */
	public boolean isTeamFriendly() {
		return teamFriendly;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * Adds the given name to the list of players to not set off alerts
	 * 
	 * @param name
	 *            the name to add
	 */
	public void addFriend(UUID name) {
		ArrayList<UUID> frnds = new ArrayList<>(Arrays.asList(friends));
		frnds.add(name);
		friends = frnds.toArray(new UUID[] {});
		changed = true;

	}

	/**
	 * Removes the given name to the list of players to not set off alerts
	 * 
	 * @param name
	 *            the name to remove
	 */
	public void removeFriend(UUID name) {
		ArrayList<UUID> frnds = new ArrayList<>(Arrays.asList(friends));
		Iterator<UUID> iter = frnds.iterator();
		while (iter.hasNext()) {
			if (iter.next().equals(name))
				iter.remove();
		}
		friends = frnds.toArray(new UUID[] {});
		changed = true;

	}

	/**
	 * Sets the non-set off friends list
	 * 
	 * @param friends
	 *            friends to set to
	 */
	public void setFriends(UUID[] friends) {
		this.friends = friends;
		changed = true;

	}

	/**
	 * Sets the owner
	 * 
	 * @param owner
	 *            owner
	 */
	public void setOwner(UUID owner) {
		this.owner = owner;
		changed = true;

	}

	/**
	 * Sets if the alarm is team friendly or not
	 * 
	 * @param teamFriendly
	 *            team friendy
	 */
	public void setTeamFriendly(boolean teamFriendly) {
		this.teamFriendly = teamFriendly;
		changed = true;

	}

	/**
	 * Checks if the given player is a friend of this alarm
	 * 
	 * @param name
	 *            the player to check
	 * @return friends
	 */
	public boolean isFriend(UUID name) {
		for (UUID fr : friends)
			if (name.equals(fr))
				return true;
		return false;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public boolean hasChanged() {
		return changed;
	}

	public String getFriendString() {
		String friendStr = "";
		boolean first = true;
		for (UUID fstr : friends) {
			if (!first)
				friendStr += ", ";
			friendStr += fstr.toString();
			first = false;
		}
		return friendStr;
	}

	@Override
	public String toString() {
		String str = "Player:%s" + '\n' + "Location:%s" + '\n' + "Team:%s" + '\n' + "Friends:%s";
		String friendStr = "";
		boolean first = true;
		for (UUID fstr : friends) {
			if (!first)
				friendStr += ",";
			friendStr += fstr.toString();
			first = false;
		}
		return String.format(str, owner, getString(getLocation()), String.valueOf(isTeamFriendly()), friendStr);
	}

	/**
	 * Saves the Alarm to the given jedis object
	 * 
	 * @param j
	 *            jedis
	 */
	public void save(Jedis j) {
		j.set("alarm." + owner.toString(), toString());
		changed = false;
	}

	/**
	 * Loads an alarm from a data string
	 * 
	 * @param data
	 *            the data to load from
	 * @return alarm
	 */
	public static Alarm load(String data) {

		Alarm a = new Alarm();
		String[] lines = data.split("\n");
		for (String line : lines) {
			String identifier = line.substring(0, line.indexOf(':'));
			String[] lineParts = line.substring(line.indexOf(':') + 1).split(",");

			if (identifier.equalsIgnoreCase("Player")) {
				a.setOwner(UUID.fromString(lineParts[0]));
			} else if (identifier.equalsIgnoreCase("Location")) {
				a.setLocation(parseLocation(lineParts));
			} else if (identifier.equalsIgnoreCase("Team")) {
				a.setTeamFriendly(Boolean.parseBoolean(lineParts[0]));
			} else if (identifier.equalsIgnoreCase("Friends")) {
				for (String str : lineParts)
					if (str != null && !str.equalsIgnoreCase(""))
						a.addFriend(UUID.fromString(str));
			}
		}
		return a;

	}

	public boolean isRaiding(Player raider) {
		if (owner.equals(raider.getUniqueId()) || isFriend(raider.getUniqueId()) || (isTeamFriendly() && RavenPlugin.get().getServerManager().areOnSameTeam(
				owner, raider.getUniqueId())))
			return false;
		Player player = Bukkit.getPlayer(owner);
		if (location.getWorld().getName().equals(raider.getWorld().getName())) {
			Location l1c = location.clone();
			l1c.setY(0);
			Location l2c = raider.getLocation().clone();
			l2c.setY(0);
			if (l1c.distanceSquared(l2c) <= 625) {
				if (player != null && !player.canSee(raider))
					return false;
				return true;
			}
		}
		return false;

	}

	private static String getString(Location loc) {
		return (loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch());
	}

	private static Location parseLocation(String[] args) {
		if (args.length != 6)
			return null;

		World world = Bukkit.getWorld(args[0]);
		double x = Double.parseDouble(args[1]);
		double y = Double.parseDouble(args[2]);
		double z = Double.parseDouble(args[3]);
		float yaw = Float.parseFloat(args[4]);
		float pitch = Float.parseFloat(args[5]);

		return new Location(world, x, y, z, yaw, pitch);
	}
}
