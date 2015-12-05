package net.frozenorb.Raven.Utilities;

import net.frozenorb.Raven.Types.XPBottle;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Ryan on 5/29/2015
 * <p/>
 * Project: raven
 */
public class XPUtils {


    public static int getExpAtLevel(final int level) {
        if (level > 29) {
            return 62 + (level - 30) * 7;
        }
        if (level > 15) {
            return 17 + (level - 15) * 3;
        }
        return 17;
    }

    public static int getExpToLevel(final int level) {
        int currentLevel = 0;
        int exp = 0;

        while (currentLevel < level) {
            exp += getExpAtLevel(currentLevel);
            currentLevel++;
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }


    public static ItemStack createXPBottle(int level) {
        return new XPBottle(getExpToLevel(level)).create();
    }

}
