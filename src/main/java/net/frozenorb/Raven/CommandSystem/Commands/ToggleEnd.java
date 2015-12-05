package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.GameEvents.Koth.KOTH;
import net.frozenorb.Raven.RavenPlugin;
import org.bukkit.ChatColor;

public class ToggleEnd extends BaseCommand {

    public static boolean enabled = false;

    public ToggleEnd() {
        super("endevent");

        registerSubcommand(new Subcommand("start", new String[]{"begin"}) {
            @Override
            protected void syncExecute() {
                enabled = true;
                sender.sendMessage(ChatColor.YELLOW + "End enabled: " + enabled);
                if (RavenPlugin.get().getKOTHHandler().getKOTH("End") != null) {
                    KOTH koth = RavenPlugin.get().getKOTHHandler().getKOTH("End");
                    koth.activate();
                } else if (RavenPlugin.get().getKOTHHandler().getKOTH("EndEvent") != null) {
                    KOTH koth = RavenPlugin.get().getKOTHHandler().getKOTH("EndEvent");
                    koth.activate();
                } else {
                    sender.sendMessage("§cThe End Event does not appear to be setup properly!");
                }
            }
        });


        registerSubcommand(new Subcommand("stop", new String[]{"end"}) {
            @Override
            protected void syncExecute() {
                enabled = false;
                sender.sendMessage(ChatColor.YELLOW + "End enabled: " + enabled);
                if (RavenPlugin.get().getKOTHHandler().getKOTH("End") != null) {
                    KOTH koth = RavenPlugin.get().getKOTHHandler().getKOTH("End");
                    koth.deactivate();
                } else if (RavenPlugin.get().getKOTHHandler().getKOTH("EndEvent") != null) {
                    KOTH koth = RavenPlugin.get().getKOTHHandler().getKOTH("EndEvent");
                    koth.deactivate();
                }
            }
        });

        registerSubcommandsToTabCompletions();

        setPermissionLevel("op", "§cYou are not allowed to do this.");
    }

    @Override
    public void syncExecute() {
        sender.sendMessage("§c/endevent [start | stop]");

    }

}
