package net.frozenorb.Raven.Traps;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import redis.clients.jedis.Jedis;

public class Trap {

	private String name;
	private Location location;
	private Multimap<String, Long> triggered = LinkedListMultimap.create();

	public Trap(String name, Location location) {
		this.location = location;
		this.name = name;
	}

	Trap() {}

	public boolean canTrigger(String name) {
		if (triggered.containsKey(name)) {
			LinkedList<Long> coll = new LinkedList<Long>(triggered.get(name));
			Collections.sort(coll);

			long passedMillis = System.currentTimeMillis() - coll.getLast();

			if (passedMillis < 1000 * 3600) {
				return false;
			}
		}
		return true;
	}

	public void trigger(String name) {
		triggered.put(name, System.currentTimeMillis());
	}

	public Location getLocation() {
		return location;
	}

	public String getName() {
		return name;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public String toString() {
		String data = getTriggers();
		String str = "Name:%s" + '\n' + "Location:%s" + '\n' + "Triggered:%s";
		return String.format(str, name, getString(getLocation()), data);
	}

	public String getTriggers() {
		String data = "";
		boolean first = true;

		for (Entry<String, Long> entry : triggered.entries()) {
			if (!first) {
				data += ",";
			}
			data += entry.getKey() + "-" + entry.getValue();
			first = false;
		}
		return data;
	}

	public Multimap<String, Long> getTriggered() {
		return triggered;
	}

	/**
	 * Saves the Alarm to the given jedis object
	 * 
	 * @param j
	 *            jedis
	 */
	public void save(Jedis j) {
		j.set("trap." + name, toString());
	}

	/**
	 * Loads an alarm from a data string
	 * 
	 * @param data
	 *            the data to load from
	 * @return alarm
	 */
	public static Trap load(String data) {

		Trap a = new Trap();
		String[] lines = data.split("\n");
		for (String line : lines) {
			String identifier = line.substring(0, line.indexOf(':'));
			String datas = line.substring(line.indexOf(':') + 1);

			String[] lineParts = datas.split(",");

			if (identifier.equalsIgnoreCase("Name")) {
				a.name = lineParts[0];
			} else if (identifier.equalsIgnoreCase("Location")) {
				a.location = parseLocation(lineParts);
			} else if (identifier.equalsIgnoreCase("Triggered") && datas.contains("-")) {
				for (String str : lineParts) {
					a.triggered.put(str.split("-")[0], Long.parseLong(str.split("-")[1]));
				}
			}
		}
		return a;

	}

	@SuppressWarnings("deprecation")
	public boolean isRaiding(Player raider) {
		if (raider.hasPermission("raven.trap")) {
			return false;
		}

		Player player = Bukkit.getPlayerExact(name);
		if (location.getWorld().getName().equals(raider.getWorld().getName())) {
			Location l1c = location.clone();
			Location l2c = raider.getLocation().clone();
			if (l1c.distanceSquared(l2c) <= 100) {
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
