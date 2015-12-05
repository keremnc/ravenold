package net.frozenorb.Raven.Types;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ryan on 5/4/2015
 * <p/>
 * Project: raven
 * <p/>
 * Represents an ItemStack that is tracking kills. Name is not the best, but meh.
 */
public class KillTrackerStack {

    private LinkedList<String> kills = new LinkedList<>();
    private int killCount = 0;

    public KillTrackerStack(int killCount, List<String> kills) {
        this.killCount = killCount;
        this.kills = new LinkedList<>(kills);
    }
    /**
     *
     * @param who - Display name of the killer
     * @param killed - Display name of who got killed
     */
    public void addKill(String who, String killed) {
        if (kills.size() >= 2) {
            kills.pollLast(); // Remove the last "kill"
        }
        kills.push("§f" + who + " §ekilled §f" + killed); // Push the new element to the top of the stack
        killCount++;
    }

    public int getKillCount() {
        return killCount;
    }

    public LinkedList<String> getKills() {
        return kills;
    }
}
