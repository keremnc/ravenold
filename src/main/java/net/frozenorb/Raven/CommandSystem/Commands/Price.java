package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.EconomySystem.Economy;
import net.frozenorb.Raven.EconomySystem.EconomyItem;
import net.frozenorb.Raven.EconomySystem.EconomyUtils;
import net.frozenorb.Raven.Utilities.FormatUtils;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mBasic.Utilities.ItemDb;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Price extends BaseCommand {
	public Price() {
		super("price", new String[] {});
	}

	@Override
	public List<String> tabComplete() {
		return new LinkedList<String>();
	}

	@Override
	public void syncExecute() {

		try {
			if (args.length == 2) {
				if (!EconomyUtils.isInt(args[0])) {
					sender.sendMessage("§c/price <amount> <itemStack> ");
					return;
				}
				int timesToRun = Integer.parseInt(args[0]);
				if (timesToRun < 1) {
					sender.sendMessage(ChatColor.RED + "You must use a positive integer to check prices.");
					return;
				}
				if (timesToRun > 1024) {
					sender.sendMessage(ChatColor.RED + "You may buy items with the amount of 1024 or less.");
					return;
				}
				ItemStack item = null;
				String friendlyName = null;
				if (EconomyUtils.isPot(args[1])) {
					item = new ItemStack(Material.POTION);
					int dmg = EconomyUtils.getData(args[1].toLowerCase());
					item.setDurability((short) dmg);
					friendlyName = args[1].toLowerCase();
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

				String dataValue = item.getType().toString() + ":" + item.getDurability();
				if (Economy.economy.containsKey(dataValue) && Economy.economy.get(dataValue).size() > 0) {
					ArrayList<EconomyItem> ec = Economy.economy.get(dataValue);
					ArrayList<EconomyItem> eec = EconomyUtils.cloneList(ec);
					Collections.sort(eec, new Comparator<EconomyItem>() {
						public int compare(EconomyItem a, EconomyItem b) {
							return Double.compare(a.getPrice(), b.getPrice());
						}
					});
					int availableAmount = 0;
					for (EconomyItem ei : eec) {
						availableAmount += ei.getAmount();
					}
					double price = 0;
					if (timesToRun > availableAmount) {
						sender.sendMessage(ChatColor.RED + "Only " + availableAmount + " " + friendlyName + " remain in the economy.");
						return;
					}
					int timesRun = 0;
					HashMap<UUID, Double> payments = new HashMap<>();
					HashMap<UUID, Integer> amountsold = new HashMap<>();
					Iterator<EconomyItem> iter = eec.iterator();
					while (iter.hasNext()) {
						EconomyItem ite = iter.next();
						if (ite.getAmount() == 0) {
							continue;
						}
						while (timesRun < timesToRun) {
							if (ite.getAmount() == 0) {
								break;
							}
							price += ite.getPrice();
							EconomyUtils.increment(amountsold, ite.getSeller(), 1);
							payments.put(ite.getSeller(), ite.getPrice());
							timesRun += 1;
							ite.setAmount(ite.getAmount() - 1);
						}
					}
					sender.sendMessage(String.format("§7%s %s costs %s gold.", timesToRun, friendlyName, FormatUtils.BALANCE_FORMAT.format(price)));

				} else {
					sender.sendMessage(ChatColor.GRAY + "The economy has no " + friendlyName + " left.");
					return;
				}

			} else {
				sender.sendMessage(ChatColor.RED + "/price <amount> <item>");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}
