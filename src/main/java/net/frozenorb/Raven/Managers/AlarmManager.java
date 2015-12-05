package net.frozenorb.Raven.Managers;

import net.frozenorb.Raven.Types.Alarm;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlarmManager {
	private Map<UUID, Alarm> alarmMap = new ConcurrentHashMap<>();

	public AlarmManager() {
		loadAllData();
	}

	/**
	 * Gets the given player's alarm
	 * 
	 * @param name
	 *            the player to get the alarm from
	 * @return alarm
	 */
	public Alarm getAlarm(UUID name) {
		if (alarmMap.containsKey(name))
			return alarmMap.get(name);
		return null;
	}

	/**
	 * Gets the arraylist of all alarms
	 * 
	 * @return alarms
	 */
	public ArrayList<Alarm> getAlarms() {
		return new ArrayList<Alarm>(alarmMap.values());
	}

	/**
	 * Removes alarms from the given player
	 * 
	 * @param name
	 *            name to remove from
	 */
	public void removeAlarm(UUID name) {
		alarmMap.remove(name);
	}

	/**
	 * Sets the player's alarm
	 * 
	 * @param name
	 *            player to set
	 * @param a
	 *            alarm to set to
	 */
	public void setAlarm(UUID name, Alarm a) {
		alarmMap.put(name, a);
	}

	/**
	 * Writes all of the data to Redis;
	 * 
	 * @param j
	 *            the jedis instance to use
	 */
	public void saveAllData(Jedis j) {
		for (Alarm a : getAlarms())
			a.save(j);
	}

	/**
	 * Loads all data from Redis
	 */
	public void loadAllData() {
		Jedis j = new Jedis("localhost");
		for (String key : j.keys("alarm.*")) {
			Alarm a = Alarm.load(j.get(key));
			alarmMap.put(a.getOwner(), a);
		}
	}
}
