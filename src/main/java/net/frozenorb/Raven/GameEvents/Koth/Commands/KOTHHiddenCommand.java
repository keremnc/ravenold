package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHHiddenCommand {
 
    @Command(names={ "KOTH Hidden" }, permissionNode="raven.koth.admin")
    public static void kothHidden(Player sender, @Parameter(name="KOTH") KOTH target, @Parameter(name="Hidden") boolean hidden) {
        target.setHidden(hidden);
        sender.sendMessage(ChatColor.GRAY + "Set visibility for the " + target.getName() + " KOTH.");
    }
 
}