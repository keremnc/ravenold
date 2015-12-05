package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
 
public class KOTHLevelCommand {
 
    @Command(names={ "KOTH Level" }, permissionNode="raven.koth")
    public static void kothLevel(Player sender, @Parameter(name="KOTH") KOTH target, @Parameter(name="Tier") int level) {
        target.setLevel(level);
        sender.sendMessage(ChatColor.GRAY + "Set level for the " + target.getName() + " KOTH.");
    }
 
}