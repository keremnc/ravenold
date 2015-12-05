package net.frozenorb.Raven.Types.LootTables;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

/**
 * Created by Ryan on 5/27/2015
 * <p/>
 * Project: raven
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor
public abstract class LootTable {
    @NonNull
    private Set<LootItem> items;
    private Random random;

    public boolean addItem(LootItem item) {
        return items.add(item);
    }

    public abstract void populate(Inventory inventory);

    public abstract LootItem select(boolean isMust, Material material);

    public boolean canSpawn(LootItem lootItem) {
        return lootItem.getChance() == 0 || random.nextInt(100) <= lootItem.getChance() || lootItem.getChance() == -1;
    }

    public LootItem selectByMaterial(Material material) {
        LootItem lootItem = null;
         for (LootItem item : items) {
             if (item.getParent().getData().getItemType().equals(material)) {
                 lootItem = item;
             }
         }
        return lootItem;
    }

    public int random(int min, int max) {
        int range = max - min;
        return (int) (min + (Math.random() * range));
    }

    /**
     * @param inventory
     * @param stack
     * @return true if the item was successfully added to the inventory, false if not
     * @author rbrick
     */
    public boolean addItemToInventory(Inventory inventory, ItemStack stack) {
        HashMap<Integer, ItemStack> stacksThatCouldNotBeAdded = inventory.addItem(stack);
        return !(stacksThatCouldNotBeAdded.size() > 0) || (stacksThatCouldNotBeAdded.size() >= 1 && !stacksThatCouldNotBeAdded.get(0).equals(stack));
    }
}
