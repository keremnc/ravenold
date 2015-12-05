package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.Utilities.FormatUtils;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mBasic.EconomySystem.UUID.UUIDEconomyAccess;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@SuppressWarnings("deprecation")
public class Deposit extends BaseCommand {

	public Deposit() {
		super("deposit", new String[] { "d" });
	}

	public static int countItems(Player player, Material material, int damageValue) {
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

	@Override
	public void syncExecute() {
		if (HaltEconomy.halted) {
			sender.sendMessage(ChatColor.RED + "Economy is currently disabled!");
			return;
		}
		UUIDEconomyAccess economyManager = Basic.get().getUuidEconomyAccess();
		Player p = ((Player) sender);
		if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
			int g = countItems((Player) sender, Material.GOLD_INGOT, 0);
			if (g > 0) {
				economyManager.depositPlayer(p.getUniqueId(), g);
				p.getInventory().remove(Material.GOLD_INGOT);
				p.updateInventory();
				p.sendMessage("§7Deposit succesful. ");
				p.sendMessage("§7You now have " + FormatUtils.BALANCE_FORMAT.format(economyManager.getBalance(p.getUniqueId())) + " gold in your bank account.");
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have any gold!");
			}
			return;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "/" + cmd.getName() + " <amount>");
			return;
		}
		String number = args[0];

		int amount = 0;
		try {
			amount = Integer.parseInt(number);
		}
		catch (NumberFormatException ex) {
			sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a number.");
			return;
		}
		if (amount < 1) {
			sender.sendMessage(ChatColor.RED + "You may not deposit less than 1 gold.");
			return;
		}
		if (countItems(p, Material.GOLD_INGOT, 0) < amount) {
			sender.sendMessage(ChatColor.RED + "You tried to deposit " + amount + " gold, but you only have " + countItems(p, Material.GOLD_INGOT, 0) + ".");
			return;
		}
		economyManager.depositPlayer(p.getUniqueId(), amount);
		remove(p, Material.GOLD_INGOT, amount);
		p.updateInventory();
		p.sendMessage("§7Deposit succesful.");
		p.sendMessage("§7You now have " + FormatUtils.BALANCE_FORMAT.format(economyManager.getBalance(p.getUniqueId())) + " gold in your bank account.");

	}

	private void remove(Player p, Material material, int amount) {
		for (int i = 1; i <= amount; i++) {
			ItemStack ite = p.getInventory().getItem(p.getInventory().first(material));
			ItemStack it = new ItemStack(ite.getType(), 1, ite.getDurability());
			it.setItemMeta(ite.getItemMeta());
			p.getInventory().removeItem(it);
		}
	}
}
