package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.command.Command;
import net.frozenorb.qlib.command.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHDeleteCommand {
 
    @Command(names={ "KOTH Delete" }, permissionNode="raven.koth.admin")
    public static void kothDelete(Player sender, @Parameter(name="KOTH") KOTH target) {
        RavenPlugin.get().getKOTHHandler().getKOTHs().remove(target);
        RavenPlugin.get().getKOTHHandler().saveKOTHs();
        sender.sendMessage(ChatColor.GRAY + "Deleted KOTH " + target.getName() + ".");
    }
 
}