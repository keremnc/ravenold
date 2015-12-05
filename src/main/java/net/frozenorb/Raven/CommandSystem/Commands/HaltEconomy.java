package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;

public class HaltEconomy extends BaseCommand {
	public static boolean halted = false;

	public HaltEconomy() {
		super("halteconomy");
		setPermissionLevel("op", "");
	}

	@Override
	public void syncExecute() {
		halted = !halted;
		sender.sendMessage("§eEconomy halted: " + halted);
	}

}
