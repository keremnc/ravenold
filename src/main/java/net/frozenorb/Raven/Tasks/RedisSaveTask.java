package net.frozenorb.Raven.Tasks;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.EconomySystem.Economy;
import net.frozenorb.Raven.Team.Team;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import redis.clients.jedis.Jedis;

public class RedisSaveTask extends BukkitRunnable {

	@Override
	public void run() {
		RavenPlugin rpl = RavenPlugin.get();
		System.out.println("Starting redis save task!");
		Jedis j = new Jedis("localhost");
		j.connect();
		rpl.getTrapManager().saveAllData(j);

		int done = rpl.getWarpManager().saveAllWarps(j);
		int teams = 0;
		for (Team t : rpl.getTeamManager().getTeams()) {
			if (t.hasChanged()) {
				t.save(j);
				teams += 1;
			}
		}
		rpl.getAlarmManager().saveAllData(j);
		Economy.getInstance().saveUsingJedis(j);
		j.disconnect();
		System.out.println(ChatColor.RED + "Saved " + teams + " teams and " + done + " warps to redis!");

	}
}
