package net.frozenorb.Raven.Tasks;

import java.util.HashMap;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Listeners.ScoreboardBalanceListener;
import net.frozenorb.Raven.Types.PlayerScoreboardUpdater;
import net.frozenorb.mBasic.Basic;
import net.frozenorb.mCommon.Common;
import net.frozenorb.mCommon.Types.User;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Class that sets a scoreboard of a player
 * 
 * @author Kerem
 * @since 10/19/2013
 * 
 */
@Deprecated
public class ScoreboardTask extends BukkitRunnable {
	private static boolean name = true;
	private static boolean initialized;

	private HashMap<String, PlayerScoreboardUpdater> scoreboardUpdaters = new HashMap<String, PlayerScoreboardUpdater>();
	private int second = 0;

	/**
	 * Creates and returns a new {@link PlayerScoreboardUpdater} instance
	 * 
	 * @param p
	 *            the player to create the scoreboard updater for
	 * @return the instance created
	 */
	public PlayerScoreboardUpdater createNewUpdater(Player p) {
		scoreboardUpdaters.remove(p.getName());
		PlayerScoreboardUpdater psu = new PlayerScoreboardUpdater(p);
		scoreboardUpdaters.put(p.getName(), psu);
        if (p.getScoreboard() == null) {
            psu.loadScoreboard(p);
        }
        psu.update();
		return psu;
	}

	/**
	 * Gets the updater for the player if one exists.
	 * 
	 * @param p
	 *            the player to get the updater for
	 * @return updater if exists, null if not existent
	 */
	public PlayerScoreboardUpdater getUpdater(Player p) {
		if (scoreboardUpdaters.containsKey(p.getName())) {
			return scoreboardUpdaters.get(p.getName());
		}
		return null;
	}

	/**
	 * Removes the player's updater
	 * 
	 * @param p
	 *            the player to remove
	 */
	public void removeUpdater(Player p) {
		scoreboardUpdaters.remove(p.getName());
	}

	/**
	 * Gets whether the player name or 'MCTeams' is being displayed on the title
	 * 
	 * @return player name
	 */
	public static boolean isDisplayingPlayerName() {
		return name;
	}

	@Override
	public void run() {
		if (!initialized) {
			initialized = true;
			Basic.get().getUuidEconomyAccess().registerListener(new ScoreboardBalanceListener());
			System.out.println("Registered economy listener to mBasic.");
		}
		second++;
		if (second % 10 == 0) {
			name = !name;
		}
		for (Player p : Bukkit.getOnlinePlayers()) {
			updatePlayer(p);
		}
	}

	/**
	 * Updates a player after one tick, so calling it in events that the
	 * scoreboard depends on the cancellation (or uncancellation) of the event
	 * can access it's data better
	 * 
	 * @param p
	 *            the player to update
	 */
	public void updatePlayerLater(final Player p) {
		Bukkit.getScheduler().runTaskLater(RavenPlugin.get(), new Runnable() {
			@Override
			public void run() {
				updatePlayer(p);
			}
		}, 1L);
	}

	/**
	 * Updates a player's scoreboard
	 * 
	 * @param p
	 *            the player to update
	 */
	public void updatePlayer(Player p) {

		User profile = Common.get().getUserManager().getUser(p);
		if (!(profile.getServerData().containsField("hud") && profile.getServerData().getBoolean("hud"))) {
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			return;
		}
		if (getUpdater(p) == null) {
			createNewUpdater(p);
		}
		PlayerScoreboardUpdater data = getUpdater(p);
		if (!data.isInitialized()) {
			data.loadScoreboard(p);
		}
		data.update();

	}
}
