package net.frozenorb.Raven.Tasks;

import net.frozenorb.Raven.GameEvents.Tracking.TrackingEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Ryan on 3/31/2015
 * <p/>
 * Project: raven
 */
public class TrackingEventTask extends BukkitRunnable {

    @Override
    public void run() {
        if (TrackingEvent.isStarted()) {
            System.err.println("§cTracking event already in progress.");
        } else {
            TrackingEvent event = new TrackingEvent(null);
            TrackingEvent.setStarted(true);
            event.run();
        }
    }
}
