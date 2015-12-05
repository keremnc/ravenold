package net.frozenorb.Raven.Managers;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Team.Team;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {
	private volatile ConcurrentHashMap<String, Team> teamNameMap = new ConcurrentHashMap<>();
	private volatile ConcurrentHashMap<UUID, Team> playerTeamMap = new ConcurrentHashMap<>();

	public TeamManager(JavaPlugin plugin) {
		loadTeams();
	}

	public ArrayList<Team> getTeams() {
		return new ArrayList<>(teamNameMap.values());
	}

	public void setTeam(UUID playerName, Team team) {
		playerTeamMap.put(playerName, team);
	}

	public Team getTeam(String teamName) {
		return teamNameMap.get(teamName.toLowerCase());
	}

	private void loadTeams() {
		if (!RavenPlugin.syncRedis.isConnected()) {
			RavenPlugin.syncRedis.connect();
		}
		for (String key : RavenPlugin.syncRedis.keys("teams.*")) {
			String str = RavenPlugin.syncRedis.get(key);
			Team team = new Team(key.split("\\.")[1]);
			team.load(str);
			teamNameMap.put(team.getName().toLowerCase(), team);
			for (UUID member : team.getMembers()) {
				playerTeamMap.put(member, team);
			}
		}

	}

	public Team getPlayerTeam(UUID name) {
		if (!playerTeamMap.containsKey(name))
			return null;

		return playerTeamMap.get(name);
	}

	public boolean teamExists(String teamName) {
		return teamNameMap.containsKey(teamName.toLowerCase());
	}

	public void addTeam(net.frozenorb.Raven.Team.Team team) {
		team.setChanged(true);
		teamNameMap.put(team.getName().toLowerCase(), team);

		for (UUID member : team.getMembers()) {
			playerTeamMap.put(member, team);
		}
	}

	public void removePlayerFromTeam(UUID name) {
		playerTeamMap.remove(name);
	}

	public boolean isOnTeam(UUID name) {
		return playerTeamMap.containsKey(name);
	}

	public void removeTeam(final String name) {
		if (teamExists(name)) {
			Team t = getTeam(name);
			for (UUID names : t.getMembers())
				removePlayerFromTeam(names);
		}
		teamNameMap.remove(name.toLowerCase());

		Bukkit.getScheduler().runTaskAsynchronously(RavenPlugin.get(), new Runnable() {

			@Override
			public void run() {
				Jedis j = new Jedis("localhost");
				j.del("teams." + name.toLowerCase());
			}
		});
	}
}