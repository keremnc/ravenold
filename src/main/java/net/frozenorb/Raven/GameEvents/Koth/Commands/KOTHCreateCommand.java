package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHCreateCommand {
 
    @Command(names={ "KOTH Create" }, permissionNode="raven.koth.admin")
    public static void kothCreate(Player sender, @Parameter(name="KOTH") String target) {
        new KOTH(target, sender.getLocation());
        sender.sendMessage(ChatColor.GRAY + "Created a KOTH named " + target + ".");
    }
 
}