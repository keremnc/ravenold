package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.EconomySystem.Economy;
import net.frozenorb.Raven.EconomySystem.EconomyItem;
import net.frozenorb.Raven.EconomySystem.EconomyUtils;
import net.frozenorb.Raven.Utilities.FormatUtils;
import net.frozenorb.Utilities.Types.RedisOperation;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mBasic.EconomySystem.UUID.UUIDEconomyAccess;
import net.frozenorb.mBasic.Utilities.ItemDb;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Buy extends BaseCommand {
	public Buy() {
		super("buy", new String[] { "purchase" });
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

		Player player = (Player) sender;

		UUIDEconomyAccess economyManager = Basic.get().getUuidEconomyAccess();

		try {
			if (args.length == 3) {
				if (!EconomyUtils.isInt(args[0]) || !EconomyUtils.isDub(args[2].toLowerCase().replace("g", ""))) {
					sender.sendMessage("§c/buy <amount> <item> <limit>");
					return;
				}
				int timesToRun = Integer.parseInt(args[0]);
				double limit = Double.parseDouble(args[2].toLowerCase().replace("g", ""));
				if (timesToRun < 1) {
					sender.sendMessage(ChatColor.RED + "You must use a positive integer to buy items.");
					return;
				}
				if (Double.parseDouble(args[2].toLowerCase().replace("g", "")) < 0) {
					sender.sendMessage(ChatColor.RED + "You cannot buy for a negative amount of gold.");
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

				if (item.getType() == Material.MUSHROOM_SOUP && timesToRun % 64 != 0) {
					sender.sendMessage(ChatColor.RED + "You can only buy soups in increments of 64.");
					return;
				}
				if (item.getType() == Material.EXP_BOTTLE) {
					sender.sendMessage(ChatColor.RED + "You cannot buy this!");
					return;
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
					/* The amount of cash to deal out after the purchase */
					HashMap<UUID, Double> payments = new HashMap<>();
					/* The amount of items a player sold */
					HashMap<UUID, Integer> amountsold = new HashMap<>();
					Iterator<EconomyItem> iter = eec.iterator();
					while (iter.hasNext()) {
						EconomyItem ite = iter.next();
						if (ite.getAmount() == 0) {
							ite.setOperation(RedisOperation.DELETE);
							continue;
						}
						while (timesRun < timesToRun) {
							if (ite.getAmount() == 0) {
								break;
							}
							price += ite.getPrice();
							double itemPriceEach = ite.getPrice();
							EconomyUtils.increment(amountsold, ite.getSeller(), 1);
							if (payments.containsKey(ite.getSeller())) {
								itemPriceEach += payments.get(ite.getSeller());
							}
							payments.put(ite.getSeller(), itemPriceEach);
							timesRun += 1;
							ite.setAmount(ite.getAmount() - 1);
							ite.cacheOldValue();
							ite.setOperation(RedisOperation.REPLACE);
						}
					}
					if (price > economyManager.getBalance(player.getUniqueId())) {
						sender.sendMessage(String.format("§cThis costs %s gold while you only have %s gold.", EconomyUtils.getPriceFromDouble(price), economyManager.getBalance(player.getUniqueId())));
						return;
					}
					if (price > limit) {
						sender.sendMessage(String.format("§c%s %s costs %s gold which is over your %s gold limit.", timesToRun, friendlyName, FormatUtils.BALANCE_FORMAT.format(price), limit));
						return;
					}
					Player p = (Player) sender;
					if (p.getInventory().firstEmpty() == -1) {
						sender.sendMessage(ChatColor.RED + "Your inventory is full!");
						return;
					}
					item.setAmount(timesToRun);
					p.getInventory().addItem(item);
					economyManager.withdrawPlayer(player.getUniqueId(), price);
					for (UUID s : payments.keySet()) {
						double amountGive = payments.get(s);
						economyManager.depositPlayer(s, amountGive);
						if (Bukkit.getPlayer(s) != null) {
							Bukkit.getPlayer(s).sendMessage(ChatColor.GOLD + "A player has bought " + amountsold.get(s) + " " + friendlyName + " from you for " + FormatUtils.BALANCE_FORMAT.format(amountGive) + " gold. Your new balance is " + economyManager.getBalance(s) + ".");
						}
					}
					Economy.economy.put(dataValue, eec);
					sender.sendMessage(String.format("§7You have bought %s %s for %s gold. ", timesToRun, friendlyName, FormatUtils.BALANCE_FORMAT.format(price)) + "Your new balance is " + economyManager.getBalance(player.getUniqueId()) + ".");

				} else {
					sender.sendMessage(ChatColor.GRAY + "The economy has no " + friendlyName + " left.");
					return;
				}

			} else {
				sender.sendMessage(ChatColor.RED + "/buy <amount> <item> <limit>");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
}
