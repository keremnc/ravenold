package net.frozenorb.Raven.GameEvents.Koth.Commands;

import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.command.Command;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class KOTHListCommand {
 
    @Command(names={ "KOTH List" }, permissionNode="raven.koth")
    public static void kothList(Player sender) {
        for (KOTH koth : RavenPlugin.get().getKOTHHandler().getKOTHs()) {
            sender.sendMessage((koth.isHidden() ? ChatColor.DARK_GRAY + "[H] " : "") + (koth.isActive() ? ChatColor.GREEN : ChatColor.RED) + koth.getName() + ChatColor.WHITE + " - " + ChatColor.GRAY + koth.getPercentage() + ChatColor.DARK_GRAY + "%" + " " + ChatColor.WHITE + "- " + ChatColor.GRAY + (koth.getCurrentCapper() == null ? "None" : koth.getCurrentCapper()) + (koth.isHidden() ? "" : ChatColor.WHITE + " - " + ChatColor.GRAY + "Level " + koth.getLevel()));
        }
    }
 
}