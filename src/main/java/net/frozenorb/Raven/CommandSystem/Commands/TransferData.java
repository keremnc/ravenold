package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import org.bukkit.ChatColor;

public class TransferData extends BaseCommand {

    public TransferData() {
        super("transferwarps", "xferdata");
        setPermissionLevel("raven.transferdata", "§cYou are not allowed to do this.");
    }

    @Override
    public void syncExecute() {
        if (args.length > 1) {
             sender.sendMessage("THIS COMMAND IS DEPRECATED! #FINALLY_ON_UUIDS");
        } else {
            sender.sendMessage(ChatColor.RED + "/xferdata <p1> <p1>");
        }
    }
}