package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetHQ extends Subcommand {

    public SetHQ(String name, String errorMessage, String... aliases) {
        super(name, errorMessage, aliases);
    }

    @Override
    public void syncExecute() {
        Player p = (Player) sender;

        net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());

        if (team != null) {
            if (team.isOwner(p.getUniqueId())) {
                // Team  can set passwords
                Location loc = ((Player) sender).getLocation();
                if ((Math.abs(loc.getX()) < 512.0D) && (Math.abs(loc.getZ()) < 512.0D)) {
                    if (loc.getWorld().getEnvironment() != Environment.NETHER) {
                        sender.sendMessage(ChatColor.RED + "You cannot set warps within 512 blocks of spawn.");
                        return;
                    }
                }
                if (loc.getWorld().getEnvironment() == Environment.THE_END) {
                    sender.sendMessage(ChatColor.RED + "You cannot set warps in the end!");
                    return;
                }

                team.setHQ(p.getLocation());

                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (team.isOnTeam(pl)) {
                        pl.sendMessage(ChatColor.ITALIC + "" + ChatColor.DARK_AQUA + p.getName() + " has updated the team's HQ point!");
                    }
                }

                p.sendMessage(ChatColor.DARK_AQUA + "Headquarters Set");
                return;

            } else if (team.isOnTeam(p)) {
                sender.sendMessage(ChatColor.DARK_AQUA + "Only team managers can do this.");
                return;
            }
        } else {
            sender.sendMessage(ChatColor.GRAY + "You are not on a team!");
        }

    }

    @Override
    public List<String> tabComplete() {
        return new ArrayList<String>();
    }

}
