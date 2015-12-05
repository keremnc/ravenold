package net.frozenorb.Raven.GameEvents.Tracking;

import org.bukkit.Location;

/**
 * Created by Ryan on 3/26/2015
 * <p/>
 * Project: raven
 *
 * this just stores the location of the center of the triangle.
 */
public class TrackingEventManager {

    static Location location;

    public static void setLocation(Location location) {
        TrackingEventManager.location = location;
    }

    public static Location getLocation() {
        return location;
    }
}
