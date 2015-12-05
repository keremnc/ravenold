package net.frozenorb.Raven.Managers;

import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class HomeManager {
	private HashMap<UUID, Location> homeMap = new HashMap<>();
	private HashMap<UUID, ArrayList<Home>> oldHomes = new HashMap<>();

	public HomeManager(JavaPlugin plugin) {
		loadHomes();
	}

	public void setHome(UUID name, Location playerLocation) {
		homeMap.put(name, playerLocation);
		ArrayList<Home> homes = new ArrayList<HomeManager.Home>();
		if (oldHomes.containsKey(name)) {
			homes = oldHomes.get(name);
		}
		Home h = new Home(name, playerLocation).setTimestamp(System.currentTimeMillis());
		homes.add(h);
		oldHomes.put(name, homes);
		boolean hqFirst = true;
		String hist = "";
		if (!hqFirst)
			hist += ",";
		hqFirst = false;
		Location l = h.getHome();
		hist += ("(" + l.getX() + " " + l.getY() + " " + l.getZ() + " " + l.getYaw() + " " + l.getPitch() + " " + l.getWorld().getName() + ") - " + h.timestamp);
		RavenPlugin.syncRedis.append("homes.history." + name.toString(), "," + hist);
		saveHome(name);
	}

	public ArrayList<Home> getHomes() {
		ArrayList<Home> homes = new ArrayList<Home>();
		for (UUID playerName : homeMap.keySet()) {
			Home h = new Home(playerName, homeMap.get(playerName));
			homes.add(h);
		}
		return homes;
	}

	public ArrayList<Home> getOldHomes(UUID name) {
		if (!oldHomes.containsKey(name)) {
			return null;
		}
		ArrayList<Home> al = oldHomes.get(name);
		Collections.sort(al, new Comparator<Home>() {
			@Override
			public int compare(Home o1, Home o2) {
				return ((Long) o1.timestamp).compareTo(o2.timestamp);
			}
		});
		return al;
	}

	public Location getHome(UUID name) {
		return homeMap.get(name);
	}

	private void saveHome(UUID name) {
		if (!RavenPlugin.syncRedis.isConnected()) {
			RavenPlugin.syncRedis.connect();
		}
		Location location = homeMap.get(name);
		String home = (location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch());
		RavenPlugin.syncRedis.set("homes." + name.toString(), home);
	}

	private void loadHomes() {
		if (!RavenPlugin.syncRedis.isConnected()) {
			RavenPlugin.syncRedis.connect();
		}
		for (String key : RavenPlugin.syncRedis.keys("homes.*")) {
			if (key.startsWith("homes.history")) {
				continue;
			}
			String str = RavenPlugin.syncRedis.get(key);
			String name = (key.split("\\.")[1]);
			UUID id = UUID.fromString(name);
			loadHome(id, str);
			loadHistory(id, RavenPlugin.syncRedis.get("homes.history." + id.toString()));
		}
	}

	private void loadHistory(UUID name, String data) {
		if (data == null) {
			return;
		}
		String[] lineParts = data.split(",");
		for (String meta : lineParts) {
			if (!meta.contains(" - "))
				continue;
			long timestamp = Long.parseLong(meta.split(" - ")[1]);
			String coords = meta.split(" - ")[0].replace("(", "").replace(")", "");
			double x = Double.parseDouble(coords.split(" ")[0]);
			double y = Double.parseDouble(coords.split(" ")[1]);
			double z = Double.parseDouble(coords.split(" ")[2]);
			float yaw = Float.parseFloat(coords.split(" ")[3]);
			float pitch = Float.parseFloat(coords.split(" ")[4]);
			String world = coords.split(" ")[5];
			Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
			Home h = new Home(name, loc).setTimestamp(timestamp);
			ArrayList<Home> homes = new ArrayList<HomeManager.Home>();
			if (oldHomes.containsKey(name)) {
				homes = oldHomes.get(name);
			}
			homes.add(h);
			oldHomes.put(name, homes);
		}
	}

	private void loadHome(UUID playerName, String data) {

		String[] lineParts = data.split(",");

		if (lineParts.length == 6) {
			World world = Bukkit.getWorld(lineParts[0]);
			double x = Double.parseDouble(lineParts[1]);
			double y = Double.parseDouble(lineParts[2]);
			double z = Double.parseDouble(lineParts[3]);
			float yaw = Float.parseFloat(lineParts[4]);
			float pitch = Float.parseFloat(lineParts[5]);

			homeMap.put(playerName, new Location(world, x, y, z, yaw, pitch));
		} else {}

	}

	public class Home {
		private long timestamp;
		private UUID player;
		private Location home;

		public Location getHome() {
			return home;
		}

		public Home setTimestamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public UUID getPlayer() {
			return player;
		}

		@Override
		public int hashCode() {
			return (int) (timestamp * home.hashCode());
		}

		public Home(UUID player, Location home) {
			this.player = player;
			this.home = home;
		}
	}
}