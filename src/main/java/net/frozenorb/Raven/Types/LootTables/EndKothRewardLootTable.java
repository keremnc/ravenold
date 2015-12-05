package net.frozenorb.Raven.Types.LootTables;

import net.frozenorb.Raven.RavenPlugin;
import net.frozenorb.Raven.Types.AscendedItem;
import net.frozenorb.Raven.Types.Wrench;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Ryan on 6/30/2015
 * <p/>
 * Project: raven
 */
public class EndKothRewardLootTable extends LootTable {

    List<ItemStack> rewards = new ArrayList<>();

    public EndKothRewardLootTable() {
        HashSet<LootItem> items = new HashSet<>();
        setItems(items);
        {
            addItem(new LootItem(new ItemStack(Material.DIAMOND_BLOCK), -1, 32, 1));
            addItem(new LootItem(new ItemStack(Material.IRON_BLOCK), -1, 64, 1));
            addItem(new LootItem(new ItemStack(Material.GOLD_BLOCK), -1, 24, 1));
            addItem(new LootItem(new ItemStack(Material.EMERALD_ORE), -1, 64, 1));
        }
        setRandom(RavenPlugin.RANDOM);
    }

    @Override
    public void populate(Inventory inventory) {
        LootItem[] mustGive = {
                select(true, Material.DIAMOND_BLOCK),
                select(true, Material.IRON_BLOCK),
                select(true, Material.GOLD_BLOCK),
                select(true, Material.EMERALD_ORE)
        };

        for (LootItem item : mustGive) {
            item.setAmount((item.getMinAmount() == 0 ? getRandom().nextInt(item.getMaxWeight()) + 1 : random(item.getMinAmount(), item.getMaxWeight() + 1)));
            rewards.add(item.getParent());
            if (!addItemToInventory(inventory, item.getParent())) {
                if (inventory.getHolder() instanceof Player) {
                    Player player = (Player) inventory.getHolder();
                    player.getWorld().dropItem(player.getLocation(), item.getParent()).setVelocity(new Vector(0, 0, 0));

                }
            }
        }

        if (inventory.getHolder() instanceof Player) {
            Player player = (Player) inventory.getHolder();

            ItemStack wrench = new Wrench().get();
            wrench.setAmount(2);

            ItemStack sword = new AscendedItem(player.getDisplayName(), new ItemStack(Material.DIAMOND_SWORD)).getParent();
            sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 7);
            sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);

            ItemStack[] rareTools = {
                    sword,
                    new AscendedItem(player.getDisplayName(), new ItemStack(Material.DIAMOND_PICKAXE)).getParent(),
                    wrench
            };
            ItemStack selected = rareTools[getRandom().nextInt(rareTools.length)];
            rewards.add(selected);
            if (!addItemToInventory(inventory, selected)) {
                player.getWorld().dropItem(player.getLocation(), selected).setVelocity(new Vector(0, 0, 0));
            }
        }

    }

    @Override
    public LootItem select(boolean isMust, Material material) {
        if (isMust) {
            return selectByMaterial(material);
        }
        return (LootItem) getItems().toArray()[getRandom().nextInt(getItems().size())];
    }

    public List<ItemStack> getRewards() {
        return rewards;
    }
}
