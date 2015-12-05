package net.frozenorb.Raven.EconomySystem;

import net.frozenorb.Utilities.Interfaces.Loadable;
import net.frozenorb.Utilities.Interfaces.Saveable;
import net.frozenorb.Utilities.Types.RedisOperation;
import net.frozenorb.mBasic.EconomySystem.UUID.UUIDEconomyManager;
import org.bukkit.Material;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that provides economy utilities for economy commands
 * 
 * @author Kerem Celik
 * 
 */
@SuppressWarnings("deprecation")
public class Economy implements Loadable<Jedis>, Saveable {
	public static Map<String, ArrayList<EconomyItem>> economy = new ConcurrentHashMap<String, ArrayList<EconomyItem>>();

	private static Economy instance;

	protected Economy() {
		instance = this;
	}

	public static Economy getInstance() {
		if (instance == null) {
			return new Economy();
		}
		return instance;
	}

	@Override
	public void load(Jedis j) {
		for (String key : j.keys("econ.*")) {
			ArrayList<EconomyItem> eitems = new ArrayList<EconomyItem>();
			String materialType = key.split("\\.")[1].split(":")[0];
			String idString = key.split(":")[1];
			Material m = Material.getMaterial(materialType);
			short id = Short.parseShort(idString);
			String name = m.toString() + ":" + id;
			Map<String, String> data = j.hgetAll(key);
			for (Entry<String, String> entry : data.entrySet()) {
				String sellerData = entry.getKey();
				String amount = entry.getValue();
				double price = Double.parseDouble(sellerData.split(":")[1]);
				UUID sellerName = UUID.fromString(sellerData.split(":")[0]);
				int amt = Integer.parseInt(amount);
				EconomyItem item = new EconomyItem(m.getId(), price, id, sellerName, amt);
				if (item.getAmount() == 0) {
					item.setOperation(RedisOperation.DELETE);
				}
				eitems.add(item);
			}
			economy.put(name, eitems);
		}
	}

	@Override
	public void save() {
		// temporary work around for a derp.
		JedisPool pool = UUIDEconomyManager.getPool();
		Jedis j = pool.getResource();
		try {
			saveUsingJedis(j);
		} catch (JedisException ex) {
			pool.returnBrokenResource(j);
		} finally {
			pool.returnResource(j);
		}
	}

	public void saveUsingJedis(Jedis j) {
		for (Entry<String, ArrayList<EconomyItem>> entries : economy.entrySet()) {
			Iterator<EconomyItem> iter = entries.getValue().iterator();
			while (iter.hasNext()) {
				EconomyItem item = iter.next();
				String key = "econ." + Material.getMaterial(item.getId()).toString() + ":" + item.getDurability();
				if (item.getOperation() == RedisOperation.DELETE) {
					j.hdel(key, item.getSeller() + ":" + item.getPrice());
				}
				if (item.getOperation() == RedisOperation.INSERT) {
					j.hset(key, item.getSeller().toString() + ":" + item.getPrice(), item.getAmount() + "");
				}
				if (item.getOperation() == RedisOperation.REPLACE) {
					j.hset(key, item.getSeller().toString() + ":" + item.getPrice(), item.getAmount() + "");
				}
			}
		}

	}
}
