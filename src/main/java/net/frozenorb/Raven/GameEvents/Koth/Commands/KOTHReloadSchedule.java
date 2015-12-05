package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHReloadSchedule {
 
    @Command(names={ "KOTHSchedule Reload" }, permissionNode="raven.koth.admin")
    public static void kothReloadSchedule(Player sender) {
        RavenPlugin.get().getKOTHHandler().loadSchedules();
        sender.sendMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.YELLOW + "Reloaded the KOTH schedule.");
    }
 
}