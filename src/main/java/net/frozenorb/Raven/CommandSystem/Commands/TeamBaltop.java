package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.Raven.Utilities.FormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Created by Ryan on 6/7/2015
 * <p/>
 * Project: raven
 */
public class TeamBaltop extends BaseCommand {

    public TeamBaltop() {
        super("teambaltop");
    }

    /**
     * Called when the command is executed
     * <p/>
     * Do NOT run any async tasks, the player pointer will change
     */
    @Override
    public void syncExecute() {

    }

    @Override
    public void asyncExecute(CommandSender sender, Command cmd, String label, String[] args) {

        HashMap<String, Double> doubleHashMap = new HashMap<>();

        for (Team team : RavenPlugin.get().getTeamManager().getTeams()) {
            doubleHashMap.put(team.getFriendlyName(), (double) team.getTotalBalance());
        }

        ValComp comp = new ValComp(doubleHashMap);
        TreeMap<String, Double> sorted = new TreeMap<>(comp);
        sorted.putAll(doubleHashMap);

        if (sorted.size() <= 0) {
            sender.sendMessage("§cNo teams found.");
            return;
        }
        sender.sendMessage("§7§m------§r §6Team Baltop §7§m------");
        int i = 0;
        Iterator<Map.Entry<String, Double>> iter = sorted.entrySet().iterator();
        do {
            Map.Entry<String, Double> entry = iter.next();
            int cur = i;
            sender.sendMessage("§7" + (cur + 1) + ". " + entry.getKey() + " - §6" + FormatUtils.BALANCE_FORMAT.format(entry.getValue()));
            i++;
        } while (i < 10 && iter.hasNext());

    }

    private static class ValComp implements Comparator<String> {

        Map<String, Double> base;

        public ValComp(Map<String, Double> base) {
            this.base = base;
        }

        @Override
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
