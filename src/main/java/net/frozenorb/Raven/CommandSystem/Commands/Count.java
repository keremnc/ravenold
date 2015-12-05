package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.EconomySystem.EconomyUtils;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mBasic.Utilities.ItemDb;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map.Entry;

public class Count extends BaseCommand {

	public Count() {
		super("count", "items");
	}

	@Override
	public void syncExecute() {
		if (args.length == 0) {
			Player p = (Player) sender;

			HashMap<String, Integer> items = new HashMap<String, Integer>();
			for (ItemStack it : p.getInventory()) {
				if (it != null && it.getType() != Material.AIR) {
					String str = it.getType() == Material.POTION ? CraftItemStack.asNMSCopy(it).getName() : ItemDb.getFriendlyName(it);

					if (items.containsKey(str)) {
						items.put(str, items.get(str) + it.getAmount());
					} else {
						items.put(str, it.getAmount());
					}
				}
			}
			for (Entry<String, Integer> entry : items.entrySet()) {
				p.sendMessage("§a" + entry.getKey() + "§e: " + entry.getValue());
			}
		} else {
			String name = args[0];

			ItemStack it = Basic.get().getItemDb().get(name);

			if (it.getType() == Material.AIR) {
				sender.sendMessage(ChatColor.RED + "Item '" + name + "' unrecognized!");
			} else {
				int count = 0;

				if (it.getType().getMaxDurability() == (short) 0) {
					count = EconomyUtils.countItems((Player) sender, it.getType(), it.getDurability(), true);
				} else {
					count = EconomyUtils.countItems((Player) sender, it.getType());

				}

				sender.sendMessage(ChatColor.YELLOW + "You have " + count + " of " + (it.getType() == Material.POTION ? CraftItemStack.asNMSCopy(it).getName() : ItemDb.getFriendlyName(it)) + "!");
			}
		}
	}
}
