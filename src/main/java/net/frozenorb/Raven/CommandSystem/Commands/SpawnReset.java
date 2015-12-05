package net.frozenorb.Raven.CommandSystem.Commands;

import java.text.NumberFormat;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.WorldControl.SpawnResetter;
import net.frozenorb.Utilities.Core;
import net.frozenorb.Utilities.Interfaces.Callback;

import org.bukkit.ChatColor;

public class SpawnReset extends BaseCommand {
	private SpawnResetter resetter;
	private long lastCleared;
	private String clearedBy;

	public SpawnReset() {
		super("spawnreset", new String[] { "resetspawn", "clearspawn", "rs" });
		registerSubcommand(new Subcommand("clear", new String[] { "c" }) {

			@Override
			protected void syncExecute() {
				if (resetter != null) {
					sender.sendMessage(ChatColor.RED + "A spawn reset task is in progress.");
					return;
				}
				clearedBy = sender.getName();
				lastCleared = System.currentTimeMillis();
				resetter = new SpawnResetter();
				sender.sendMessage("§e" + NumberFormat.getInstance().format(resetter.populate()) + " blocks will be changed.");
				sender.sendMessage("§e" + resetter.getPassesNeeded() + " passes will be called.");
				resetter.clear(new Callback<String>() {

					@Override
					public void callback(String data) {
						for (String msg : data.split("\n")) {
							sender.sendMessage(msg);
						}
						resetter = null;
					}
				});
			}
		});
		registerSubcommand(new Subcommand("cancel", new String[] { "end", "stop" }) {

			@Override
			protected void syncExecute() {
				if (resetter == null) {
					sender.sendMessage(ChatColor.RED + "No spawn reset tasks are currently running.");
					return;
				}
				if (resetter.cancel()) {
					sender.sendMessage(ChatColor.YELLOW + "Spawn resetter task has been cancelled.");
				} else {
					sender.sendMessage(ChatColor.RED + "Spawn resetter task could not be ended.");
				}
				resetter = null;
			}
		});
		registerSubcommand(new Subcommand("progress", new String[] { "p" }) {

			@Override
			protected void syncExecute() {
				if (resetter == null) {
					sender.sendMessage(ChatColor.RED + "No spawn reset tasks are currently running.");
					return;
				}
				double changed = resetter.getBlocksChanged();
				double toChange = resetter.getBlocksToChange();
				double percent = (changed / toChange) * 100D;
				percent = Math.round(percent * 10) / 10;
				long time = System.currentTimeMillis() - resetter.getTaskStarted();
				String timeStr = Core.get().getConvertedTime(time / 1000);
				sender.sendMessage(ChatColor.YELLOW + "Spawn resetter task has been running for " + timeStr.trim() + ".");
				sender.sendMessage(ChatColor.YELLOW + "" + (int) changed + "/" + (int) toChange + " blocks changed, task at " + percent + "% complete.");
				sender.sendMessage(ChatColor.YELLOW + "Task has done " + resetter.getPasses() + " passes.");
				long l = resetter.getEstimatedFinishTime() - System.currentTimeMillis();
				sender.sendMessage(ChatColor.YELLOW + "Estimated to finish in " + Core.get().getConvertedTime(l / 1000).trim() + ".");
			}
		});
		registerSubcommand(new Subcommand("last", new String[] { "lastclear" }) {

			@Override
			protected void syncExecute() {
				if (clearedBy == null) {
					sender.sendMessage(ChatColor.RED + "Spawn was not cleared since the server started up.");
				} else {
					String when = Core.get().getConvertedTime((System.currentTimeMillis() - lastCleared) / 1000).trim();
					sender.sendMessage(ChatColor.YELLOW + "Spawn was last cleared by " + clearedBy + " " + when + " ago.");
				}
			}
		});
		registerSubcommandsToTabCompletions();
		setPermissionLevel("raven.clearspawn", ChatColor.RED + "You are not allowed to do this.");
	}

	@Override
	public boolean verify() {
		if (!SpawnResetter.isValid()) {
			sender.sendMessage(ChatColor.RED + "The spawn region is not defined.");
			return false;
		}
		return super.verify();
	}

	@Override
	public void syncExecute() {
		sender.sendMessage(ChatColor.YELLOW + "/rs clear - Begins clearing spawn.");
		sender.sendMessage(ChatColor.YELLOW + "/rs cancel - Cancels any spawn clearing tasks.");
		sender.sendMessage(ChatColor.YELLOW + "/rs progress - View the progress of clearing spawn.");
		sender.sendMessage(ChatColor.YELLOW + "/rs last - View the last time spawn was cleared.");
	}

}
