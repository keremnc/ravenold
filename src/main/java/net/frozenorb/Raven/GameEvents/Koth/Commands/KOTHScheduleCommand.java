package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.Raven.GameEvents.Koth.KOTHScheduledTime;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
 
public class KOTHScheduleCommand {
 
    // Make this pretty.
    @Command(names={ "KOTH Schedule" }, permissionNode="")
    public static void kothSchedule(Player sender) {
        int sent = 0;
 
        for (Map.Entry<KOTHScheduledTime, String> entry : RavenPlugin.get().getKOTHHandler().getKOTHSchedule().entrySet()) {
            KOTH resolved = RavenPlugin.get().getKOTHHandler().getKOTH(entry.getValue());
 
            if (resolved == null || resolved.isHidden()) {
                continue;
            }
 
            sent++;
            sender.sendMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.YELLOW + entry.getValue() + ChatColor.GOLD + " can be captured at " + ChatColor.BLUE + DateFormat.getTimeInstance(DateFormat.SHORT).format(entry.getKey().toDate()) + ChatColor.GOLD + ".");
        }
 
        if (sent == 0) {
            sender.sendMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.RED + "KOTH Schedule: " + ChatColor.YELLOW + "Undefined");
        } else {
            sender.sendMessage(ChatColor.GOLD + "[KingOfTheHill] " + ChatColor.YELLOW + "It is currently " + ChatColor.BLUE + DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()) + ChatColor.GOLD + ".");
        }
    }
 
}