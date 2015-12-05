package net.frozenorb.Raven.CommandSystem.Commands;

import net.frozenorb.Raven.CommandSystem.BaseCommand;
import net.frozenorb.Raven.CommandSystem.SubcommandSystem.Subcommand;
import net.frozenorb.Raven.GameEvents.Tracking.TrackingEvent;

/**
 * Created by Ryan on 3/26/2015
 * <p/>
 * Project: raven
 */
public class TrackEvent extends BaseCommand {

    public TrackEvent() {
        super("trackevent");
        registerSubcommand(new Subcommand("start") {
            @Override
            protected void syncExecute() {
                if (TrackingEvent.isStarted()) {
                    sender.sendMessage("§cTracking event already in progress.");
                } else {
                    TrackingEvent event = new TrackingEvent(null);
                    TrackingEvent.setStarted(true);
                    event.run();
                    sender.sendMessage("§aTracking event started");
                }

            }
        });

        registerSubcommand(new Subcommand("end") {
            @Override
            protected void syncExecute() {
                if(!TrackingEvent.isStarted()) {
                    sender.sendMessage("§cNo tracking event in progress");
                } else {
                    TrackingEvent.setStarted(false);
                    sender.sendMessage("§cTracking event ended");
                }
            }
        });
        setPermissionLevel("raven.trackevent", "&cYou do not have permission.");
    }

    /**
     * Called when the command is executed
     * <p/>
     * Do NOT run any async tasks, the player pointer will change
     */
    @Override
    public void syncExecute() {

    }
}
