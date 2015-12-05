package net.frozenorb.Raven.Utilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Ryan on 7/1/2015
 * <p/>
 * Project: raven
 */
public class ItemUtils {

    public static ItemStack constructItem(Material material) {
        return new ItemStack(material);
    }

    public static void pop(Player player, ItemStack heldItem) {
        if (heldItem.getAmount() == 1) {
            player.getInventory().setItemInHand(constructItem(Material.AIR));
            player.updateInventory();
        } else {
            heldItem.setAmount(heldItem.getAmount() - 1);
            player.setItemInHand(heldItem);
            player.updateInventory();
        }
    }

    public static void push(Player player, int slot, ItemStack item) {
        if (player.getInventory().contains(item.getType())) {
            player.getInventory().addItem(item);
        } else {
            player.getInventory().setItem(slot, item);
        }
    }

}
