package net.frozenorb.Raven.Utilities.reflection;

import java.lang.reflect.Field;

/**
 * @author Ryan
 */
public class SafeField<T> {

    Field field;

    SafeClass<?> owner;

    public SafeField(SafeClass<?> owner, String name) {
        this.owner = owner;
        try {
            this.field = this.owner.getClazz().getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    public T get() {
        try {
            field.setAccessible(true);
            return (T) field.get(owner.get());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


}
