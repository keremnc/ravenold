package net.frozenorb.Raven.GameEvents.Koth;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Team.Team;
import net.frozenorb.Raven.Utilities.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.TimerTask;

/**
 * Created by Ryan on 6/12/2015
 * <p/>
 * Project: raven
 *  Ticks the percentage of a player.
 */
public class KOTHPercentage extends TimerTask {

    private String player;

    private double percentage;

    private boolean increasing = true;

    private boolean running = true;

    private boolean canSendMessage = true;

    int runs = 0;

    private KOTH parent;


    public KOTHPercentage(KOTH parent, String player, double percentage) {
        this.percentage = percentage;
        this.player = player;
        this.parent = parent;
    }

    private void tick() {
       if (isRunning()) {
           if (increasing) { // if it is increasing, they are capping
               // Increase the percentage
               if (percentage < 100) {
                   percentage += 0.1;
               }

               int percentInt = Double.valueOf(percentage).intValue();
               if (percentInt != 0 && percentInt % 10 == 0 && MathUtils.isInteger(percentage, 1e-2)) {
                   canSendMessage = false;
                  if (parent.getCurrentCapper() != null && parent.getCurrentCapper().equalsIgnoreCase(player)) {
                      Player p = Bukkit.getPlayer(getPlayer());
                      if (p != null) {
                          String teamPrefix = "§6[§e-§6]";

                          if (RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId()) != null) {
                              Team t = RavenPlugin.get().getTeamManager().getPlayerTeam(p.getUniqueId());
                              teamPrefix = "§6[§e" + t.getFriendlyName() + "§6]";
                          }

                          String prefix = (parent.getName().equalsIgnoreCase("End") || parent.getName().equalsIgnoreCase("EndEvent"))
                                  ?  "§9[End Event]" : "§6[KingOfTheHill]";
//                          p.sendMessage(prefix + " §eAttempting to control " + (parent.getName().equalsIgnoreCase("End") || parent.getName().equalsIgnoreCase("EndEvent") ? "§9The End" : "§9" +
//                                  parent.getName()) + " §6[§9" + getPercentage() + "% Captured§6]§e.");
                          Bukkit.getServer().broadcastMessage(prefix + " " + teamPrefix + "§r" +  p.getDisplayName() + " §eis attempting to capture " +
                                          (parent.getName().equalsIgnoreCase("End") || parent.getName().equalsIgnoreCase("EndEvent") ? "§9The End" : "§9" + parent.getName()) +
                                  " §6[§9" + percentInt + "% Captured§6]§e.");
                      }
                  }
               }

           } else {
               if (percentage < 1.0) {
                   percentage = 0;
               } else if (percentage > 1.0) {
                   percentage -= 1.0;
               }
           }
       }
        runs++;

        if (runs >= 10) {
            runs = 0;
            setCanSendMessage(true);
        }
    }

    @Override
    public void run() {
        tick();
    }


    public void setIncreasing(boolean increasing) {
        this.increasing = increasing;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getPlayer() {
        return player;
    }

    public boolean isCompleted() {
        return percentage >= 100;
    }

    public boolean isIncreasing() {
        return increasing;
    }

    // shitty work around
    public void setCanSendMessage(boolean canSendMessage) {
        this.canSendMessage = canSendMessage;
    }

    public boolean canSendMessage() {
        return canSendMessage;
    }

    @Override
    public boolean cancel() {
        this.running = false;
        return super.cancel();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }


}
