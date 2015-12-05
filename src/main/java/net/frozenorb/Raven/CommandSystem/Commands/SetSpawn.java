package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.SpawnCommand;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.CommandSystem.BaseCommand;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NullConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class SetSpawn extends BaseCommand {

	public SetSpawn() {
		super("setspawn");
	}

	@Override
	public void syncExecute() {
		if (sender.isOp()) {
			ConversationFactory factory = new ConversationFactory(RavenPlugin.get()).withModality(true).withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {

				public String getPromptText(ConversationContext context) {
					return "§aAre you sure you want to set spawn here? Type §byes§a to confirm or §cno§a to quit.";
				}

				@Override
				public Prompt acceptInput(ConversationContext cc, String s) {
					if (s.equalsIgnoreCase("yes")) {
						Location l = ((Player) cc.getForWhom()).getLocation();
						cc.getForWhom().sendRawMessage(ChatColor.GREEN + "Spawn set!");
						((Player) cc.getForWhom()).getWorld().setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
						if (l.getWorld().getEnvironment() == World.Environment.THE_END) {
							SpawnCommand.endSpawn = l;
						}
						return Prompt.END_OF_CONVERSATION;
					}
					if (s.equalsIgnoreCase("no")) {
						cc.getForWhom().sendRawMessage(ChatColor.GREEN + "Spawn setting cancelled.");
						return Prompt.END_OF_CONVERSATION;

					}
					cc.getForWhom().sendRawMessage(ChatColor.GREEN + "Unrecognized response. Type §b/yes§a to confirm or §c/no§a to quit.");
					return Prompt.END_OF_CONVERSATION;
				}

			}).withEscapeSequence("/no").withTimeout(10).thatExcludesNonPlayersWithMessage("Go away evil console!");
			Conversation con = factory.buildConversation((Player) sender);
			((Player) sender).beginConversation(con);
		}
	}
}
