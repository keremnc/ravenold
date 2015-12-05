package net.frozenorb.Raven.GameEvents;

import org.bukkit.event.Listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Ryan on 3/26/2015
 * <p/>
 * Project: raven
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GameEventListener {
    /**
     * This holds information about listeners, if any, for the game
     */
    Class<? extends Listener>[] value();
}
