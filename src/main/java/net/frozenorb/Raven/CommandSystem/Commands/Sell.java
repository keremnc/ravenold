package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.EconomySystem.Economy;
import net.frozenorb.Raven.EconomySystem.EconomyItem;
import net.frozenorb.Raven.EconomySystem.EconomyUtils;
import net.frozenorb.Raven.Utilities.FormatUtils;
import net.frozenorb.Utilities.Types.RedisOperation;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mBasic.Utilities.ItemDb;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Sell extends BaseCommand {
	public Sell() {
		super("sell", new String[] {});
	}

	@Override
	public List<String> tabComplete() {
		return new LinkedList<String>();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void syncExecute() {

		if (HaltEconomy.halted) {
			sender.sendMessage(ChatColor.RED + "Economy is currently disabled!");
			return;
		}

		try {
			if (args.length == 3) {

				if (args[0].equalsIgnoreCase("debug") && sender.isOp()) {
					sender.sendMessage(Economy.economy.toString());
					return;
				}

				String strAmount = args[0];

				if (!EconomyUtils.isInt(strAmount) || !EconomyUtils.isDub(args[2].toLowerCase().replace("g", ""))) {
					sender.sendMessage("§c/sell <amount> <item> <price>");
					return;
				}

				if (Integer.parseInt(strAmount) < 1) {
					sender.sendMessage(ChatColor.RED + "You must use a positive integer to sell items.");
					return;
				}

				if (Double.parseDouble(args[2].toLowerCase().replace("g", "")) < 0) {
					sender.sendMessage(ChatColor.RED + "You cannot sell for a negative amount of gold.");
					return;
				}

				ItemStack item = null;
				String friendlyName = null;

				if (EconomyUtils.isPot(args[1])) {

					int dmg = EconomyUtils.getData(args[1].toLowerCase());
					item = new ItemStack(Material.POTION);
					item.setDurability((short) dmg);
					friendlyName = args[1].toLowerCase();

					if (dmg == 16428) {
						for (ItemStack it : ((Player) sender).getInventory()) {
							if (it != null && it.getDurability() == 24620) {
								it.setDurability((short) 16428);
							}
						}
					}

				} else {

					item = new ItemDb(Basic.get()).get(args[1]);

					if (item.getType() == Material.AIR) {
						sender.sendMessage(ChatColor.RED + "Item '" + args[1] + "' not found.");
						return;
					}

					if (item.getDurability() != 0) {
						friendlyName = CraftItemStack.asNMSCopy(item).getName();
					} else {
						friendlyName = ItemDb.getFriendlyName(item);
					}
				}

				if (item.getType() == Material.EXP_BOTTLE || item.getType() == Material.MONSTER_EGG) {
					sender.sendMessage(ChatColor.RED + "You cannot sell this!");
					return;
				}

				Player p = (Player) sender;

				if (p.getInventory().contains(item.getType())) {
					int amt = EconomyUtils.countItems(p, item.getType(), item.getDurability(), true);
					if (amt < Integer.parseInt(args[0])) {
						sender.sendMessage(ChatColor.RED + "You only have " + amt + " " + friendlyName + "!");
						return;
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have any " + friendlyName + "!");
					return;
				}

				if (item.getType().getMaxDurability() != 0) {
					sender.sendMessage(ChatColor.RED + "You are not allowed to sell this.");
					return;
				}

				double price = Double.parseDouble(args[2].toLowerCase().replace("g", ""));
				double eachPrice = price / Integer.parseInt(strAmount);
				int times = Integer.parseInt(strAmount);

				if (Economy.economy.containsKey(item.getType().toString() + ":" + item.getDurability())) {
					ArrayList<EconomyItem> eitems = Economy.economy.get(item.getType().toString() + ":" + item.getDurability());
					boolean updatedExisting = false;

					for (EconomyItem eitem : eitems) {

						if (eitem.getSeller().equals(((Player) sender).getUniqueId()) && eitem.getPrice() == price) {
							eitem.setAmount(eitem.getAmount() + times);
							eitem.cacheOldValue();
							eitem.setOperation(RedisOperation.INSERT);
							updatedExisting = true;
						}
					}

					if (!updatedExisting) {
						eitems.add(new EconomyItem(item.getTypeId(), eachPrice, item.getDurability(), ((Player) sender).getUniqueId(), times).setOperation(RedisOperation.INSERT));
					}
				} else {
					ArrayList<EconomyItem> items = new ArrayList<EconomyItem>();
					items.add(new EconomyItem(item.getTypeId(), eachPrice, item.getDurability(), ((Player) sender).getUniqueId(), times).setOperation(RedisOperation.INSERT));
					Economy.economy.put(item.getType().toString() + ":" + item.getDurability(), items);
				}

				for (int sis = 0; sis < times; sis += 1) {
					for (ItemStack i : p.getInventory().getContents()) {
						if (i != null) {
							if (i.getType() == item.getType() && i.getDurability() == item.getDurability()) {
								if (i.getAmount() == 1) {
									p.getInventory().clear(p.getInventory().first(i));
									break;
								} else {
									i.setAmount(i.getAmount() - 1);
									break;
								}
							}
						}
					}
				}

				p.sendMessage(ChatColor.GRAY + "You have put " + strAmount + " " + friendlyName + " on the market for " + FormatUtils.BALANCE_FORMAT.format(price) + " gold.");
				p.updateInventory();

			} else {
				sender.sendMessage("§c/sell <amount> <item> <price>");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
