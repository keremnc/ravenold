package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHDeactivateCommand {
 
    @Command(names={ "KOTH Deactivate", "KOTH Inactive" }, permissionNode="raven.koth")
    public static void kothDectivate(Player sender, @Parameter(name="KOTH") KOTH target) {
        target.deactivate();
        sender.sendMessage(ChatColor.GRAY + "Deactivated " + target.getName() + " KOTH.");
    }
 
}