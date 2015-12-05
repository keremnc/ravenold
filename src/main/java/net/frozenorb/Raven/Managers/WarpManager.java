package net.frozenorb.Raven.Managers;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Types.TimestampedLocation;
import net.frozenorb.Raven.Types.Warp;
import net.frozenorb.Utilities.Message.*;
import net.frozenorb.mShared.Shared;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class WarpManager {
	private volatile ConcurrentHashMap<UUID, HashSet<Warp>> warpMap = new ConcurrentHashMap<>();
	private ConcurrentLinkedDeque<UUID> needsWarpSave = new ConcurrentLinkedDeque<>();

	public WarpManager(JavaPlugin pl) {
		loadWarps();
	}

	public ArrayList<Warp> getWarps() {
		ArrayList<Warp> warpList = new ArrayList<Warp>();
		for (HashSet<Warp> warps : warpMap.values()) {
			for (Warp w : warps) {
				warpList.add(w);
			}
		}
		return warpList;
	}

	public int getWarpCount(UUID playerName) {
		if (!warpMap.containsKey(playerName))
			return 0;

		return getWarpList(playerName).size();
	}

	public void setWarpList(UUID playerName, Collection<Warp> warps) {
		warpMap.put(playerName, new HashSet<Warp>(warps));
	}

	public void setWarp(UUID playerName, String warpName, Location playerLocation) {
		if (!warpMap.containsKey(playerName))
			warpMap.put(playerName, new HashSet<Warp>());
		needsWarpSave.add(playerName);
		if (warpExists(playerName, warpName)) {
			HashSet<Warp> wrps = warpMap.get(playerName);
			Iterator<Warp> iter = wrps.iterator();
			while (iter.hasNext()) {
				Warp w = iter.next();
				if (w.getName().equalsIgnoreCase(warpName)) {
					if (w.getName().equalsIgnoreCase(warpName)) {
						w.setActive(false);
						w.setWarpName(w.getName() + "- DELETED(" + System.currentTimeMillis() / 1000 + ")");
					}
				}
			}
		}
		warpMap.get(playerName).add(new Warp(playerName, warpName, playerLocation, true));

	}

	public ArrayList<Warp> getWarpList(UUID playerName) {

		if (warpMap.containsKey(playerName)) {
			ArrayList<Warp> warps = new ArrayList<Warp>();
			for (Warp w : warpMap.get(playerName)) {
				if (w.isActive())
					warps.add(w);
			}
			return warps;
		}
		return new ArrayList<Warp>();
	}

	public ArrayList<TimestampedLocation> getOldWarpList(UUID playerName) {
		if (warpMap.containsKey(playerName)) {
			ArrayList<TimestampedLocation> warps = new ArrayList<TimestampedLocation>();
			for (Warp w : warpMap.get(playerName)) {
				if (!w.isActive()) {
					long del = Long.parseLong(w.getName().split("- DELETED")[1].replace(")", "").replace("(", ""));
					TimestampedLocation loc = new TimestampedLocation(w.getLocation(), del * 1000);
					loc.setData(w.getName().split("- DELETED")[0]);
					warps.add(loc);
				}
			}
			Collections.sort(warps, new Comparator<TimestampedLocation>() {
				@Override
				public int compare(TimestampedLocation o1, TimestampedLocation o2) {
					return ((Long) o1.timestamp).compareTo(o2.timestamp);
				}
			});
			return warps;
		}
		return null;
	}

	public Warp getWarp(UUID playerName, String warpName) {
		if (!warpMap.containsKey(playerName))
			return null;
		for (Warp w : warpMap.get(playerName)) {
			if (w.getName().equalsIgnoreCase(warpName) && w.isActive())
				return w;
		}
		return null;
	}

	public void deleteWarp(UUID playerName, String warpName) {
		if (!warpMap.containsKey(playerName))
			return;
		needsWarpSave.add(playerName);
		for (Warp w : warpMap.get(playerName)) {
			if (w.getName().equalsIgnoreCase(warpName)) {
				w.setActive(false);
				w.setWarpName(w.getName() + "- DELETED(" + System.currentTimeMillis() / 1000 + ")");
			}
		}

	}

	public int saveAllWarps(Jedis j) {
		int amt = 0;
		for (UUID name : needsWarpSave) {
			saveWarps(name, j);
			amt += 1;
		}
		needsWarpSave.clear();
		return amt;
	}

	public void saveWarps(UUID name, Jedis j) {
		StringBuilder warpString = new StringBuilder();
		for (Warp warpEntry : warpMap.get(name)) {
			String warpName = warpEntry.getName();
			Location location = warpEntry.getLocation();
			warpString.append(warpName + "," + location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch() + "," + warpEntry.isActive() + '\n');
		}
		String warps = warpString.toString();
		j.set("warps." + name.toString(), warps);
	}

	private void loadWarps() {
		if (!RavenPlugin.syncRedis.isConnected()) {
			RavenPlugin.syncRedis.connect();
		}
		for (String key : RavenPlugin.syncRedis.keys("warps.*")) {
			String str = RavenPlugin.syncRedis.get(key);
			String player = (key.split("\\.")[1]);
			loadWarps(UUID.fromString(player), str);
		}

	}

	private void loadWarps(UUID playerName, String data) {

		String[] lines = data.split("\n");
		for (String line : lines) {

			String[] lineParts = line.split(",");

			if (lineParts.length == 8) {
				String name = lineParts[0];
				World world = Bukkit.getWorld(lineParts[1]);
				double x = Double.parseDouble(lineParts[2]);
				double y = Double.parseDouble(lineParts[3]);
				double z = Double.parseDouble(lineParts[4]);
				float yaw = Float.parseFloat(lineParts[5]);
				float pitch = Float.parseFloat(lineParts[6]);
				boolean active = Boolean.parseBoolean(lineParts[7]);
				if (!warpMap.containsKey(playerName))
					warpMap.put(playerName, new HashSet<Warp>());
				Warp w = new Warp(playerName, name, new Location(world, x, y, z, yaw, pitch), active);
				warpMap.get(playerName).add(w);
			} else {}

		}
	}

	public void printList(final Player player) {

		JSONChatMessage msg = new JSONChatMessage("", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>());
		msg.addExtra(new JSONChatExtra("***Warp List (" + getWarpCount(player.getUniqueId()) + "/" + RavenPlugin.get().getWarpAmountManager().getMaxWarps(player) + ")***", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()) {
			{
				if (!(Shared.get().getProfileManager().getProfile(player) != null && Shared.get().getProfileManager().getProfile(player).isRegistered()))
					setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§7Registering via §3/register <email>\n§7gives you §33§7 extra warps!");
			}
		});
		JSONChatMessage list = new JSONChatMessage("[", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>());
		boolean first = true;
		for (final Warp w : getWarpList(player.getUniqueId())) {
			if (!first)
				list.addExtra(new JSONChatExtra(", ", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()));
			list.addExtra(new JSONChatExtra(w.getName(), JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()) {
				{
					setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to warp to §f'" + w.getName() + "'§a.");
					setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/warp " + w.getName());
				}
			});
			first = false;
		}
		list.addExtra(new JSONChatExtra("]", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()));
		msg.sendToPlayer(player);
		list.sendToPlayer(player);
	}

	public String getList(UUID name) {
		List<String> warpList = new ArrayList<String>();

		for (Warp warpEntry : getWarpList(name))
			warpList.add(warpEntry.getName());

		Collections.sort(warpList);

		return ChatColor.GRAY + "" + warpList.toString();
	}

	public List<String> getWarpList(UUID name, boolean arg1) {
		List<String> warpList = new ArrayList<String>();

		for (Warp warpEntry : getWarpList(name))
			warpList.add(warpEntry.getName());

		Collections.sort(warpList);

		if (arg1) {
			warpList.add("DEL");
			warpList.add("SET");
			warpList.add("LIST");
		}

		return warpList;
	}

	public boolean canSetWarp(Player player) {
		if (getWarpCount(player.getUniqueId()) >= RavenPlugin.get().getWarpAmountManager().getMaxWarps(player))
			return false;

		return true;
	}

	public boolean sendHelp(Player player, String cmd) {
		player.sendMessage(ChatColor.RED + "Invalid /" + cmd + " usage.");
		player.sendMessage(ChatColor.GRAY + "/" + cmd + " set [warpName] - Set a warp");
		player.sendMessage(ChatColor.GRAY + "/" + cmd + " delete [warpName] - Delete a warp");
		player.sendMessage(ChatColor.GRAY + "/" + cmd + " [warpName] - Warp");
		player.sendMessage(ChatColor.GRAY + "/" + cmd + " list - List your warps.");
		return true;
	}

	public boolean warpPlayer(UUID playerName, String warpName) {
		if (warpExists(playerName, warpName)) {
			Bukkit.getPlayer(playerName).teleport(getWarp(playerName, warpName).getLocation());
			return true;
		}

		return false;
	}

	public boolean warpExists(UUID playerName, String warpName) {
		Warp w = getWarp(playerName, warpName);
		if (w == null)
			return false;
		if (!w.isActive())
			return false;
		return true;
	}
}