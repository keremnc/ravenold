package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
 
public class KOTHTPCommand {
 
    @Command(names={ "KOTH TP" }, permissionNode="raven.koth")
    public static void kothTP(Player sender, @Parameter(name="KOTH") KOTH target) {
        sender.teleport(target.getCapLocation().toLocation(RavenPlugin.get().getServer().getWorld(target.getWorld())));
        sender.sendMessage(ChatColor.GRAY + "Teleported to the " + target.getName() + " KOTH.");
    }
 
}