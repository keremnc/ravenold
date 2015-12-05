package net.frozenorb.Raven.Types.LootTables;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Utilities.XPUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Ryan on 5/27/2015
 * <p/>
 * Project: raven
 */
public class Tier1LootTable extends LootTable {

    public Tier1LootTable() {
        HashSet<LootItem> items = new HashSet<>();
        setItems(items);
        {
            // add items
            addItem(new LootItem(new ItemStack(Material.DIAMOND), -1, 12, 6));
            addItem(new LootItem(new ItemStack(Material.GOLD_INGOT), 0, 30, 1));
            ItemStack stack = new ItemStack(Material.DIAMOND_PICKAXE);
            stack.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 2);
            addItem(new LootItem(stack, 15, 1));
            addItem(new LootItem(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1), 5, 1));
            addItem(new LootItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 96), 5, 1));
            addItem(new LootItem(new ItemStack(Material.BOOKSHELF), 0, 15, 5));
            addItem(new LootItem(XPUtils.createXPBottle(random(15, 31)), 0, 1));
        }


        setRandom(RavenPlugin.RANDOM);
    }

    @Override
    public void populate(Inventory inventory) {
        // Work around so XP Bottles always have a unique level
        LootItem xp = new LootItem(XPUtils.createXPBottle(random(15, 31)), 0, 1);
        addItem(xp);

        List<Material> usedMaterials = new ArrayList<>();

        LootItem item;
        item = select(true, Material.DIAMOND);
        item.setAmount((item.getMinAmount() == 0 ? getRandom().nextInt(item.getMaxWeight()) + 1 : random(item.getMinAmount(), item.getMaxWeight() + 1)));
        inventory.addItem(item.getParent());
        usedMaterials.add(item.getParent().getType());

        int given = 1, tries = 0;

        while (given <= getRandom().nextInt(2) + 1 && tries < 100) {
            item = select(false, null);
            if (usedMaterials.contains(item.getParent().getType()) || !canSpawn(item)) {
                tries++;
            } else {
                item.setAmount((item.getMinAmount() == 0 ? getRandom().nextInt(item.getMaxWeight()) + 1 : random(item.getMinAmount(), item.getMaxWeight() + 1)));
                inventory.addItem(item.getParent());
                usedMaterials.add(item.getParent().getType());
                given++;
            }

        }

        getItems().remove(xp);
    }

    @Override
    public LootItem select(boolean isMust, @Nullable Material material) {
        if (isMust) {
            return selectByMaterial(material);
        }
        return (LootItem) getItems().toArray()[getRandom().nextInt(getItems().size())];
    }


}
