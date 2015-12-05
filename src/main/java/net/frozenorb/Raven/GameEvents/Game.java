package net.frozenorb.Raven.GameEvents;

import lombok.NonNull;

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
public @interface Game {
    /**
      The name of the Game
     */
    @NonNull String name();

}
