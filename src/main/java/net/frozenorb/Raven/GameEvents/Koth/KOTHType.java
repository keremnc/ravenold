package net.frozenorb.Raven.GameEvents.Koth;

import com.mysql.jdbc.StringUtils;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.qlib.command.ParameterType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KOTHType implements ParameterType<KOTH> {
 
    public KOTH transform(CommandSender sender, String source) {
        KOTH koth = RavenPlugin.get().getKOTHHandler().getKOTH(source);
 
        if (koth == null) {
            sender.sendMessage(ChatColor.RED + "No KOTH with the name " + source + " found.");
            return (null);
        }
 
        return (koth);
    }
 
    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> completions = new ArrayList<>();
 
        for (KOTH koth : RavenPlugin.get().getKOTHHandler().getKOTHs()) {
            if (StringUtils.startsWithIgnoreCase(koth.getName(), source)) {
                completions.add(koth.getName());
            }
        }
 
        return (completions);
    }
 
}