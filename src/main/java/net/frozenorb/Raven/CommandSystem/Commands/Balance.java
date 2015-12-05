package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.Utilities.FormatUtils;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mBasic.EconomySystem.UUID.UUIDEconomyAccess;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Balance extends BaseCommand {

	public Balance() {
		super("balance", new String[] { "bal", "$", "dinero" });
	}

	@Override
	public void syncExecute() {

		if (HaltEconomy.halted) {
			sender.sendMessage(ChatColor.RED + "Economy is currently disabled!");
			return;
		}

		UUIDEconomyAccess economyManager = Basic.get().getUuidEconomyAccess();
		if (sender.hasPermission("raven.balothers") && args.length > 0) {
			UUID id = FrozenUUIDCache.uuid(args[0]);
			if (id == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player '" + args[0] + "'.");
				return;
			}
			sender.sendMessage("§6" + FrozenUUIDCache.name(id) + "'s gold: §f" + FormatUtils.BALANCE_FORMAT.format(economyManager.getBalance(id)));
		} else {
			sender.sendMessage("§7You have " + FormatUtils.BALANCE_FORMAT.format(economyManager.getBalance(((Player) sender).getUniqueId())) + " gold in your bank account.");
		}
	}

}
