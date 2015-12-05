package net.frozenorb.Raven.Utilities;

/**
 * Created by Ryan on 6/26/2015
 * <p/>
 * Project: raven
 */
public class MathUtils {

    public static int random(int min, int max) {
        int range = max - min;
        return (int) Math.round((min + (Math.random() * range)));
    }

    public static boolean isInteger(double n, double tolerance) {
        double absN = Math.abs(n);
        return Math.abs(absN - Math.round(absN)) <= tolerance;
    }
}
