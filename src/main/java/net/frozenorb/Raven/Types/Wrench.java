package net.frozenorb.Raven.Types;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Created by Ryan on 6/30/2015
 * <p/>
 * Project: raven
 */
public class Wrench {

    public ItemStack get() {
        ItemStack stack = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("§b§lWrench");
        stack.setItemMeta(meta);
        stack.addUnsafeEnchantment(Enchantment.DURABILITY, 5);
        return stack;
    }

    public static boolean isWrench(ItemStack stack) {
        return stack.getType() == Material.DIAMOND_HOE
                && hasEnchantment(stack, Enchantment.DURABILITY, 5)
                && stack.hasItemMeta()
                && stack.getItemMeta().hasDisplayName()
                && stack.getItemMeta().getDisplayName().equalsIgnoreCase("§b§lWrench");
    }


    private static boolean hasEnchantment(ItemStack itemStack, Enchantment enchantment, int level) {
        boolean found = false;
        for (Map.Entry<Enchantment, Integer> enchants : itemStack.getEnchantments().entrySet()) {
            if (enchants.getKey().equals(enchantment) && enchants.getValue() == level) {
                found = true;
            }
        }
        return found;
    }

}
