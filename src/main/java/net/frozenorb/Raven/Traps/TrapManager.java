package net.frozenorb.Raven.Traps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

import com.google.common.collect.Multimap;
import com.mongodb.BasicDBObject;

import net.frozenorb.Utilities.Message.JSONChatClickEventType;
import net.frozenorb.Utilities.Message.JSONChatColor;
import net.frozenorb.Utilities.Message.JSONChatExtra;
import net.frozenorb.Utilities.Message.JSONChatFormat;
import net.frozenorb.Utilities.Message.JSONChatHoverEventType;
import net.frozenorb.Utilities.Message.JSONChatMessage;
import net.frozenorb.mCommon.HackSystem.AlertSystem.Hack;
import net.frozenorb.mCommon.HackSystem.AlertSystem.HackMeta;
import net.frozenorb.mShared.Shared;
import redis.clients.jedis.Jedis;

public class TrapManager {

	private Map<String, Trap> alarmMap = new ConcurrentHashMap<String, Trap>();

	public TrapManager() {
		loadAllData();
	}

	public Trap getTrap(String name) {
		if (alarmMap.containsKey(name.toLowerCase()))
			return alarmMap.get(name.toLowerCase());
		return null;
	}

	public ArrayList<Trap> getTraps() {
		return new ArrayList<Trap>(alarmMap.values());
	}

	public void removeTrap(String name) {
		alarmMap.remove(name.toLowerCase());
	}

	public void setTrap(String name, Trap a) {
		alarmMap.put(name.toLowerCase(), a);
	}

	public void saveAllData(Jedis j) {
		for (Trap tr : getTraps())
			tr.save(j);
	}

	public JSONChatMessage[] getData(int page) {
		ArrayList<JSONChatMessage> data = new ArrayList<JSONChatMessage>();

		HashMap<Long, Trap> trapMap = new HashMap<Long, Trap>();
		TreeMap<Long, String> raids = new TreeMap<Long, String>(Collections.reverseOrder());

		for (Trap t : getTraps()) {
			Multimap<String, Long> mm = t.getTriggered();

			Map<String, Collection<Long>> map = mm.asMap();

			for (Entry<String, Collection<Long>> entry : map.entrySet()) {
				for (Long l : entry.getValue()) {
					raids.put(l, entry.getKey());
					trapMap.put(l, t);
				}
			}

		}
		int count = 0;

		for (final Map.Entry<Long, String> entry : raids.entrySet()) {
			if (count >= 20) {
				break;
			}

			String pName = entry.getValue();

			if (Shared.get().getProfileManager().isBanCached(pName)) {
				pName = "§8§m" + pName;
			} else {
				pName = "§e" + pName;
			}
			final Trap t = trapMap.get(entry.getKey());

			String msg = pName + "§7 triggered ";

			JSONChatMessage jcm = new JSONChatMessage(msg + "'§e" + t.getName() + "§7' ", JSONChatColor.YELLOW);
			jcm.addExtra(new JSONChatExtra(getString(t.getLocation()), JSONChatColor.LIGHT_PURPLE) {

				{
					setClickEvent(JSONChatClickEventType.RUN_COMMAND, "/trap " + t.getName());
					setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§aClick here to teleport to '" + t.getName() + "'.");
				}
			});

			long diff = System.currentTimeMillis() - entry.getKey();
			jcm.addExtra(new JSONChatExtra(" " + getConvertedTime(diff / 1000) + " ago.", JSONChatColor.GRAY, new ArrayList<JSONChatFormat>()) {
				{
					setHoverEvent(JSONChatHoverEventType.SHOW_TEXT, "§a" + getDate(entry.getKey()));
				}
			});

			data.add(jcm);

			count++;
		}

		return data.toArray(new JSONChatMessage[] {});
	}

	/**
	 * Loads all data from Redis
	 */
	public void loadAllData() {
		Jedis j = new Jedis("localhost");
		for (String key : j.keys("trap.*")) {
			Trap tr = Trap.load(j.get(key));
			alarmMap.put(tr.getName(), tr);
		}
	}

	public String getDate(long ts) {
		Date d = new Date(ts);
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm z");
		return df.format(d);
	}

	public String getConvertedTime(long i) {
		i = Math.abs(i);
		int hours = (int) Math.floor(i / 3600);
		int remainder = (int) (i % 3600), minutes = remainder / 60, seconds = remainder % 60;
		String toReturn;
		if (seconds == 0 && minutes == 0)
			return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + "0s";
		if (minutes == 0) {
			if (seconds == 1)
				return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%ss", seconds);
			return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%ss", seconds);
		}
		if (seconds == 0) {
			if (minutes == 1)
				return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm", minutes);
			return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm", minutes);
		}
		if (seconds == 1) {
			if (minutes == 1)
				return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm%ss", minutes, seconds);
			return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm%ss", minutes, seconds);
		}
		if (minutes == 1) {
			return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + String.format("%sm%ss", minutes, seconds);
		}
		toReturn = String.format("%sm%ss", minutes, seconds);
		return (hours != 0 ? (hours == 1 ? hours + "h" : hours + "h") : "") + "" + toReturn;
	}

	public String getString(Location loc) {
		return String.format("§d{%s, %s, %s}", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

}
