package net.frozenorb.Raven.Managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Utilities.Core;
import net.frozenorb.mShared.Shared;

import org.bukkit.entity.Player;

public class WarpAmountManager {
	private HashMap<String, Integer> groups;
	private Set<String> groupSet;
	public static int DEFAULT_WARP_AMOUNT;

	public WarpAmountManager() {
		DEFAULT_WARP_AMOUNT = RavenPlugin.get().getConfig().getInt("Default_Warp_Amount");
		reload();
	}

	public void reload() {
		this.groups = new HashMap<String, Integer>();
		this.groupSet = RavenPlugin.get().getConfig().getConfigurationSection("Groups").getKeys(false);
		Iterator<String> it = this.groupSet.iterator();
		while (it.hasNext()) {
			String currentValue = (String) it.next();
			this.groups.put(currentValue, Integer.valueOf(RavenPlugin.get().getConfig().getInt("Groups." + currentValue)));
		}

	}

	public int getMaxWarps(Player player) {
		if (player.isOp()) {
			return 30;
		}
		int warpAmount = 0;
		for (String group : this.groupSet) {
			String perm = "warp.group." + group;
			if (Core.get().hasPermission(player, perm)) {
				warpAmount = ((Integer) this.groups.get(group)).intValue();
				if (Shared.get().getProfileManager().getProfile(player) != null && Shared.get().getProfileManager().getProfile(player).isRegistered())
					warpAmount += 3;
				return warpAmount;
			}
		}
		warpAmount = 5;
		if (Shared.get().getProfileManager().getProfile(player) != null && Shared.get().getProfileManager().getProfile(player).isRegistered())
			warpAmount += 3;
		return warpAmount;
	}
}