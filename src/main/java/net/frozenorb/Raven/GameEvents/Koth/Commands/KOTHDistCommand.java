package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHDistCommand {
 
    @Command(names={ "KOTH Dist" }, permissionNode="raven.koth.admin")
    public static void kothDist(Player sender, @Parameter(name="KOTH") KOTH target, @Parameter(name="Max Distance") int maxDistance) {
        target.setCapDistance(maxDistance);
        sender.sendMessage(ChatColor.GRAY + "Set max distance for the " + target.getName() + " KOTH.");
    }
 
}