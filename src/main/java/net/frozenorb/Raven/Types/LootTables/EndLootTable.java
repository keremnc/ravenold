package net.frozenorb.Raven.Types.LootTables;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Types.AscendedItem;
import net.frozenorb.Raven.Utilities.XPUtils;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Ryan on 7/1/2015
 * <p/>
 * Project: raven
 */
public class EndLootTable extends LootTable {

    List<Chest> chests;

    List<Inventory> dblockChests = new ArrayList<>(); // chests that have diamond blocks in them

    List<Inventory> xpBottles = new ArrayList<>();

    int dbAmount;

    int xpBAmount;

    public EndLootTable(List<Chest> chests) {
        this.chests = chests; // the chests
        Set<LootItem> items = new HashSet<>();
        setItems(items);
        {
            // adding diamond block & xp bottles manually, since they are done in a special way
            addItem(new LootItem(new ItemStack(Material.DIAMOND_BLOCK), -1, 6, 1));

            addItem(new LootItem(new ItemStack(Material.EMERALD_ORE), -1, 15, 1));

            // ascended items: 1% chance of being in chests
            addItem(new LootItem(new AscendedItem(null, new ItemStack(Material.DIAMOND_PICKAXE)).getParent(), 1, 1));
            addItem(new LootItem(new AscendedItem(null, new ItemStack(Material.DIAMOND_SWORD)).getParent(), 1, 1));


            // Tier 2 loot table items
            addItem(new LootItem(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1), 10, 1));
            addItem(new LootItem(new ItemStack(Material.MONSTER_EGG, 1, (short) 96), 10, 1));
            addItem(new LootItem(new ItemStack(Material.BOOKSHELF), 0, 15, 5));
            addItem(new LootItem(new ItemStack(Material.GOLD_INGOT), 0, 40, 1));


        }
        setRandom(RavenPlugin.RANDOM);
        // the amount of diamond blocks to be put in the chests
        dbAmount = chests.size() / 2;

        double percAsDec = 25/100; // 25% of chest have xp bottles in them
        double amount = percAsDec * chests.size(); // the amount of chests that should have xp bottles in them
        xpBAmount = (int) amount;
    }

    @Override
    public void populate(Inventory inventory) {
        LootItem xp = new LootItem(XPUtils.createXPBottle(random(10, 31)), 0, 1);
        addItem(xp);
        if (canSpawn(select(true, Material.DIAMOND_BLOCK))) {
            dblockChests.add(inventory);
            LootItem dblock = select(true, Material.DIAMOND_BLOCK);
            dblock.setAmount((dblock.getMinAmount() == 0 ? getRandom().nextInt(dblock.getMaxWeight()) + 1 : random(dblock.getMinAmount(), dblock.getMaxWeight() + 1)));
            inventory.addItem(dblock.getParent());
        }

        LootItem xpBottle = select(true, Material.EXP_BOTTLE);
        xpBottle.setAmount((xpBottle.getMinAmount() == 0 ? getRandom().nextInt(xpBottle.getMaxWeight()) + 1 : random(xpBottle.getMinAmount(), xpBottle.getMaxWeight() + 1)));
        inventory.addItem(xpBottle.getParent());

        LootItem emerald = select(true, Material.EMERALD_ORE);
        emerald.setAmount((emerald.getMinAmount() == 0 ? getRandom().nextInt(emerald.getMaxWeight()) + 1 : random(emerald.getMinAmount(), emerald.getMaxWeight() + 1)));
        inventory.addItem(emerald.getParent());

        for (LootItem item : getItems()) {
            if (item.getParent().getType() == Material.EMERALD_ORE || item.getParent().getType() == Material.DIAMOND_BLOCK
                    || item.getParent().getType() == Material.EXP_BOTTLE) {
                continue;
            }
            if (canSpawn(item)) {
                item.setAmount((item.getMinAmount() == 0 ? getRandom().nextInt(item.getMaxWeight()) + 1 : random(item.getMinAmount(), item.getMaxWeight() + 1)));
                inventory.addItem(item.getParent());
            }
        }

        getItems().remove(xp);


    }

    @Override
    public boolean canSpawn(LootItem lootItem) {
        if (lootItem.getParent().getType() == Material.DIAMOND_BLOCK) {
            return dblockChests.size() <= dbAmount;
        } else {
            return super.canSpawn(lootItem);
        }
    }

    @Override
    public LootItem select(boolean isMust, Material material) {
        if (isMust) {
            return selectByMaterial(material);
        }
        return (LootItem) getItems().toArray()[getRandom().nextInt(getItems().size())];
    }

    public void cleanup() {
        dblockChests.clear();
        xpBottles.clear();
    }
}
