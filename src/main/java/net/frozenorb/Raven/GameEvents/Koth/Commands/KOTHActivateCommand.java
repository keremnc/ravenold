package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHActivateCommand {
 
    @Command(names={ "KOTH Activate", "KOTH Active" }, permissionNode="raven.koth")
    public static void kothActivate(Player sender, @Parameter(name="KOTH") KOTH target) {
        for (KOTH koth : RavenPlugin.get().getKOTHHandler().getKOTHs()) {
            if (koth.isActive() && !koth.isHidden()) {
                sender.sendMessage(ChatColor.RED + "Another KOTH (" + koth.getName() + ") is already active.");
                return;
            }
        }
 
        target.activate();
        sender.sendMessage(ChatColor.GRAY + "Activated " + target.getName() + ".");
    }
 
}