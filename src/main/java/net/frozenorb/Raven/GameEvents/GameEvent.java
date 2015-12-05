package net.frozenorb.Raven.GameEvents;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/**
 * Created by Ryan on 3/26/2015
 * <p/>
 * Project: raven
 */
public abstract class GameEvent {

    Player host;

    public GameEvent(@Nullable Player host) {
        this.host = host;
    }

    public abstract void run();

}
