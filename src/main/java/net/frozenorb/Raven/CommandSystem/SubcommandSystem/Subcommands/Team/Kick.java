package net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommands.Team;

import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.Events.TeamEvent;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Kick extends Subcommand {

    public Kick(String name, String errorMessage, String... aliases) {
        super(name, errorMessage, aliases);
    }

    @Override
    public void syncExecute() {
        final Player p = (Player) sender;

        if (args.length == 2) {

            String name = args[1];
            UUID id = FrozenUUIDCache.uuid(name);

            if (id == null) {
                p.sendMessage(ChatColor.RED + "Could not find player '" + name + "'.");
                return;
            }

            if (Bukkit.getPlayer(id) != null) {
                name = Bukkit.getPlayer(args[1]).getName();
            }
            net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());
            if (team == null) {
                sender.sendMessage(ChatColor.GRAY + "You are not on a team!");
                return;
            }
            if (team.isOwner(p.getUniqueId())) {
                if (team.isOnTeam(id)) {
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        if (team.isOnTeam(pl)) {
                            pl.sendMessage(ChatColor.DARK_AQUA + name + " was kicked by " + p.getName() + "!");
                        }
                    }

                    if (team.remove(id))
                        RavenPlugin.get().getTeamManager().removeTeam(team.getName());
                    RavenPlugin.get().getTeamManager().removePlayerFromTeam(id);
                    Bukkit.getPluginManager().callEvent(new TeamEvent(Bukkit.getPlayer(id), team)); // should not be null

                } else {
                    p.sendMessage(ChatColor.DARK_AQUA + "Player is not on your team.");
                }
            } else
                p.sendMessage(ChatColor.DARK_AQUA + "Only team managers can do this.");
        } else {
            sendErrorMessage();
        }

    }

    @Override
    public List<String> tabComplete() {
        net.frozenorb.Raven.Team.Team team = RavenPlugin.get().getTeamManager().getPlayerTeam(((Player) sender).getUniqueId());
        if (team == null) {
            return super.tabComplete();
        }

        List<String> options = new ArrayList<>();

        for (UUID id : team.getMembers()) {
            options.add(FrozenUUIDCache.name(id));
        }

        return options;
    }
}
