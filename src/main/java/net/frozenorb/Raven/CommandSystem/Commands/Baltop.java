package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.Utilities.FormatUtils;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Created by Ryan on 5/10/2015
 * <p/>
 * Project: raven
 */
public class Baltop extends BaseCommand {
    // Stores the recent requested baltop that occurred before the cacheTime has passed (30 minutes)
    HashMap<String, Integer> cache = new HashMap<>();
    long cacheTime = 1800000;
    long lastRequested = 0L;

    public Baltop() {
        super("baltop");
    }


    /**
     * Called when the command is executed
     * <p/>
     * Do NOT run any async tasks, the player pointer will change
     */
    @Override
    public void syncExecute() {
        // NO-OP
    }

    @Override
    public void asyncExecute(CommandSender sender, Command cmd, String label, String[] args) {
        Map<UUID, Double> doubleHashMap = Basic.get().getUuidEconomyAccess().getBalances();
        ValComp comp = new ValComp(doubleHashMap);
        TreeMap<UUID, Double> sorted = new TreeMap<>(comp);
        sorted.putAll(doubleHashMap);

        if (sorted.size() <= 0) {
            sender.sendMessage("§cNo players found!");
            return;
        }
        sender.sendMessage("§7§m------§r §6Player Baltop §7§m------");
        int i = 0;
        Iterator<Map.Entry<UUID, Double>> iter = sorted.entrySet().iterator();
        do {
            Map.Entry<UUID, Double> entry = iter.next();
            int cur = i;
            sender.sendMessage("§7" + (cur + 1) + ". " + FrozenUUIDCache.name(entry.getKey()) + " - §6" + FormatUtils.BALANCE_FORMAT.format(entry.getValue()));
            i++;
        } while (i < 10 && iter.hasNext());
    }

    private static class ValComp implements Comparator<UUID> {

        Map<UUID, Double> base;

        public ValComp(Map<UUID, Double> base) {
            this.base = base;
        }

        @Override
        public int compare(UUID a, UUID b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
