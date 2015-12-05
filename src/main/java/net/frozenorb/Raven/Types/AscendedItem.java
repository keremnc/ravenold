package net.frozenorb.Raven.Types;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.frozenorb.Raven.Utilities.MathUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Ryan on 6/26/2015
 * <p/>
 * Project: raven
 */
public class AscendedItem {

    @Getter
    ItemStack parent;

    public AscendedItem(@Nullable String ownerDisplayName, ItemStack parent) {
        switch (parent.getType()) {
            case DIAMOND_SWORD:
                parent.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 6);
                parent.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
                break;
            case DIAMOND_PICKAXE:
                int fortuneLevel = MathUtils.random(3, 5);
                int effLevel = MathUtils.random(6, 8);
                int unbreakingLevel = MathUtils.random(1, 3);

                if (!(fortuneLevel < 1)) {
                    parent.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, fortuneLevel);
                }
                parent.addUnsafeEnchantment(Enchantment.DIG_SPEED, effLevel);
                parent.addUnsafeEnchantment(Enchantment.DURABILITY, unbreakingLevel);
                break;
        }

        ItemMeta currentMeta = parent.getItemMeta();
        currentMeta.setDisplayName((parent.getType() == Material.DIAMOND_SWORD ? "§c§lAscended Sword" : "§c§lAscended Pickaxe") + ChatColor.RESET);
        List<String> lore = currentMeta.hasLore() ? currentMeta.getLore() : Lists.<String>newArrayList();
        if (ownerDisplayName != null) {
            lore.add(" ");
            lore.add(currentMeta.getDisplayName() + " §boriginally obtained by " + ownerDisplayName);
        }
        currentMeta.setLore(lore);
        parent.setItemMeta(currentMeta);
        this.parent = parent;
    }

}
