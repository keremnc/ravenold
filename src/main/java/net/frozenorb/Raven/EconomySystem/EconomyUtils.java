package net.frozenorb.Raven.EconomySystem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Class that provides economy utilities for economy commands
 * 
 * @author Kerem Celik
 * 
 */
public final class EconomyUtils {
	public static boolean contains(Player p, ItemStack item) {
		if (item == null) {
			return false;
		}
		for (ItemStack i : p.getInventory().getContents()) {
			if (i != null && i.getType() == item.getType() && i.getDurability() == item.getDurability()) {
				return true;
			}
		}
		return false;
	}

	public static int countItems(Player player, Material material) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] items = inventory.getContents();
		int amount = 0;
		for (ItemStack item : items) {
			if (item != null) {
				if (item.getType() != null && item.getType() == material) {
					amount += item.getAmount();
				}
			}
		}
		return amount;
	}

	public static int countItems(Player player, Material material, int damageValue, boolean yes) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] items = inventory.getContents();
		int amount = 0;
		for (ItemStack item : items) {
			if (item != null) {
				if (item.getType() != null && item.getType() == material && damageValue == item.getDurability()) {
					amount += item.getAmount();
				}
			}
		}
		return amount;
	}

	public static void increment(Map<UUID, Integer> map, UUID s, int i) {
		if (map.containsKey(s)) {
			map.put(s, map.get(s) + 1);
		} else {
			map.put(s, 1);
		}
	}

	private static String removeTrailingZeros(String str) {
		return str.replaceAll("[0]*$", "").replaceAll("\\.$", "");
	}

	public static String getPriceFromDouble(double d) {
		String str = new BigDecimal(d).toPlainString();
		if (str.contains(".")) {
			try {
				return removeTrailingZeros(str.substring(0, str.indexOf('.') + 9));
			} catch (Exception ex) {
				return removeTrailingZeros(str);
			}
		} else {
		return str;
	}
	}

	public static boolean isDub(String i) {
		try {
			Double.parseDouble(i);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public static boolean isPot(String s) {
		String[] pots = new String[] { "hp1", "hp2", "rp1", "rp1e", "rp2",
				"dp1", "dp2", "swp1", "swp1e", "swp2", "slp1", "slp1e",
				"strp1", "strp1e", "strp2", "wp1", "wp1e", "pp1", "pp1e",
				"pp2", "frp1", "frp1e", "invp", "nvp", "dp1s", "dp2s", "swp1s",
				"swp1es", "swp2s", "slp1s", "slp1es", "strp1s", "strp1es",
				"strp2s", "wp1s", "wp1es", "pp1s", "pp1es", "pp2s", "frp1s",
				"frp1es", "invps", "nvps" };
		ArrayList<String> pot = new ArrayList<String>();
		for (String ss : pots)
			pot.add(ss);
		if (pot.contains(s.toLowerCase()))
			return true;
		return false;
	}

	public static int getData(String str) {
		int dmg = 0;
		switch (str.toLowerCase()) {
		case "strp2":
			dmg = 8233;
			break;
		case "swp2":
			dmg = 8226;
			break;
		case "frp1e":
			dmg = 8259;
			break;
		case "hp1":
			dmg = 8197;
			break;
		case "hp2":
			dmg = 8229;
			break;
		case "rp1":
			dmg = 8193;
			break;
		case "rp1e":
			dmg = 8257;
			break;
		case "rp2":
			dmg = 8225;
			break;
		case "dp1":
			dmg = 8204;
			break;
		case "dp2":
			dmg = 8236;
			break;
		case "swp1":
			dmg = 8194;
			break;
		case "slp1":
			dmg = 8202;
			break;
		case "slp1e":
			dmg = 8266;
			break;
		case "strp1":
			dmg = 8201;
			break;
		case "strp1e":
			dmg = 8265;
			break;
		case "wp1":
			dmg = 8200;
			break;
		case "wp1e":
			dmg = 8264;
			break;
		case "pp1":
			dmg = 8196;
			break;
		case "pp1e":
			dmg = 8260;
			break;
		case "pp2":
			dmg = 8228;
			break;
		case "frp1":
			dmg = 8195;
			break;
		case "invp":
			dmg = 8270;
			break;
		case "nvp":
			dmg = 8262;
			break;
		/* SPLASH */
		case "strp2s":
			dmg = 16425;
			break;
		case "swp2s":
			dmg = 16418;
			break;
		case "frp1es":
			dmg = 16451;
			break;
		case "hp1s":
			dmg = 16389;
			break;
		case "hp2s":
			dmg = 16421;
			break;
		case "rp1s":
			dmg = 16385;
			break;
		case "rp1es":
			dmg = 16449;
			break;
		case "rp2s":
			dmg = 16417;
			break;
		case "dp1s":
			dmg = 16460;
			break;
		case "dp2s":
			dmg = 16428;
			break;
		case "swp1s":
			dmg = 16386;

			break;
		case "slp1s":
			dmg = 16394;

			break;
		case "slp1es":
			dmg = 16458;

			break;
		case "strp1s":
			dmg = 16393;
			break;
		case "strp1es":
			dmg = 16457;

			break;
		case "wp1s":
			dmg = 16456;

			break;
		case "wp1es":
			dmg = 16424;
			break;
		case "pp1s":
			dmg = 16388;
			break;
		case "pp1es":
			dmg = 16452;

			break;
		case "pp2s":
			dmg = 16420;
			break;
		case "frp1s":
			dmg = 16387;
			break;
		case "invps":
			dmg = 16430;
			break;
		case "nvps":
			dmg = 16422;
			break;
		}
		return dmg;
	}

	public static boolean isInt(String i) {
		try {
			Integer.parseInt(i);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public static ArrayList<EconomyItem> cloneList(ArrayList<EconomyItem> list) {
		ArrayList<EconomyItem> clone = new ArrayList<EconomyItem>(list.size());
		for (EconomyItem item : list)
			clone.add(new EconomyItem(item));
		return clone;
	}
}
