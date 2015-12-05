package net.frozenorb.Raven.Managers;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import lombok.Getter;
import net.frozenorb.Raven.Listeners.GeneralListener;
import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.Raven.Types.Alarm;
import net.frozenorb.Raven.Types.Kit;
import net.frozenorb.Utilities.DataSystem.Regioning.RegionManager;
import net.frozenorb.mShared.Shared;
import net.frozenorb.qlib.uuid.FrozenUUIDCache;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ServerManager {
	private RavenPlugin plugin;
	private boolean serverLocked = false;
	private String lockMessage = "not";
	private HashMap<String, String> teamChat = new HashMap<String, String>();
	private List<String> warpmove = new ArrayList<String>();
	private HashMap<String, Long> deathbans = new HashMap<String, Long>();
	private HashSet<Kit> kits = new HashSet<Kit>();
	private HashMap<String, Long> dontSendMessageUntil = new HashMap<String, Long>();
	private HashMap<String, Long> lastSentSMSAlert = new HashMap<String, Long>();
	@Getter private HashSet<String> usedNames = new HashSet<String>();

	public void save() {

		try {
			File f = new File("usedNames.json");
			if (!f.exists()) {
				f.createNewFile();
			}

			BasicDBObject dbo = new BasicDBObject();
			BasicDBList list = new BasicDBList();

			for (String n : usedNames) {
				list.add(n);
			}

			dbo.put("names", list);
			FileUtils.write(f, new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(dbo.toString())));

		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private static final Method[] ARMOR_METHODS;

	private static int borderDistance = 15_000;

	static {
		ArrayList<Method> methods = new ArrayList<Method>();

		try {
			String[] armor = { "Chestplate", "Leggings" };

			for (String piece : armor) {
				String methodName = "get" + piece;

				Method m = PlayerInventory.class.getMethod(methodName);
				methods.add(m);
			}
		}
		catch (NoSuchMethodException | SecurityException e) {

			e.printStackTrace();
		}

		ARMOR_METHODS = methods.toArray(new Method[] {});

	}

	public static int getBorderDistance() {
		return borderDistance;
	}

	public static void setBorderDistance(int borderDistance) {
		ServerManager.borderDistance = borderDistance;
	}

	public HashSet<Kit> getKits() {
		return kits;
	}

	public HashMap<String, Long> getDeathbans() {
		return deathbans;
	}

	public List<String> getWarpmove() {
		return warpmove;
	}

	public void setWarpmove(List<String> warpmove) {
		this.warpmove = warpmove;
	}

	public ServerManager(RavenPlugin pluin) {
		this.plugin = pluin;
		try {
			File f = new File("usedNames.json");
			if (!f.exists()) {
				f.createNewFile();
			}

			BasicDBObject dbo = (BasicDBObject) JSON.parse(FileUtils.readFileToString(f));

			if (dbo != null) {
				for (Object o : (BasicDBList) dbo.get("names")) {
					usedNames.add((String) o);
				}
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public Location getSpawn(World w) {
		w = Bukkit.getWorld("world");
		Location l = w.getSpawnLocation().add(new Vector(0.5, 1, 0.5));
		l.setWorld(Bukkit.getServer().getWorlds().get(0));
		return l;
	}

	public boolean isServerLocked() {
		return serverLocked;
	}

	public String getLockMessage() {
		return lockMessage;
	}

	public void setServerLocked(boolean serverLocked) {
		this.serverLocked = serverLocked;
	}

	public void setLockMessage(String lockMessage) {
		this.lockMessage = lockMessage;
	}

	public boolean canWarp(Player player) {
		int max = 26;
		List<Entity> nearbyEntities = player.getNearbyEntities(max, max, max);

		if (player.getGameMode() == GameMode.CREATIVE) {
			return true;
		}
		Team warpeeTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());

		for (Entity e : nearbyEntities) {
			if ((e instanceof Player)) {
				Player p = (Player) e;
				if (!p.canSee(player)) {
					return true;
				}
				if (!player.canSee(p)) {
					continue;
				}

				Team team = plugin.getTeamManager().getPlayerTeam(p.getUniqueId());

				if (team == null || warpeeTeam == null) {
					return false;
				}
				if (team != warpeeTeam)
					return false;

				if (team == warpeeTeam)
					continue;

			}
		}

		return true;
	}

	public boolean safeToLogout(Player player) {
		if (RavenPlugin.spawnprot.contains(player.getName()) || player.getGameMode() == GameMode.CREATIVE)
			return true;
		if (!GeneralListener.combatLogRunnables.containsKey(player.getName())) {
			return true;
		}
		int max = 26;
		List<Entity> nearbyEntities = player.getNearbyEntities(max, max, max);
		Team warpeeTeam = plugin.getTeamManager().getPlayerTeam(player.getUniqueId());

		for (Entity e : nearbyEntities) {
			if ((e instanceof Player)) {
				Player p = (Player) e;
				if (RavenPlugin.spawnprot.contains(p.getName())) {
					continue;
				}
				if (!p.canSee(player)) {
					return true;
				}
				if (!player.canSee(p)) {
					continue;
				}

				Team team = plugin.getTeamManager().getPlayerTeam(p.getUniqueId());

				if (team == null || warpeeTeam == null) {
					return false;
				}
				if (team != warpeeTeam)
					return false;

				if (team == warpeeTeam)
					continue;

			}
		}

		return true;

	}

	public void handleRaid(final Player raider) {
		final Player p = raider;
		if (p.hasMetadata("invisible") || p.hasMetadata("modsuite"))
			return;
		Bukkit.getScheduler().runTaskAsynchronously(RavenPlugin.get(), new Runnable() {
			@Override
			public void run() {
				for (Alarm a : RavenPlugin.get().getAlarmManager().getAlarms()) {
					if (a.isRaiding(raider)) {
						@SuppressWarnings("deprecation") Player p = Bukkit.getPlayer(a.getOwner());
						sendRaidSMS(raider, FrozenUUIDCache.name(a.getOwner()));
						if (p != null)
							sendRaidBaseAlert(p, raider);
					}
				}
			}
		});
	}

	private void sendRaidSMS(Player raider, String recipient) {
		if (lastSentSMSAlert.containsKey(recipient) && lastSentSMSAlert.get(recipient) > System.currentTimeMillis())
			return;
		lastSentSMSAlert.put(recipient, System.currentTimeMillis() + 1800000);
		Shared.get().getConnectionManager().sendPost(Shared.get().getConnectionManager().getApiRoot() + "/servercmd/notify/user", "user=" + recipient + "&message=Your base is being raided!&type=raid");

	}

	private void sendRaidBaseAlert(Player recipient, Player raider) {

		if (dontSendMessageUntil.containsKey(recipient.getName())) {
			long l = System.currentTimeMillis();
			if (dontSendMessageUntil.get(recipient.getName()) < l) {

				if (recipient.hasPermission("raven.alarm"))
					recipient.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "A player is raiding your base!");
			}
		} else {

			if (recipient.hasPermission("raven.alarm"))
				recipient.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "A player is raiding your base!");
		}
		dontSendMessageUntil.put(recipient.getName(), System.currentTimeMillis() + 10000);

	}

	/**
	 * Gets if two players are on the same team
	 * 
	 * @param p1
	 *            player1
	 * @param p2
	 *            player2
	 * @return same team
	 */
	public boolean areOnSameTeam(Player p1, Player p2) {
		return areOnSameTeam(p1.getUniqueId(), p2.getUniqueId());

	}

	/**
	 * Gets whether two names are on the same team
	 * 
	 * @param s1
	 *            player1's name
	 * @param s2
	 *            player2's name
	 * @return same team
	 */
	public boolean areOnSameTeam(UUID s1, UUID s2) {
		Team team = plugin.getTeamManager().getPlayerTeam(s1);
		Team warpeeTeam = plugin.getTeamManager().getPlayerTeam(s2);

		if (team == null || warpeeTeam == null) {
			return false;
		}
		if (team != warpeeTeam)
			return false;

		if (team == warpeeTeam)
			return true;
		return false;

	}

	/**
	 * Gets if the player should be deathbanned or not in the End.
	 * 
	 * @param player
	 *            the player to check
	 * @return deathban or not
	 */
	public boolean shouldDeathban(Player player) {
		PlayerInventory pi = player.getInventory();
		try {
			for (Method m : ARMOR_METHODS) {

				ItemStack item = (ItemStack) m.invoke(pi);
				if (item == null) {
					return true;
				}

				String name = item.getType().name();

				if (!(name.contains("IRON_") || name.contains("DIAMOND_"))) {
					return true;
				}
			}
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Gets if the location's region is outer spawn
	 * 
	 * @param loc
	 *            the location to check
	 * @return outer spawn
	 */
	public boolean isOuterSpawn(Location loc) {
		return RegionManager.get().hasTag(loc, "outer-spawn");

	}

	/**
	 * Gets if the location's region is spawn
	 * 
	 * @param loc
	 *            the location to check
	 * @return spawn
	 */
	public boolean isSpawn(Location loc) {
		return RegionManager.get().hasTag(loc, "spawn");
	}

	/**
	 * Gets if blow flow should be prevented on the block
	 * 
	 * @param b
	 *            the block to check
	 * @return prevent flow
	 */
	public boolean shouldPreventFlow(Block b) {
		return !b.hasMetadata("flow");
	}

	/**
	 * Gets the team chat map used for handling team chat
	 * 
	 * @return team chat map
	 */
	public HashMap<String, String> getTeamChatMap() {
		return teamChat;
	}

	/**
	 * Sets the team chat map used for handling team chat
	 * 
	 * @param tchat
	 *            teamchat map to use
	 */
	public void setTeamChatMap(HashMap<String, String> tchat) {
		this.teamChat = tchat;
	}

	/**
	 * Disables a player from attacking for 10 seconds
	 * 
	 * @param p
	 *            the player to disable
	 */
	public void disablePlayerAttacking(final Player p) {
		p.sendMessage(ChatColor.GRAY + "You cannot attack for 10 seconds.");
		final Listener l = new Listener() {
			@EventHandler
			public void onPlayerDamage(EntityDamageByEntityEvent e) {
				if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
					if (((Player) e.getDamager()).getName().equals(p.getName())) {
						e.setCancelled(true);
					}
				}

			}
		};
		Bukkit.getPluginManager().registerEvents(l, RavenPlugin.get());
		Bukkit.getScheduler().runTaskLater(RavenPlugin.get(), new Runnable() {
			public void run() {
				HandlerList.unregisterAll(l);
			}
		}, 10 * 20);
	}

}
